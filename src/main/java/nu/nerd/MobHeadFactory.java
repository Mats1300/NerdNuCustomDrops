package nu.nerd;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nu.nerd.utils.EntityVariantUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

public class MobHeadFactory {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Creates a custom head item for the given entity based on configuration.
     *
     * @param entity the entity that died
     * @param config the plugin config used to define the head's appearance and data
     * @return an ItemStack of the custom head, or null if something fails
     */
    public static ItemStack createHeadFor(Entity entity, FileConfiguration config, Logger logger) {
        boolean debug = config.getBoolean("debug", false); // Respect debug flag

        if (entity == null) {
            if (debug) logger.warn("[DEBUG] Entity or config is null!");
            return null;
        }

        // Get base entity type name, e.g., "WOLF"
        String mobName = entity.getType().name();

        // Get variant key (e.g., "minecraft:pale" for wolves or frogs)
        String variantKey = EntityVariantUtils.getVariantId(entity);

        // Load the correct config section for this mob or variant
        ConfigurationSection section = config.getConfigurationSection("drops." + mobName + ".variants." + variantKey);
        if (section == null) {
            if (debug) logger.warn("[DEBUG] No variant config for {}, trying base config.", mobName);
            section = config.getConfigurationSection("drops." + mobName);
        }
        if (section == null) {
            if (debug) logger.warn("[DEBUG] No config section found for entity type: {}", mobName);
            return null;
        }

        if (debug) logger.info("[DEBUG] Creating head for {}, variant: {}", mobName, variantKey);

        // Determine material for the head, default to PLAYER_HEAD
        Material material = Material.matchMaterial(section.getString("itemstack.type", "PLAYER_HEAD"));
        if (material == null) {
            material = Material.PLAYER_HEAD;
            if (debug) logger.warn("[DEBUG] Invalid material specified, defaulting to PLAYER_HEAD for {}", mobName);
        }

        ItemStack head = new ItemStack(material);
        ItemMeta meta = head.getItemMeta();
        if (meta == null) {
            if (debug) logger.warn("[DEBUG] Failed to get ItemMeta for head!");
            return head;
        }

        // Set display name if specified
        String displayName = section.getString("itemstack.display-name");
        if (displayName != null) {
            meta.displayName(miniMessage.deserialize(displayName));
        }

        // Set lore using MiniMessage formatting
        Object loreObj = section.get("itemstack.lore");
        if (loreObj instanceof String loreStr) {
            meta.lore(List.of(miniMessage.deserialize(loreStr)));
        } else if (loreObj instanceof List<?> loreListObj) {
            meta.lore(loreListObj.stream()
                    .filter(line -> line instanceof String)
                    .map(line -> miniMessage.deserialize((String) line))
                    .toList());
        }

        // Store the head-sound in PersistentDataContainer for later use on noteblocks
        String sound = section.getString("itemstack.head-sound");
        if (sound != null && !sound.isEmpty()) {
            NamespacedKey soundKey = new NamespacedKey("nerdnucustomdrops", "head_sound");
            meta.getPersistentDataContainer().set(soundKey, PersistentDataType.STRING, sound);
        }

        head.setItemMeta(meta);

        // If it's a PLAYER_HEAD, try applying a custom texture
        if (material == Material.PLAYER_HEAD) {
            ConfigurationSection itemstack = section.getConfigurationSection("itemstack");
            if (itemstack != null) {
                String texture = itemstack.getString("internal");
                if (texture != null && !texture.isEmpty()) {
                    if (debug) logger.info("[DEBUG] Applying custom texture to player head for {}", mobName);
                    ItemStack skull = getSkull(texture);
                    ItemMeta skullMeta = skull.getItemMeta();

                    // Apply display name and lore again for the custom head
                    if (displayName != null) {
                        skullMeta.displayName(miniMessage.deserialize(displayName));
                    }

                    if (loreObj instanceof String loreStr) {
                        skullMeta.lore(List.of(miniMessage.deserialize(loreStr)));
                    } else if (loreObj instanceof List<?> loreListObj) {
                        skullMeta.lore(loreListObj.stream()
                                .filter(line -> line instanceof String)
                                .map(line -> miniMessage.deserialize((String) line))
                                .toList());
                    }

                    if (sound != null && !sound.isEmpty()) {
                        NamespacedKey soundKey = new NamespacedKey("nerdnucustomdrops", "head_sound");
                        skullMeta.getPersistentDataContainer().set(soundKey, PersistentDataType.STRING, sound);
                    }

                    skull.setItemMeta(skullMeta);
                    return skull;
                }
            }
        }

        return head;
    }

    /**
     * Creates a skull item with a custom texture encoded as a base64 string.
     *
     * @param texture base64-encoded skin texture (64-bit)
     * @return an ItemStack of a custom player head with that texture
     */
    public static ItemStack getSkull(String texture) {
        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, skullMeta -> {
            final UUID uuid = UUID.randomUUID();
            final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
            playerProfile.setProperty(new ProfileProperty("textures", texture));
            skullMeta.setPlayerProfile(playerProfile);
        });
        return head;
    }
}