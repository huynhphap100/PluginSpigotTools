package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SpigotScheduler implements Scheduler {
    private final Plugin plugin;

    public SpigotScheduler(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Object runTaskTimer(@NonNull Runnable task, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    @Override
    public Object runTaskTimerAsync(@NonNull Runnable task, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
    }

    @Override
    public Object runTaskLater(@NonNull Runnable task, long delay) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    @Override
    public Object runTaskLaterAsync(@NonNull Runnable task, long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }

    @Override
    public Object runTaskAsync(@NonNull Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public Object runTaskTimerAsync(@NonNull Runnable task, @NonNull Location location, long delay, long period) {
        return runTaskTimerAsync(task, delay, period);
    }

    @Override
    public Object runTaskLaterAsync(@NonNull Runnable task, @NonNull Location location, long delay) {
        return runTaskLaterAsync(task, delay);
    }

    @Override
    public Object runTaskAsync(@NonNull Runnable task, @NonNull Location location) {
        return runTaskAsync(task);
    }

    @Override
    public Object runTask(@NonNull Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    @Override
    public void cancelTask(@NonNull Object taskHandle) {
        if (taskHandle instanceof BukkitTask) {
            BukkitTask bukkitTask = (BukkitTask) taskHandle;
            Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
        }
    }
} 