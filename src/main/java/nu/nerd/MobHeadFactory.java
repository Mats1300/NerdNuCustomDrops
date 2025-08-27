package nu.nerd;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nu.nerd.utils.EntityVariantUtils;
import nu.nerd.utils.MobHeadUtils;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Factory class for creating custom mob and player heads.
 * <p>
 * Provides functionality to generate heads with custom textures, display names,
 * lore, and persistent data such as head sounds. Supports variant-specific
 * configurations for different mob subtypes.
 */
public class MobHeadFactory {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Component PLAYER_HEAD_LORE = MINI.deserialize("[Certified Authentic]"); // Consistent lore for player heads

    /**
     * Creates a custom head ItemStack for a given entity based on configuration.
     * <p>
     * Looks up variant-specific settings in the configuration, falls back to
     * the base mob configuration if necessary, and applies textures, display names,
     * lore, and head sounds as defined in the config.
     *
     * @param entity the entity that died or spawned the head
     * @param config the plugin configuration file
     * @param logger SLF4J logger for debug output
     * @return an ItemStack representing the custom head, or null if the entity
     *         or configuration section is invalid
     */
    public static ItemStack createHeadFor(Entity entity, FileConfiguration config, Logger logger) {
        boolean debug = config.getBoolean("debug", false);

        if (entity == null) {
            if (debug) logger.warn("[DEBUG] Entity is null!");
            return null;
        }

        String mobName = entity.getType().name();
        String variantKey = EntityVariantUtils.getVariantId(entity);

        // Try to get variant config section
        ConfigurationSection section = null;
        if (variantKey != null) {
            section = config.getConfigurationSection("drops." + mobName + ".variants." + variantKey);
        }

        // Fallback to base mob section if variant not found
        if (section == null) {
            section = config.getConfigurationSection("drops." + mobName);
        }

        // If still null, log debug and return null
        if (section == null) {
            if (debug) logger.warn("[DEBUG] No config section found for mob {} (variant {})", mobName, variantKey);
            return null;
        }

        if (debug) logger.info("[DEBUG] Creating head for {}, variant: {}", mobName, variantKey);

        // Get display-name from config first
        String displayNameStr = null;
        ConfigurationSection itemstackSection = section.getConfigurationSection("itemstack");
        if (itemstackSection != null) {
            displayNameStr = itemstackSection.getString("display-name");
        }

        Component displayName;
        if (displayNameStr != null && !displayNameStr.isEmpty()) {
            displayName = MINI.deserialize(displayNameStr); // Use config value
        } else {
            // Fallback auto-generated
            displayName = (variantKey != null)
                    ? MINI.deserialize(capitalize(variantKey) + " " + capitalize(mobName.toLowerCase()) + " Head")
                    : MINI.deserialize(capitalize(mobName.toLowerCase()) + " Head");
        }

        // Determine material directly from config (default to PLAYER_HEAD)
        Material material = Material.matchMaterial(section.getString("itemstack.type", "PLAYER_HEAD"));
        if (material == null) {
            material = Material.PLAYER_HEAD;
        }

        // Determine lore
        String loreString = section.getString("itemstack.lore", "");
        List<Component> lore = loreString.isEmpty() ? List.of() : List.of(MINI.deserialize(loreString));

        // Create the Itemstack
        ItemStack head = new ItemStack(material);
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            meta.displayName(displayName);
            meta.lore(lore);

            // Store head-sound
            String sound = section.getString("itemstack.head-sound");
            if (sound != null && !sound.isEmpty()) {
                NamespacedKey soundKey = new NamespacedKey("nerdnucustomdrops", "head_sound");
                meta.getPersistentDataContainer().set(soundKey, PersistentDataType.STRING, sound);
            }

            head.setItemMeta(meta);
        }

        // Apply custom player texture if available
        String texture = (itemstackSection != null) ? itemstackSection.getString("internal") : null;
        if (material == Material.PLAYER_HEAD && texture != null && !texture.isEmpty()) {
            if (debug) logger.info("[DEBUG] Applying custom texture to player head for {}", mobName);

            String profileName = MobHeadUtils.sanitizeProfileName(displayName);
            logger.info("[DEBUG] Sanitized profile name: {}", profileName);
            applyTexture(head, texture, profileName, displayName, lore);
        }

        return head;
    }

    /**
     * Applies a custom base64 texture to a player head while preserving its
     * display name and lore.
     *
     * @param head        the ItemStack representing the head
     * @param texture     the base64-encoded texture string
     * @param profileName the PlayerProfile name associated with the head
     * @param displayName the display name of the head
     * @param lore        the lore of the head
     */
    private static void applyTexture(ItemStack head, String texture, String profileName, Component displayName, List<Component> lore) {
        head.editMeta(SkullMeta.class, skullMeta -> {
            UUID uuid = UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8));
            PlayerProfile profile = Bukkit.createProfile(uuid, profileName);
            profile.setProperty(new ProfileProperty("textures", texture));
            skullMeta.setPlayerProfile(profile);
            skullMeta.displayName(displayName);
            skullMeta.lore(lore);
        });
    }

    /**
     * Creates a player head for a given player name (used for PvP drops) with consistent lore.
     *
     * @param playerName the player's name
     * @param debug whether debug logging is enabled
     * @param logger the SLF4J logger
     * @param reason why the head is being created (for debug)
     * @return ItemStack of the player head
     */
    public static ItemStack createPlayerHead(String playerName, boolean debug, Logger logger, String reason) {
        if (debug) logger.info("[DEBUG] Creating player head for: {} due to {}", playerName, reason);
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            meta.displayName(MINI.deserialize(playerName + " Head"));
            meta.lore(List.of(PLAYER_HEAD_LORE));
        });
        return head;
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the string with its first letter capitalized
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}