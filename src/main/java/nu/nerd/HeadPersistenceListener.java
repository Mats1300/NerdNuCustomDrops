package nu.nerd;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nu.nerd.utils.MobHeadUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Listener to ensure Minecraft player heads persist their metadata across placement and break events.
 * <p>
 * This class handles:
 * <ul>
 *     <li>Preserving a head's display name and lore using MiniMessage serialization.</li>
 *     <li>Persisting custom player textures through {@link PlayerProfile} and PDC.</li>
 *     <li>Ensuring the "[Certified Authentic]" lore line is always present exactly once.</li>
 *     <li>Dropping the correct ItemStack on head break while preventing vanilla drops.</li>
 * </ul>
 */
public class HeadPersistenceListener implements Listener {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    /** Standard lore line for certified heads */
    private static final Component PLAYER_HEAD_LORE = MINI.deserialize("[Certified Authentic]");

    /** Delimiter used to serialize multiple lore lines into a single string */
    private static final String LORE_DELIM = "\u001F"; // Unit Separator

    /** PDC keys for storing head data */
    private final NamespacedKey loreKey;
    private final NamespacedKey nameKey;
    private final NamespacedKey textureKey;
    private final NamespacedKey certifiedFlagKey;
    private final NamespacedKey materialKey;

    private final boolean debug;
    private final Logger logger;

    /**
     * Constructs a new {@code HeadPersistenceListener}.
     *
     * @param plugin the plugin instance (used for NamespacedKey and config access)
     * @param logger logger instance for optional debug output
     */
    public HeadPersistenceListener(Plugin plugin, Logger logger) {
        this.loreKey = new NamespacedKey(plugin, "head_lore");
        this.nameKey = new NamespacedKey(plugin, "head_name");
        this.textureKey = new NamespacedKey(plugin, "head_texture");
        this.certifiedFlagKey = new NamespacedKey(plugin, "certified_added");
        this.materialKey = new NamespacedKey(plugin, "head_material");
        this.debug = plugin.getConfig().getBoolean("debug", false);
        this.logger = logger;
    }

