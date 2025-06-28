package nu.nerd;

import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.NotePlayEvent;

import java.net.URL;

public class NoteblockHeadSoundListener implements Listener {

    private final FileConfiguration config;
    private final boolean debug;

    // Constructor: Retrieves the debug setting from the config file
    public NoteblockHeadSoundListener(FileConfiguration config) {
        this.config = config;
        this.debug = config.getBoolean("debug", false); // Default to false if "debug" is not set in config
    }

    @EventHandler
    public void onNoteBlockPlay(NotePlayEvent event) {
        Block noteBlock = event.getBlock();
        Block blockAbove = noteBlock.getRelative(BlockFace.UP);

        // Checks if the block above is a head and logs if it’s not
        if (!isHead(blockAbove.getType())) {
            logDebug("Block above is not a head, skipping.");
            return;
        }

        if (blockAbove.getType() != Material.PLAYER_HEAD) {
            logDebug("Block above is not a player head, skipping.");
            return;
        }

        if (!(blockAbove.getState() instanceof Skull skull)) {
            logDebug("Block above is not a skull, skipping.");
            return;
        }

        String matchedSound = getHeadSoundFromConfig(skull);
        if (matchedSound == null) {
            logDebug("No matching sound found for the head.");
            return;
        }

        logDebug("Matched custom head! Playing sound: " + matchedSound);

        // Plays the sound if a match is found
        noteBlock.getWorld().playSound(
                noteBlock.getLocation(),
                matchedSound,
                SoundCategory.MASTER, // switched to MASTER so it definitely plays
                1.0f,
                1.0f
        );
    }

    // Helper function to check if the block is a type of head
    private boolean isHead(Material material) {
        return material == Material.PLAYER_HEAD ||
                material == Material.ZOMBIE_HEAD ||
                material == Material.CREEPER_HEAD ||
                material == Material.SKELETON_SKULL ||
                material == Material.WITHER_SKELETON_SKULL ||
                material == Material.PIGLIN_HEAD ||
                material == Material.DRAGON_HEAD;
    }

    // Retrieves sound associated with a player's head texture from config
    private String getHeadSoundFromConfig(Skull skull) {
        PlayerProfile profile = skull.getPlayerProfile();
        if (profile == null) {
            logDebug("No player profile found on skull.");
            return null;
        }

        URL skinUrl = profile.getTextures().getSkin();
        if (skinUrl == null) {
            logDebug("No skin URL found for player profile.");
            return null;
        }

        String fullTextureUrl = skinUrl.toString();
        logDebug("Extracted texture URL: " + fullTextureUrl);

        // Look for sound in the config based on texture URL
        ConfigurationSection dropsSection = config.getConfigurationSection("drops");
        if (dropsSection == null) {
            logDebug("No drops section found in config.");
            return null;
        }

        // Iterate over all drops configurations and check for matching texture URLs
        for (String mobKey : dropsSection.getKeys(false)) {
            ConfigurationSection mobSection = dropsSection.getConfigurationSection(mobKey);
            if (mobSection == null) continue;

            String rootUrl = mobSection.getString("itemstack.url");
            if (rootUrl != null && rootUrl.equalsIgnoreCase(fullTextureUrl)) {
                String sound = mobSection.getString("itemstack.head-sound");
                logDebug("Matched root texture URL: " + rootUrl + " with sound: " + sound);
                return sound;
            }

            // Check inside variants for a matching texture
            ConfigurationSection variantsSection = mobSection.getConfigurationSection("variants");
            if (variantsSection != null) {
                for (String variantKey : variantsSection.getKeys(false)) {
                    ConfigurationSection variantSection = variantsSection.getConfigurationSection(variantKey);
                    if (variantSection == null) continue;

                    String variantUrl = variantSection.getString("itemstack.url");
                    if (variantUrl != null && variantUrl.equalsIgnoreCase(fullTextureUrl)) {
                        String sound = variantSection.getString("itemstack.head-sound");
                        logDebug("Matched variant texture URL: " + variantUrl + " with sound: " + sound);
                        return sound;
                    }
                }
            }
        }

        logDebug("No matching texture URL found in drops section.");
        return null;
    }

    // Logs debug messages based on config
    private void logDebug(String message) {
        if (debug) {
            System.out.println("[NoteBlockHeadSoundListener] " + message);
        }
    }
}