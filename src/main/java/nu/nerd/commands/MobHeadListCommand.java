package nu.nerd.commands;

import nu.nerd.CustomDrops;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MobHeadListCommand implements CommandExecutor {

    private final CustomDrops plugin;

    public MobHeadListCommand(CustomDrops plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof Player player) {  // Pattern matching for instance check
            if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
                // Handle listing the mob heads
                StringBuilder mobHeadsList = new StringBuilder("Available Mob Heads:\n");

                // Check if the "drops" section exists and is not null
                if (plugin.getConfig().contains("drops")) {
                    var dropsSection = plugin.getConfig().getConfigurationSection("drops");
                    if (dropsSection != null) {
                        dropsSection.getKeys(false).forEach(mobKey -> mobHeadsList.append("- ").append(mobKey).append("\n"));
                    } else {
                        player.sendMessage("No mob heads available in the configuration.");
                        return false;
                    }
                } else {
                    player.sendMessage("No 'drops' section found in the configuration.");
                    return false;
                }

                player.sendMessage(mobHeadsList.toString());
            } else {
                player.sendMessage("Usage: /mobhead list");
            }
        } else {
            sender.sendMessage("This command is only available for players.");
        }
        return true;
    }
}

