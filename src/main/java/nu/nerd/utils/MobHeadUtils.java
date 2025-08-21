package nu.nerd.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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
}
