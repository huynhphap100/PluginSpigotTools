package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class FoliaScheduler implements Scheduler {
    private final Plugin plugin;
    private final Set<Object> tasks = Collections.synchronizedSet(new HashSet<>());

    public FoliaScheduler(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Object runTaskTimer(@NonNull Runnable task, long delay, long period) {
        if (delay <= 0) delay = 1;
        if (period <= 0) period = 1;
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method getGlobalRegionSchedulerMethod = bukkitClass.getMethod("getGlobalRegionScheduler");
            Object regionScheduler = getGlobalRegionSchedulerMethod.invoke(null);
            Class<?> regionSchedulerClass = regionScheduler.getClass();
            Method runAtFixedRateMethod = regionSchedulerClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            Class<?> consumerClass = Class.forName("java.util.function.Consumer");
            Object consumer = Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class[]{consumerClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("accept")) {
                            task.run();
                        }
                        return null;
                    }
            );
            return runAtFixedRateMethod.invoke(regionScheduler, plugin, consumer, delay, period);

        } catch (Exception e) {
            throw new RuntimeException("Error Folia Schedule", e);
        }
    }

    @Override
    public Object runTaskTimerAsync(@NonNull Runnable task, long delay, long period) {
        if (delay <= 0) delay = 1;
        if (period <= 0) period = 1;
        Location location = getLocation();
        if (location == null) return new Object();
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method getRegionSchedulerMethod = bukkitClass.getMethod("getRegionScheduler");
            Object regionScheduler = getRegionSchedulerMethod.invoke(null); // static method
            Class<?> regionSchedulerClass = regionScheduler.getClass();
            Class<?> consumerClass = Class.forName("java.util.function.Consumer");
            Object consumer = Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class[]{consumerClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("accept")) {
                            task.run();
                        }
                        return null;
                    }
            );
            Method runAtFixedRateMethod = regionSchedulerClass.getMethod("runAtFixedRate", Plugin.class, Location.class, consumerClass, long.class, long.class);
            Object scheduleTask = runAtFixedRateMethod.invoke(regionScheduler, plugin, location, consumer, delay, period);
            tasks.add(scheduleTask);
            return scheduleTask;
        } catch (Exception e) {
            throw new RuntimeException("Error Folia Schedule", e);
        }
    }

    @Override
    public Object runTaskLater(@NonNull Runnable task, long delay) {
        if (delay <= 0) delay = 1;
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method getGlobalRegionSchedulerMethod = bukkitClass.getMethod("getGlobalRegionScheduler");
            Object regionScheduler = getGlobalRegionSchedulerMethod.invoke(null); // static method
            Class<?> regionSchedulerClass = regionScheduler.getClass();
            Class<?> consumerClass = Class.forName("java.util.function.Consumer");
            Object consumer = Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class[]{consumerClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("accept")) {
                            task.run();
                        }
                        return null;
                    }
            );
            Method runDelayedMethod = regionSchedulerClass.getMethod("runDelayed", Plugin.class, consumerClass, long.class);
            return runDelayedMethod.invoke(regionScheduler, plugin, consumer, delay);
        } catch (Exception e) {
            throw new RuntimeException("Error Folia Schedule", e);
        }
    }

    @Override
    public Object runTaskLaterAsync(@NonNull Runnable task, long delay) {
        if (delay <= 0) delay = 1;
        Location location = getLocation();
        if (location == null) return new Object();
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method getRegionSchedulerMethod = bukkitClass.getMethod("getRegionScheduler");
            Object globalRegionScheduler = getRegionSchedulerMethod.invoke(null); // static method
            Class<?> globalRegionSchedulerClass = globalRegionScheduler.getClass();
            Class<?> consumerClass = Class.forName("java.util.function.Consumer");
            Object consumer = Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class[]{consumerClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("accept")) {
                            task.run();
                        }
                        return null;
                    }
            );
            Method runDelayedMethod = globalRegionSchedulerClass.getMethod("runDelayed", Plugin.class, consumerClass, long.class);
            Object scheduleTask = runDelayedMethod.invoke(globalRegionScheduler, plugin, consumer, delay);
            tasks.add(scheduleTask);
            return scheduleTask;
        } catch (Exception e) {
            throw new RuntimeException("Error Folia Schedule", e);
        }
    }

    @Override
    public Object runTask(@NonNull Runnable task) {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method getGlobalRegionSchedulerMethod = bukkitClass.getMethod("getGlobalRegionScheduler");
            Object globalRegionScheduler = getGlobalRegionSchedulerMethod.invoke(null); // static method
            Class<?> globalRegionSchedulerClass = globalRegionScheduler.getClass();
            Class<?> consumerClass = Class.forName("java.util.function.Consumer");
            Object consumer = Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class[]{consumerClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("accept")) {
                            task.run();
                        }
                        return null;
                    }
            );
            Method runMethod = globalRegionSchedulerClass.getMethod("run", Plugin.class, consumerClass);
            return runMethod.invoke(globalRegionScheduler, plugin, consumer);
        } catch (Exception e) {
            throw new RuntimeException("Error Folia Schedule", e);
        }
    }

    @Override
    public Object runTaskAsync(@NonNull Runnable task) {
        Location location = getLocation();
        if (location == null) return new Object();
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method getRegionSchedulerMethod = bukkitClass.getMethod("getRegionScheduler");
            Object regionScheduler = getRegionSchedulerMethod.invoke(null); // static method
            Class<?> regionSchedulerClass = regionScheduler.getClass();
            Class<?> consumerClass = Class.forName("java.util.function.Consumer");
            Object consumer = Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class[]{consumerClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("accept")) {
                            task.run();
                        }
                        return null;
                    }
            );
            Method runMethod = regionSchedulerClass.getMethod("run", Plugin.class, Location.class, consumerClass);
            Object scheduleTask = runMethod.invoke(regionScheduler, plugin, location, consumer);
            tasks.add(scheduleTask);
            return scheduleTask;
        } catch (Exception e) {
            throw new RuntimeException("Error Folia Schedule", e);
        }
    }

    @Override
    public void cancelAllTasks() {
        try {
            Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            Method isCancelledMethod = scheduledTaskClass.getMethod("isCancelled");
            Method cancelMethod = scheduledTaskClass.getMethod("cancel");
            tasks.removeIf(task -> {
                if (!scheduledTaskClass.isInstance(task)) return true;
                try {
                    boolean isCancelled = (boolean) isCancelledMethod.invoke(task);
                    if (!isCancelled) {
                        cancelMethod.invoke(task);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error canceling Folia ScheduledTask", e);
                }
                return true;
            });
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method getGlobalRegionSchedulerMethod = bukkitClass.getMethod("getGlobalRegionScheduler");
            Object globalScheduler = getGlobalRegionSchedulerMethod.invoke(null);
            Method cancelTasksMethod = globalScheduler.getClass().getMethod("cancelTasks", Plugin.class);
            cancelTasksMethod.invoke(globalScheduler, plugin);
        } catch (Exception e) {
            throw new RuntimeException("Error during cancelAllTasks", e);
        }
    }

    @Override
    public void cancelTask(@NonNull Object taskHandle) {
        try {
            Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            if (!scheduledTaskClass.isInstance(taskHandle)) return;
            Method cancelMethod = scheduledTaskClass.getMethod("cancel");
            cancelMethod.invoke(taskHandle);
            tasks.remove(taskHandle);
        } catch (Exception e) {
            throw new RuntimeException("Failed to cancel task via reflection", e);
        }
    }

    @Nullable
    private Location getLocation() {
        if (Bukkit.getWorlds().isEmpty()) return null;

        Random random = new Random();
        World world = Bukkit.getWorlds().get(0);
        int radius = 160000; // block radius from (0, 0)

        // Random block coordinates
        int blockX = random.nextInt(radius * 2) - radius;
        int blockZ = random.nextInt(radius * 2) - radius;

        // Random Y within build height range
        int minY = world.getMinHeight();       // Usually -64
        int maxY = world.getMaxHeight();       // Usually 319
        int blockY = random.nextInt(maxY - minY) + minY;

        return new Location(world, blockX + 0.5, blockY, blockZ + 0.5);
    }
} 