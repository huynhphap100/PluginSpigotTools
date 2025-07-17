package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpigotScheduler implements Scheduler {
    private final Plugin plugin;
    private final Set<BukkitTask> tasks = ConcurrentHashMap.newKeySet();

    public SpigotScheduler(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTaskTimer(Runnable task, long delay, long period) {
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskTimer(plugin, delay, period);
        tasks.add(bukkitTask);
    }

    @Override
    public void runTaskTimerAsync(Runnable task, long delay, long period) {
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskTimerAsynchronously(plugin, delay, period);
        tasks.add(bukkitTask);
    }

    @Override
    public void runTaskLater(Runnable task, long delay) {
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskLater(plugin, delay);
        tasks.add(bukkitTask);
    }

    @Override
    public void runTaskAsync(Runnable task) {
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskAsynchronously(plugin);
        tasks.add(bukkitTask);
    }

    @Override
    public void runTask(Runnable task) {
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTask(plugin);
        tasks.add(bukkitTask);
    }

    @Override
    public void cancelAllTasks() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }

    @Override
    public void cancelTask(Object taskHandle) {
        if (taskHandle instanceof BukkitTask) {
            ((BukkitTask) taskHandle).cancel();
            tasks.remove(taskHandle);
        }
    }
} 