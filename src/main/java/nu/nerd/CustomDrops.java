package nu.nerd;

import org.bukkit.plugin.java.JavaPlugin;
import nu.nerd.commands.MobHeadListCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NerdNuCustomDrops - A Minecraft plugin for Nerd Nu Server
 * Licensed under the MIT License.
 * See LICENSE file for details.
 */

public class CustomDrops extends JavaPlugin {

    // SLF4J logger instance for the plugin
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDrops.class);

    // Provide access to the logger for other classes
    public Logger getSlf4jLogger() {
        return LOGGER;
    }

    // This method is called when your plugin is enabled
    @Override
    public void onEnable() {
        // Load the plugin's default config (or custom config)
        saveDefaultConfig();  // This will load the config.yml if it doesn't already exist

        // Register the MobDeathListener to handle mob deaths and custom drops
        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);

        // Register the HeadPersistenceListener to handle NBT Data to stay on heads when placed and broken
        getServer().getPluginManager().registerEvents(new HeadPersistenceListener(this, LOGGER), this);

        // Register the NoteBlockHeadSoundListener to handle noteblock sounds for the heads
        getServer().getPluginManager().registerEvents(new NoteblockHeadSoundListener(getConfig()), this);

        // Get the command object
        var command = getCommand("mobhead");

        // Ensure the command is not null before calling setExecutor
        if (command != null) {
            command.setExecutor(new MobHeadListCommand(this)); // Pass the plugin instance to the command class
        } else {
            // If the command is null, log a warning
            LOGGER.warn("The 'mobhead' command was not found in plugin.yml or failed to load.");
        }

        // Log that the plugin has been successfully enabled
        LOGGER.info("NerdNuCustomDrops plugin has been enabled!");
    }
}