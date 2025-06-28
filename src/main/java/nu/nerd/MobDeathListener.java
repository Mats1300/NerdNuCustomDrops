package nu.nerd;

// Bukkit imports
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;

// Plugin utility import
import nu.nerd.utils.EntityVariantUtils;

/**
 * Listener that handles the logic for dropping custom mob heads on mob death.
 * This class checks for configuration-defined drop chances, looting bonuses,
 * and optional entity variants. It also logs debug messages based on config.
 */
public class MobDeathListener implements Listener {
    private final CustomDrops plugin;
    private final boolean debug;

    /**
     * Constructor initializes the plugin reference and debug mode from config.
     *
     * @param plugin The main plugin instance.
     */
    public MobDeathListener(CustomDrops plugin) {
        this.plugin = plugin;
        this.debug = plugin.getConfig().getBoolean("debug", false);
    }

    /**
     * Handles mob death events. Drops a custom head if the RNG roll succeeds
     * based on drop chances defined in the config.
     *
     * @param event The entity death event.
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Ignore non-mob entities (e.g., players, armor stands)
        if (!(entity instanceof Mob)) {
            return;
        }

        // Ensure the killer was a player
        Player killer = entity.getKiller();
        if (killer == null) return;

        FileConfiguration config = plugin.getConfig();
        String mobKey = entity.getType().name(); // Example: "ZOMBIE"
        String variantKey = EntityVariantUtils.getVariantId(entity); // Optional variant (e.g., villager profession)

        // Construct the config path based on whether a variant is available
        String configPath;
        if (variantKey != null) {
            configPath = "drops." + mobKey + ".variants." + variantKey;
            logDebug("EntityType: " + mobKey + ", Variant: " + variantKey);
        } else {
            configPath = "drops." + mobKey;
            logDebug("EntityType: " + mobKey + " (no Variant)");
        }

        logDebug("Trying to access config path: " + configPath);
        ConfigurationSection dropSection = config.getConfigurationSection(configPath);
        if (dropSection == null) {
            logDebugWarning("No config section at path: " + configPath + ", skipping drop.");
            return;
        }

        // Read drop parameters from the config
        double baseChance = dropSection.getDouble("base-drop-chance", 0.0);     // e.g. 0.05 = 5%
        double lootingBonus = dropSection.getDouble("looting-bonus", 0.0);      // e.g. 0.02 per looting level

        // Determine killer's looting level
        Enchantment looting = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("looting"));
        int lootingLevel = 0;
        if (looting != null) {
            lootingLevel = killer.getInventory().getItemInMainHand().getEnchantmentLevel(looting);
        }

        // Final chance = base + (looting bonus × looting level)
        double finalChance = baseChance + (lootingLevel * lootingBonus);

        // Roll the RNG
        if (Math.random() < finalChance) {
            // Attempt to create the mob head
            ItemStack head = MobHeadFactory.createHeadFor(entity, config, plugin.getSlf4jLogger());
            if (head != null) {
                // Drop it naturally at mob's death location
                entity.getWorld().dropItemNaturally(entity.getLocation(), head);
                logDebug("Dropped head for: " + mobKey + " (Variant: " + variantKey + ")");
            } else {
                logDebugWarning("Failed to create head item for: " + mobKey);
            }
        } else {
            logDebug("Drop chance failed for: " + mobKey);
        }
    }

    /**
     * Logs a debug info message if debugging is enabled.
     *
     * @param message The message to log.
     */
    private void logDebug(String message) {
        if (debug) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * Logs a debug warning message if debugging is enabled.
     *
     * @param message The message to log.
     */
    private void logDebugWarning(String message) {
        if (debug) {
            plugin.getLogger().warning("[DEBUG] " + message);
        }
    }
}
