package nu.nerd.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MobHeadUtils {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    /**
     * Sanitizes a display name (MiniMessage string) for use as a PlayerProfile name.
     * Converts to plain text, strips invalid characters, truncates to 16 chars,
     * and returns "MobHead" if empty.
     *
     * @param displayName The display name in MiniMessage format.
     * @return A safe PlayerProfile name.
     */
    public static String sanitizeProfileName(String displayName) {
        if (displayName == null || displayName.isEmpty()) return "MobHead";
        String plain = PLAIN.serialize(MINI.deserialize(displayName));
        return sanitizePlainProfileName(plain);
    }

    /**
     * Sanitizes a display name (Component) for use as a PlayerProfile name.
     *
     * @param displayName The display name as a Component.
     * @return A safe PlayerProfile name.
     */
    public static String sanitizeProfileName(Component displayName) {
        if (displayName == null) return "MobHead";
        String plain = PLAIN.serialize(displayName);
        return sanitizePlainProfileName(plain);
    }

    /**
     * Core sanitizing logic that operates on plain text.
     * Keeps only letters, numbers, and underscores, truncates to 16 characters.
     *
     * @param plain The plain-text name.
     * @return Sanitized profile name.
     */
    private static String sanitizePlainProfileName(String plain) {
        if (plain == null || plain.isEmpty()) return "MobHead";
        // Allow underscores. Remove them here if you don't want them.
        String cleaned = plain.replaceAll("[^A-Za-z0-9_]", "");
        if (cleaned.length() > 16) cleaned = cleaned.substring(0, 16);
        return cleaned.isEmpty() ? "MobHead" : cleaned;
    }
    /**
     * Creates a temporary, "fake" Entity for head generation from a mob name.
     * This entity is not meant to exist in the world; it is used only for
     * MobHeadFactory to fetch configuration and generate ItemStacks.
     *
     * @param mobKey The mob type name (e.g., "ZOMBIE", "CREEPER").
     * @return A fake Entity for head generation, or null if invalid.
     */
    public static @Nullable Entity createFakeEntity(String mobKey) {
        if (mobKey == null || mobKey.isEmpty()) return null;

        EntityType type;
        try {
            type = EntityType.valueOf(mobKey.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null; // unknown mob type
        }

        World world = Bukkit.getWorlds().stream().findFirst().orElse(null);
        if (world == null) return null;

        Location loc = new Location(world, 0, 64, 0); // safe spawn location
        Entity entity;
        try {
            entity = world.spawnEntity(loc, type);
        } catch (Exception e) {
            return null;
        }

        // don't persist this entity between server restarts, remove immediately
        entity.setPersistent(false);
        entity.remove();

        return entity;
    }

    /**
     * Create a fake entity for the given mob key and set the variant string in PDC.
     * The variant is stored under namespaced key "nerdnucustomdrops:variant".
     * <p>
     * The variant parameter may be null — if null this behaves like the single-arg overload.
     */
    public static @Nullable Entity createFakeEntity(String mobKey, @Nullable String variantKey) {
        // Create base entity
        Entity entity = createFakeEntity(mobKey);
        if (entity == null) return null;

        if (variantKey != null && !variantKey.isEmpty()) {
            MobHeadVariantSetter.applyVariant(entity, variantKey);
        }

        return entity;
    }
}

