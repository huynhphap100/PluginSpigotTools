package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;
import org.bukkit.plugin.Plugin;

public class SchedulerProvider {
    public static Scheduler get(@NonNull Plugin plugin) {
        if (isFolia()) {
            return new FoliaScheduler(plugin);
        } else {
            return new SpigotScheduler(plugin);
        }
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
} 