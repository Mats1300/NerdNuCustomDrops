package nu.nerd;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HeadPersistenceListener implements Listener {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private final NamespacedKey loreKey;
    private final NamespacedKey nameKey;
    private final NamespacedKey textureKey;
    private final boolean debug;
    private final Logger logger;

    // Constructor for initializing the listener with plugin and config
    public HeadPersistenceListener(Plugin plugin, Logger logger) {
        this.loreKey = new NamespacedKey(plugin, "head_lore");
        this.nameKey = new NamespacedKey(plugin, "head_name");
        this.textureKey = new NamespacedKey(plugin, "head_texture");
        this.debug = plugin.getConfig().getBoolean("debug", false); // Fetch the debug flag from config
        this.logger = logger;
    }

    // Event handler for when a head is placed
    @EventHandler
    public void onHeadPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.PLAYER_HEAD) return;

        Block block = event.getBlockPlaced();
        BlockState state = block.getState();
        if (!(state instanceof Skull skull)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = skull.getPersistentDataContainer();

        // Save display name and lore to PersistentDataContainer
        saveDisplayNameAndLore(meta, container);

        // Store texture from the placed item
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
            }
        }

        skull.update(true);
    }

    // Event handler for when a head is broken
    @EventHandler
    public void onHeadBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return;

        BlockState state = block.getState();
        if (!(state instanceof Skull skull)) return;

        PersistentDataContainer container = skull.getPersistentDataContainer();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        if (meta == null) return;

        // Display name
        if (container.has(nameKey, PersistentDataType.STRING)) {
            String serializedName = container.get(nameKey, PersistentDataType.STRING);
            if (serializedName != null) {
                meta.displayName(MINI.deserialize(serializedName));
            }
        }

        // Lore
        if (container.has(loreKey, PersistentDataType.STRING)) {
            String loreData = container.get(loreKey, PersistentDataType.STRING);
            if (loreData != null && !loreData.isEmpty()) {
                List<Component> lore = Stream.of(loreData.split("\\|"))
                        .map(MINI::deserialize)
                        .collect(Collectors.toList());
                meta.lore(lore);
            }
        }

        // Texture
        if (container.has(textureKey, PersistentDataType.STRING)) {
            String texture = container.get(textureKey, PersistentDataType.STRING);
            if (texture != null) {
                if (debug) logger.info("[DEBUG] Applying texture: {}", texture);
                applyTextureToSkullMeta((SkullMeta) meta, texture);
            }
        }

        head.setItemMeta(meta);
        event.setDropItems(false); // Prevent default drop
        block.getWorld().dropItemNaturally(block.getLocation(), head);
    }

    // Helper method to save display name and lore to PersistentDataContainer
    private void saveDisplayNameAndLore(ItemMeta meta, PersistentDataContainer container) {
        Component displayName = meta.displayName();
        if (displayName != null) {
            container.set(nameKey, PersistentDataType.STRING, MINI.serialize(displayName));
            if (debug) logger.info("[DEBUG] Saved display name: {}", MINI.serialize(displayName));
        }

        List<Component> lore = meta.lore();
        if (lore != null && !lore.isEmpty()) {
            String joinedLore = lore.stream()
                    .map(MINI::serialize)
                    .collect(Collectors.joining("|"));
            container.set(loreKey, PersistentDataType.STRING, joinedLore);
            if (debug) logger.info("[DEBUG] Saved lore: {}", joinedLore);
        }
    }

    // Helper method to apply a custom texture to a SkullMeta
    private void applyTextureToSkullMeta(SkullMeta skullMeta, String texture) {
        UUID uuid = UUID.randomUUID();
        PlayerProfile profile = Bukkit.createProfile(uuid, "headOwner");
        profile.setProperty(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(profile);
    }
}



