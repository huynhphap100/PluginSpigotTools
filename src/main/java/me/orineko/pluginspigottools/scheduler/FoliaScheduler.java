package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class FoliaScheduler implements Scheduler {
    private final Plugin plugin;
    private final Location location;

    public FoliaScheduler(@NonNull Plugin plugin) {
        this.plugin = plugin;
        this.location = Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    private Object getRegionScheduler() {
        try {
            Method m = Bukkit.class.getMethod("getRegionScheduler");
            return m.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Folia RegionScheduler API not found", e);
        }
    }

    private Object getGlobalRegionScheduler() {
        try {
            Method m = Bukkit.class.getMethod("getGlobalRegionScheduler");
            return m.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Folia GlobalRegionScheduler API not found", e);
        }
    }

    @Override
    public void runTaskTimer(Runnable task, long delay, long period) {
        if(delay <= 0) delay = 1;
        if(period <= 0) period = 1;
        Object scheduler = getRegionScheduler();
        try {
            Method m = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Location.class, java.util.function.Consumer.class, long.class, long.class);
            m.invoke(scheduler, plugin, location, (java.util.function.Consumer<Object>) scheduledTask -> task.run(), delay, period);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke runAtFixedRate via reflection", e);
        }
    }

    @Override
    public void runTaskTimerAsync(Runnable task, long delay, long period) {
        if(delay <= 0) delay = 1;
        if(period <= 0) period = 1;
        Object scheduler = getGlobalRegionScheduler();
        try {
            Method m = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, java.util.function.Consumer.class, long.class, long.class);
            m.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) scheduledTask -> task.run(), delay, period);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke runAtFixedRate (async) via reflection", e);
        }
    }

    @Override
    public void runTaskLater(Runnable task, long delay) {
        if(delay <= 0) delay = 1;
        Object scheduler = getRegionScheduler();
        try {
            Method m = scheduler.getClass().getMethod("runDelayed", Plugin.class, Location.class, java.util.function.Consumer.class, long.class);
            m.invoke(scheduler, plugin, location, (java.util.function.Consumer<Object>) scheduledTask -> task.run(), delay);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke runDelayed via reflection", e);
        }
    }

    @Override
    public void runTaskAsync(Runnable task) {
        Object scheduler = getGlobalRegionScheduler();
        try {
            Method m = scheduler.getClass().getMethod("execute", Plugin.class, Runnable.class);
            m.invoke(scheduler, plugin, task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke execute (async) via reflection", e);
        }
    }

    @Override
    public void runTask(Runnable task) {
        Object scheduler = getGlobalRegionScheduler();
        try {
            Method m = scheduler.getClass().getMethod("execute", Plugin.class, Location.class, Runnable.class);
            m.invoke(scheduler, plugin, location, task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke execute via reflection", e);
        }
    }
} 