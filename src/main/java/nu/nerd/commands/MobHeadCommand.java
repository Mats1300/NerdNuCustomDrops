package nu.nerd.commands;

import nu.nerd.CustomDrops;
import nu.nerd.MobHeadFactory;
import nu.nerd.utils.MobHeadUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("ClassCanBeRecord")
public class MobHeadCommand implements CommandExecutor, TabCompleter {

    private final CustomDrops plugin;

    public MobHeadCommand(CustomDrops plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        if (args.length == 0) {
            if (!sender.hasPermission("nerdnucustomdrops.mobhead.list")) {
                sender.sendMessage(Component.text("You do not have permission to view mob heads!", NamedTextColor.RED));
                return true;
            }
            listMobHeads(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);

        return switch (subcommand) {
            case "give" -> {
                if (!sender.hasPermission("nerdnucustomdrops.mobhead.give")) {
                    sender.sendMessage(Component.text("You do not have permission to give mob heads!", NamedTextColor.RED));
                    yield true;
                }
                yield handleGiveSubcommand(sender, args);
            }
            case "list" -> {
                if (!sender.hasPermission("nerdnucustomdrops.mobhead.list")) {
                    sender.sendMessage(Component.text("You do not have permission to view mob heads!", NamedTextColor.RED));
                    yield true;
                }
                listMobHeads(sender);
                yield true;
            }
            default -> {
                sender.sendMessage(Component.text("Unknown subcommand. Available: list, give", NamedTextColor.RED));
                yield true;
            }
        };
    }

    /**
     * Player is optional.
     * Allowed forms:
     *  - /mobhead give <player> <drop> [variant]
     *  - /mobhead give <drop> [variant]   (only if sender is a Player)
     */
    @SuppressWarnings("SameReturnValue")
    private boolean handleGiveSubcommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /mobhead give [player] <drop> [variant]", NamedTextColor.YELLOW));
            return true;
        }

        // Determine whether the first argument after 'give' is a player name
        Player target;
        String dropArg;
        String variantArg = null;

