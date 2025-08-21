package nu.nerd;

import nu.nerd.utils.EntityVariantUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

/**
 * Listens for mob and player deaths and handles head drops.
 * <p>
 * Supports custom head drops for mobs based on configuration,
 * vanilla head drops for standard mobs, and player heads
 * when killed by other players or charged creepers. Also
 * tracks damage sources to determine killer metadata.
 */
public class MobDeathListener implements Listener {

    private final CustomDrops plugin;
    private final boolean debug;
    private static final String CHARGED_CREEPER_KEY = "NerdHeadsChargedCreeper";
    private static final String PLAYER_NAME_KEY = "NerdHeadsPlayer";

    /**
     * Constructs a new MobDeathListener.
     *
     * @param plugin the main plugin instance used for configuration and logging
     */
    public MobDeathListener(CustomDrops plugin) {
        this.plugin = plugin;
        this.debug = plugin.getConfig().getBoolean("debug", false);
    }

    /**
     * Tracks damage dealt to entities and tags them for potential head drops.
     * <p>
     * If a mob is killed by a charged creeper, it tags the victim for
     * a guaranteed head drop. If killed by a player or a player-shot projectile,
     * it tags the victim with the killer's name.
     *
     * @param event the EntityDamageByEntityEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof LivingEntity living)) return;

        double finalHealth = living.getHealth() - event.getFinalDamage();
        Entity damager = event.getDamager();

        if (debug) {
            plugin.getLogger().info("[DEBUG] DamageEvent: " + victim.getType() + " finalHealth=" + finalHealth + " by " + damager.getType());
        }

        // Case 1: Tagged for charged creeper kills
        if (damager instanceof Creeper creeper && creeper.isPowered() && finalHealth <= 0) {
            victim.setMetadata(CHARGED_CREEPER_KEY, new FixedMetadataValue(plugin, true));
            if (debug) plugin.getLogger().info("[DEBUG] Tagged for charged creeper head drop: " + victim.getType());
            return;
        }

        // Case 2: Tagged for player kills or player-shot projectiles
        Player player = null;
        if (damager instanceof Player p) player = p;
        else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player shooter) player = shooter;

        if (player != null) {
            victim.setMetadata(PLAYER_NAME_KEY, new FixedMetadataValue(plugin, player.getName()));
            if (debug) plugin.getLogger().info("[DEBUG] Tagged mob for player kill: " + player.getName() + " -> " + victim.getType());
        }
    }


    /**
     * Handles entity deaths and drops heads based on the source of the kill.
     * <p>
     * - Drops player heads if a player dies and was killed by another player or a charged creeper.
     * - Drops mob heads based on configuration if the mob was killed by a player or charged creeper.
     *
     * @param event the EntityDeathEvent
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        FileConfiguration config = plugin.getConfig();

        // ----------------------
        // REMOVE VANILLA HEAD DROPS
        // ----------------------
        event.getDrops().removeIf(item ->
                item.getType() == Material.ZOMBIE_HEAD ||
                        item.getType() == Material.SKELETON_SKULL ||
                        item.getType() == Material.WITHER_SKELETON_SKULL ||
                        item.getType() == Material.CREEPER_HEAD ||
                        item.getType() == Material.DRAGON_HEAD ||
                        item.getType() == Material.PIGLIN_HEAD
        );

        // ----------------------
        // PLAYER DEATHS
        // ----------------------
        if (entity instanceof Player deadPlayer) {
            // Drop player head if killed by another player
            if (deadPlayer.getKiller() != null) {
                dropPlayerHead(deadPlayer, "player kill");

                // Drop player head if killed by a charged creeper
            } else if (entity.hasMetadata(CHARGED_CREEPER_KEY)) {
                dropPlayerHead(deadPlayer, "charged creeper");
            }
            return;
        }

        // ----------------------
        // ONLY HANDLE MOBS
        // ----------------------
        if (!(entity instanceof Mob mob)) return;

        // ----------------------
        // CHARGED CREEPER KILLS
        // ----------------------
        if (entity.hasMetadata(CHARGED_CREEPER_KEY)) {
            dropMobHead(mob, config, "charged creeper");
            return;
        }

        // ----------------------
        // PLAYER KILLS
        // ----------------------
        MetadataValue playerMeta = entity.getMetadata(PLAYER_NAME_KEY).stream().findFirst().orElse(null);
        if (playerMeta != null) {
            Player killer = mob.getKiller();
            if (killer == null) return;

            // Get mob type and variant for config lookup
            String mobKey = mob.getType().name();
            String variantKey = EntityVariantUtils.getVariantId(mob);

            // Determine configuration path:
            // - If the mob has a variant, use drops.<MobType>.variants.<Variant>
            // - Otherwise, use drops.<MobType>
            String configPath = variantKey != null ? "drops." + mobKey + ".variants." + variantKey : "drops." + mobKey;
            ConfigurationSection dropSection = config.getConfigurationSection(configPath);
            if (dropSection == null) return;

            // ----------------------
            // DROP CHANCE CALCULATION
            // ----------------------
            int lootingLevel = getLootingLevel(killer);
            double baseChance = dropSection.getDouble("base-drop-chance", 0.0);
            double lootingBonus = dropSection.getDouble("looting-bonus", 0.0);
            double finalChance = baseChance + (lootingBonus * lootingLevel);

            // ----------------------
            // ATTEMPT HEAD DROP
            // ----------------------
            if (Math.random() < finalChance) {
                dropMobHead(mob, config, "player kill");
            } else if (debug) {
                plugin.getLogger().info("[DEBUG] Drop chance failed for: " + mob.getType() +
                        (variantKey != null ? " (Variant: " + variantKey + ")" : ""));
            }
        }
    }

    /**
     * Drops a custom mob head at the mob's location.
     *
     * @param mob    the mob whose head to drop
     * @param config the plugin configuration for custom head creation
     * @param reason reason for the head drop (used in debug logs)
     */
    private void dropMobHead(Mob mob, FileConfiguration config, String reason) {
        ItemStack head = MobHeadFactory.createHeadFor(mob, config, plugin.getSlf4jLogger());
        if (head != null) {
            mob.getWorld().dropItemNaturally(mob.getLocation(), head);
            if (debug) plugin.getLogger().info("[DEBUG] Dropped head for " + mob.getType() + " due to " + reason);
        }
    }

    /**
     * Drops a player head at the player's location.
     *
     * @param deadPlayer the player whose head to drop
     * @param reason     reason for the head drop (used in debug logs)
     */
    private void dropPlayerHead(Player deadPlayer, String reason) {
        ItemStack head = MobHeadFactory.createPlayerHead(deadPlayer.getName(), debug, plugin.getSlf4jLogger(), reason);
        deadPlayer.getWorld().dropItemNaturally(deadPlayer.getLocation(), head);
        if (debug) {
            plugin.getLogger().info("[DEBUG] Dropped player head for: " + deadPlayer.getName() + " due to " + reason);
        }
    }

    /**
     * Returns the looting enchantment level of the player's main hand item.
     *
     * @param player the player to check
     * @return looting level (0 if not enchanted)
     */
    private int getLootingLevel(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item.getEnchantmentLevel(Enchantment.LOOTING);
    }
}
