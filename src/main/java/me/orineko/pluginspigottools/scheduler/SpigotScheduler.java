package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SpigotScheduler implements Scheduler {
    private final Plugin plugin;

    public SpigotScheduler(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTaskTimer(Runnable task, long delay, long period) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskTimer(plugin, delay, period);
    }

    @Override
    public void runTaskTimerAsync(Runnable task, long delay, long period) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskTimerAsynchronously(plugin, delay, period);
    }

    @Override
    public void runTaskLater(Runnable task, long delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskLater(plugin, delay);
    }

    @Override
    public void runTaskAsync(Runnable task) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public void runTask(Runnable task) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTask(plugin);
    }
} 