        Player possiblePlayer = Bukkit.getPlayerExact(args[1]);
        if (possiblePlayer != null && args.length >= 3) {
            // Form: /mobhead give <player> <drop> [variant]
            target = possiblePlayer;
            dropArg = args[2];
            if (args.length >= 4) variantArg = args[3];
        } else {
            // Form: /mobhead give <drop> [variant] (sender must be Player)
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Console must specify a player: /mobhead give <player> <drop> [variant]", NamedTextColor.YELLOW));
                return true;
            }
            target = (Player) sender;
            dropArg = args[1];
            if (args.length >= 3) variantArg = args[2];
        }

        String dropKey = dropArg.toUpperCase(Locale.ROOT);
        String variantKey = (variantArg != null) ? variantArg.toLowerCase(Locale.ROOT) : null;

        ConfigurationSection dropsSection = plugin.getConfig().getConfigurationSection("drops");
        if (dropsSection == null) {
            sender.sendMessage(Component.text("No mob drop configuration found!", NamedTextColor.RED));
            return true;
        }

        // Verify drop exists
        ConfigurationSection dropSection = dropsSection.getConfigurationSection(dropKey);
        if (dropSection == null) {
            sender.sendMessage(Component.text("No drop found: " + dropArg, NamedTextColor.RED));
            return true;
        }

        // If variant specified, verify it exists under the drop
        if (variantKey != null) {
            ConfigurationSection variants = dropSection.getConfigurationSection("variants");
            if (variants == null || !variants.contains(variantKey)) {
                sender.sendMessage(Component.text("Variant not found for " + dropArg + ": " + variantArg, NamedTextColor.RED));
                return true;
            }
        }

        // Create fake entity (with variant PDC if applicable)
        org.bukkit.entity.Entity fakeEntity;
        if (variantKey != null) {
            fakeEntity = MobHeadUtils.createFakeEntity(dropKey, variantKey);
        } else {
            fakeEntity = MobHeadUtils.createFakeEntity(dropKey);
        }

        if (fakeEntity == null) {
            sender.sendMessage(Component.text("Failed to create entity for: " + dropArg, NamedTextColor.RED));
            return true;
        }

        var head = MobHeadFactory.createHeadFor(fakeEntity, plugin.getConfig(), plugin.getSlf4jLogger());
        if (head == null) {
            sender.sendMessage(Component.text("Failed to generate head for: " + dropArg + (variantKey != null ? " (" + variantArg + ")" : ""), NamedTextColor.RED));
            return true;
        }

        // Give the head, drop if inventory full
        var leftover = target.getInventory().addItem(head);
        if (!leftover.isEmpty()) {
            target.getWorld().dropItemNaturally(target.getLocation(), head);
        }

        target.sendMessage(Component.text("You received a " + dropArg + (variantKey != null ? " (" + variantArg + ")" : "") + " head!", NamedTextColor.GREEN));
        if (!target.equals(sender)) {
            sender.sendMessage(Component.text("Gave a " + dropArg + (variantKey != null ? " (" + variantArg + ")" : "") + " head to " + target.getName(), NamedTextColor.GREEN));
        }

        return true;
    }

    private void listMobHeads(CommandSender sender) {
        ConfigurationSection drops = plugin.getConfig().getConfigurationSection("drops");
        if (drops == null) {
            sender.sendMessage(Component.text("No mob drop configuration found!", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Available mob heads:", NamedTextColor.GREEN));
        for (String mob : drops.getKeys(false)) {
            sender.sendMessage(Component.text("- " + mob, NamedTextColor.YELLOW));
            ConfigurationSection dropSection = drops.getConfigurationSection(mob);
            ConfigurationSection variants = dropSection != null ? dropSection.getConfigurationSection("variants") : null;
            if (variants != null) {
                for (String variant : variants.getKeys(false)) {
                    sender.sendMessage(Component.text("  - " + variant, NamedTextColor.AQUA));
                }
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                String[] args) {

        // Subcommands
        if (args.length == 1) {
            return Stream.of("list", "give")
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .sorted()
                    .toList();
        }

        // args.length == 2: could be player OR drop -> suggest both players and drops
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String input = args[1].toLowerCase(Locale.ROOT);
            List<String> suggestions = new ArrayList<>();

            // players
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(input))
                    .sorted()
                    .forEach(suggestions::add);

            // drops
            ConfigurationSection drops = plugin.getConfig().getConfigurationSection("drops");
            if (drops != null) {
                drops.getKeys(false).stream()
                        .filter(k -> k.toLowerCase(Locale.ROOT).startsWith(input))
                        .sorted()
                        .forEach(suggestions::add);
            }

            return suggestions;
        }

        // args.length == 3:
        // - if args[1] is a player -> suggest drops
        // - otherwise args[1] is a drop -> suggest variants for that drop (3rd arg is variant)
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            String firstAfterGive = args[1];
            Player maybePlayer = Bukkit.getPlayerExact(firstAfterGive);
            ConfigurationSection drops = plugin.getConfig().getConfigurationSection("drops");
            if (drops == null) return Collections.emptyList();

            if (maybePlayer != null) {
                // player was specified -> suggest drops
                String input = args[2].toLowerCase(Locale.ROOT);
                return drops.getKeys(false).stream()
                        .filter(k -> k.toLowerCase(Locale.ROOT).startsWith(input))
                        .sorted()
                        .toList();
            } else {
                // player omitted -> args[1] is drop, args[2] should be variant -> suggest variants
                ConfigurationSection dropSection = drops.getConfigurationSection(firstAfterGive.toUpperCase(Locale.ROOT));
                ConfigurationSection variants = dropSection != null ? dropSection.getConfigurationSection("variants") : null;
                if (variants == null) return Collections.emptyList();

                String input = args[2].toLowerCase(Locale.ROOT);
                return variants.getKeys(false).stream()
                        .filter(v -> v.toLowerCase(Locale.ROOT).startsWith(input))
                        .sorted()
                        .toList();
            }
        }

        // args.length == 4:
        // Only valid when firstAfterGive is a player -> args[2] is drop, args[3] is variant
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            String firstAfterGive = args[1];
            Player maybePlayer = Bukkit.getPlayerExact(firstAfterGive);
            if (maybePlayer == null) return Collections.emptyList();

            ConfigurationSection drops = plugin.getConfig().getConfigurationSection("drops");
            if (drops == null) return Collections.emptyList();

            ConfigurationSection dropSection = drops.getConfigurationSection(args[2].toUpperCase(Locale.ROOT));
            ConfigurationSection variants = dropSection != null ? dropSection.getConfigurationSection("variants") : null;
            if (variants == null) return Collections.emptyList();

            String input = args[3].toLowerCase(Locale.ROOT);
            return variants.getKeys(false).stream()
                    .filter(v -> v.toLowerCase(Locale.ROOT).startsWith(input))
                    .sorted()
                    .toList();
        }

        return Collections.emptyList();
    }
}