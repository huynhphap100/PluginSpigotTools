package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class FoliaScheduler implements Scheduler {
    private final Plugin plugin;
    private final Location location;

    public FoliaScheduler(@NonNull Plugin plugin) {
        this.plugin = plugin;
        // Lấy location đầu tiên của world đầu tiên, hoặc có thể truyền vào nếu cần
        this.location = Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    @Override
    public void runTaskTimer(Runnable task, long delay, long period) {
        if(delay <= 0) delay = 1;
        if(period <= 0) period = 1;
        Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, scheduledTask -> task.run(), delay, period);
    }

    @Override
    public void runTaskTimerAsync(Runnable task, long delay, long period) {
        if(delay <= 0) delay = 1;
        if(period <= 0) period = 1;
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
    }

    @Override
    public void runTaskLater(Runnable task, long delay) {
        if(delay <= 0) delay = 1;
        Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay);
    }

    @Override
    public void runTaskAsync(Runnable task) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, task);
    }

    @Override
    public void runTask(Runnable task) {
        Bukkit.getRegionScheduler().execute(plugin, location, task);
    }
} 