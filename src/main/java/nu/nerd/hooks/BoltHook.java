package nu.nerd.hooks;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.popcraft.bolt.BoltAPI;

public class BoltHook {
    private final BoltAPI bolt;

    public BoltHook() {
        this.bolt = Bukkit.getServer().getServicesManager().load(BoltAPI.class);
    }

    public boolean isAvailable() {
        return bolt != null;
    }

    public boolean canAccess(Block block, Player player, String... perms) {
        if (!isAvailable()) return true;
        return bolt.canAccess(block, player, perms);
    }

    @SuppressWarnings("unused")
    public boolean canAccess(Entity entity, Player player, String... perms) {
        if (!isAvailable()) return true;
        return bolt.canAccess(entity, player, perms);
    }
}