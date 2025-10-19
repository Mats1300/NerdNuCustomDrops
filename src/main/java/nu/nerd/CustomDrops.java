package nu.nerd;

import org.bukkit.plugin.java.JavaPlugin;
import nu.nerd.commands.MobHeadCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main plugin class for NerdNuCustomDrops.
 * <p>
 * This plugin extends Bukkit's {@link JavaPlugin} and registers listeners and commands
 * that provide custom mob head drops, persistence of head metadata, and interaction
 * with note block sounds.
 * <p>
 * Features include:
 * <ul>
 *     <li>Custom mob head drops via {@link MobDeathListener}.</li>
 *     <li>Persistence of head display name, lore, and texture via {@link HeadPersistenceListener}.</li>
 *     <li>Custom head sounds through {@link NoteblockHeadSoundListener}.</li>
 *     <li>A {@code /mobhead} command to list available mob heads.</li>
 * </ul>
 *
 * Licensed under the MIT License. See LICENSE file for details.
 */

public class CustomDrops extends JavaPlugin {

    // SLF4J logger instance for the plugin
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDrops.class);

    /**
     * Provides access to the plugin's SLF4J {@link Logger}.
     * <p>
     * Other classes should use this method instead of creating their own logger.
     *
     * @return the shared SLF4J logger
     */
    public Logger getSlf4jLogger() {
        return LOGGER;
    }

    /**
     * Called by Bukkit when the plugin is enabled.
     * <p>
     * This method:
     * <ul>
     *     <li>Loads the default configuration file if it does not exist.</li>
     *     <li>Registers event listeners for mob deaths, head persistence, and note block sounds.</li>
     *     <li>Registers the {@code /mobhead} command if defined in {@code plugin.yml}.</li>
     *     <li>Logs a warning if the command cannot be registered.</li>
     * </ul>
     */
    @Override
    public void onEnable() {
        // Load the plugin's default config (or custom config)
        saveDefaultConfig();  // This will load the config.yml if it doesn't already exist

        // Register the MobDeathListener to handle mob deaths and custom drops
        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);

        // Register the HeadPersistenceListener to handle NBT Data to stay on heads when placed and broken
        getServer().getPluginManager().registerEvents(new HeadPersistenceListener(this, LOGGER), this);

        // Register the NoteBlockHeadSoundListener to handle noteblock sounds for the heads
        getServer().getPluginManager().registerEvents(
                new NoteblockHeadSoundListener(this, getConfig()),
                this
        );

        // Register /mobhead command with executor AND tab completer
        var mobheadCommand = getCommand("mobhead");
        if (mobheadCommand != null) {
            MobHeadCommand commandHandler = new MobHeadCommand(this);
            mobheadCommand.setExecutor(commandHandler);
            mobheadCommand.setTabCompleter(commandHandler); // <--- tab completion enabled
        } else {
            LOGGER.warn("The 'mobhead' command was not found in plugin.yml or failed to load.");
        }

        // Log that the plugin has been successfully enabled
        LOGGER.info("NerdNuCustomDrops plugin has been enabled!");
    }
    /**
     * Called by Bukkit when the plugin is disabled.
     * <p>
     * This method is optional in this case, but is included to log shutdown
     * for better observability in server logs.
     */
    @Override
    public void onDisable() {
        LOGGER.info("NerdNuCustomDrops plugin has been disabled.");
    }
}