    /**
     * Handles {@link BlockPlaceEvent} for player heads.
     * <p>
     * Saves the head's display name, lore, and texture to the block's {@link PersistentDataContainer}
     * so it can be restored upon break. Also sets a flag if the head already has the
     * "[Certified Authentic]" lore.
     *
     * @param event the block place event
     */
    @EventHandler
    public void onHeadPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!isSkullType(item.getType())) return;

        BlockState state = event.getBlockPlaced().getState();
        if (!(state instanceof Skull skull)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = skull.getPersistentDataContainer();

        // Store material
        container.set(materialKey, PersistentDataType.STRING, item.getType().name());

        // Save display name and lore (MiniMessage-serialized)
        saveDisplayNameAndLore(meta, container);

        // Save the texture from the item skull
        if (meta instanceof SkullMeta skullMeta) {
            PlayerProfile profile = skullMeta.getPlayerProfile();
            if (profile != null) {
                profile.getProperties().stream()
                        .filter(p -> p.getName().equals("textures"))
                        .findFirst()
                        .ifPresent(property -> {
                            if (debug) logger.info("[DEBUG] Storing texture: {}", property.getValue());
                            container.set(textureKey, PersistentDataType.STRING, property.getValue());
                        });

                // Also mark the flag if the lore already includes the certified line
                List<Component> lore = skullMeta.lore();
                if (lore != null && !lore.isEmpty()) {
                    final String targetPlain = PLAIN.serialize(PLAYER_HEAD_LORE);
                    boolean hasCertified = lore.stream()
                            .map(PLAIN::serialize)
                            .anyMatch(s -> s.equals(targetPlain));
                    if (hasCertified) {
                        skullMeta.getPersistentDataContainer().set(certifiedFlagKey, PersistentDataType.BYTE, (byte) 1);
                    }
                }
            }
        }

        skull.update(true);
    }

    /**
     * Handles {@link BlockBreakEvent} for player heads.
     * <p>
     * Restores the display name, lore, and texture from the block's PDC.
     * Ensures the "[Certified Authentic]" lore is present exactly once.
     * Prevents the normal block drop and drops a properly reconstructed ItemStack.
     *
     * @param event the block break event
     */
    @EventHandler
    public void onHeadBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isSkullType(block.getType())) return;

        BlockState state = block.getState();
        if (!(state instanceof Skull skull)) return;

        PersistentDataContainer container = skull.getPersistentDataContainer();

        // Determine head material to drop
        Material dropMaterial = block.getType();
        String serializedMaterial = container.get(new NamespacedKey("nerd", "head_material"), PersistentDataType.STRING);
        if (serializedMaterial != null) {
            try {
                dropMaterial = Material.valueOf(serializedMaterial);
            } catch (IllegalArgumentException ignored) {}
        }

        ItemStack head = new ItemStack(dropMaterial);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta == null) return;


        // Restore display name
        String serializedName = container.get(nameKey, PersistentDataType.STRING);
        if (serializedName != null && !serializedName.isEmpty()) {
            skullMeta.displayName(MINI.deserialize(serializedName));
        }

        // Restore lore (MiniMessage-deserialized)
        List<Component> lore = null;
        String loreData = container.get(loreKey, PersistentDataType.STRING);
        if (loreData != null && !loreData.isEmpty()) {
            lore = Stream.of(loreData.split(LORE_DELIM))
                    .map(MINI::deserialize)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        if (lore == null) lore = new ArrayList<>();

        // Certified Authentic: plain-text check + PDC flag
        final String targetPlain = PLAIN.serialize(PLAYER_HEAD_LORE);
        boolean hasPlain = lore.stream().map(PLAIN::serialize).anyMatch(s -> s.equals(targetPlain));
        boolean hadFlag = container.has(certifiedFlagKey, PersistentDataType.BYTE);

        if (!hasPlain && !hadFlag) {
            lore.add(PLAYER_HEAD_LORE);
            skullMeta.getPersistentDataContainer().set(certifiedFlagKey, PersistentDataType.BYTE, (byte) 1);
        }
        skullMeta.lore(lore);

        // Only apply texture if this is a player head
        if (dropMaterial == Material.PLAYER_HEAD && container.has(textureKey, PersistentDataType.STRING)) {
            String texture = container.get(textureKey, PersistentDataType.STRING);
            if (texture != null && !texture.isEmpty()) {
                String profileName = serializedName != null ? PLAIN.serialize(MINI.deserialize(serializedName)) : "player";
                applyTextureToSkullMeta(skullMeta, texture, profileName);
            }
        }

        head.setItemMeta(skullMeta);

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), head);
    }

    /**
     * Serializes and stores the display name and lore of a head into a {@link PersistentDataContainer}.
     *
     * @param meta the head item meta containing display name and lore
     * @param container the persistent data container of the skull block
     */
    private void saveDisplayNameAndLore(ItemMeta meta, PersistentDataContainer container) {
        Component displayName = meta.displayName();
        if (displayName != null) {
            String mm = MINI.serialize(displayName);
            container.set(nameKey, PersistentDataType.STRING, mm);
            if (debug) logger.info("[DEBUG] Saved display name: {}", mm);
        }

        List<Component> lore = meta.lore();
        if (lore != null && !lore.isEmpty()) {
            String joinedLore = lore.stream()
                    .map(MINI::serialize)
                    .collect(Collectors.joining(LORE_DELIM));
            container.set(loreKey, PersistentDataType.STRING, joinedLore);
            if (debug) logger.info("[DEBUG] Saved lore: {}", joinedLore);
        }
    }

    /**
     * Applies a custom texture to a {@link SkullMeta} using a sanitized profile name.
     *
     * @param skullMeta the SkullMeta to modify
     * @param texture base64 texture string
     * @param profileName the sanitized profile name to use for the player profile
     */
    private void applyTextureToSkullMeta(SkullMeta skullMeta, String texture, String profileName) {
        UUID uuid = UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8));
        String safeName = MobHeadUtils.sanitizeProfileName(profileName);
        PlayerProfile profile = Bukkit.createProfile(uuid, safeName);
        profile.setProperty(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(profile);
    }

    /**
     * Checks if a {@link Material} represents any type of Minecraft skull, including
     * player heads and all vanilla mob heads (zombie, skeleton, wither skeleton,
     * creeper, dragon, piglin).
     *
     * <p>Used to filter blocks or items so that head-specific logic can be applied
     * only to skull types.</p>
     *
     * @param type the material to check
     * @return {@code true} if the material is a skull type, {@code false} otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isSkullType(Material type) {
        return switch (type) {
            case PLAYER_HEAD, PLAYER_WALL_HEAD,
                 ZOMBIE_HEAD, ZOMBIE_WALL_HEAD,
                 SKELETON_SKULL, SKELETON_WALL_SKULL,
                 WITHER_SKELETON_SKULL, WITHER_SKELETON_WALL_SKULL,
                 CREEPER_HEAD, CREEPER_WALL_HEAD,
                 DRAGON_HEAD, DRAGON_WALL_HEAD,
                 PIGLIN_HEAD, PIGLIN_WALL_HEAD -> true;
            default -> false;
        };
    }
}
