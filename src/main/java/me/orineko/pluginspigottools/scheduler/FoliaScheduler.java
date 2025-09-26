package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Consumer;

public class FoliaScheduler implements Scheduler {
    private final Plugin plugin;
    private final Set<Object> tasks = Collections.synchronizedSet(new HashSet<>());

    public FoliaScheduler(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    private Object getGlobalRegionScheduler() throws Exception {
        Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
        Method method = bukkitClass.getMethod("getGlobalRegionScheduler");
        return method.invoke(null);
    }

    private Object getRegionScheduler() throws Exception {
        Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
        Method method = bukkitClass.getMethod("getRegionScheduler");
        return method.invoke(null);
    }

    // ===================== GLOBAL (NO LOCATION) ===================== //

    @Override
    public Object runTask(@NonNull Runnable task) {
        return runGlobal0("run", task);
    }

    @Override
    public Object runTaskLater(@NonNull Runnable task, long delay) {
        if (delay <= 0) delay = 1;
        return runGlobal0("runDelayed", task, delay);
    }

    @Override
    public Object runTaskTimer(@NonNull Runnable task, long delay, long period) {
        if (delay <= 0) delay = 1;
        if (period <= 0) period = 1;
        return runGlobal0("runAtFixedRate", task, delay, period);
    }

    // ===================== REGION (ASYNC - WITH LOCATION) ===================== //

    @Override
    public Object runTaskAsync(@NonNull Runnable task, @NonNull Location location) {
        return runRegion0("run", task, location);
    }

    @Override
    public Object runTaskLaterAsync(@NonNull Runnable task, @NonNull Location location, long delay) {
        if (delay <= 0) delay = 1;
        return runRegion0("runDelayed", task, location, delay);
    }

    @Override
    public Object runTaskTimerAsync(@NonNull Runnable task, @NonNull Location location, long delay, long period) {
        if (delay <= 0) delay = 1;
        if (period <= 0) period = 1;
        return runRegion0("runAtFixedRate", task, location, delay, period);
    }

    // ===================== REGION (ASYNC - RANDOM LOCATION) ===================== //

    @Override
    public Object runTaskAsync(@NonNull Runnable task) {
        Location location = getRandomLocation();
        if (location == null) return new Object();
        return runTaskAsync(task, location);
    }

    @Override
    public Object runTaskLaterAsync(@NonNull Runnable task, long delay) {
        Location location = getRandomLocation();
        if (location == null) return new Object();
        return runTaskLaterAsync(task, location, delay);
    }

    @Override
    public Object runTaskTimerAsync(@NonNull Runnable task, long delay, long period) {
        Location location = getRandomLocation();
        if (location == null) return new Object();
        return runTaskTimerAsync(task, location, delay, period);
    }

    // ===================== CANCEL ===================== //

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
                    if (!isCancelled) cancelMethod.invoke(task);
                } catch (Exception e) {
                    throw new RuntimeException("Error canceling Folia ScheduledTask", e);
                }
                return true;
            });

            // Cancel global tasks
            Object globalScheduler = getGlobalRegionScheduler();
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

    // ===================== INTERNAL HELPERS ===================== //

    private Object runGlobal0(String methodName, Runnable task, Object... extraArgs) {
        try {
            Object scheduler = getGlobalRegionScheduler();
            Class<?> clazz = scheduler.getClass();
            Class<?> consumerClass = Consumer.class;
            Object consumer = Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class[]{consumerClass},
                    (proxy, m, args) -> {
                        if (m.getName().equals("accept")) task.run();
                        return null;
                    }
            );

            List<Class<?>> params = new ArrayList<>();
            params.add(Plugin.class);
            params.add(consumerClass);
            for (Object arg : extraArgs) params.add(arg.getClass().isPrimitive() ? arg.getClass() : long.class);

            Method method = findMethod(clazz, methodName, params.size());
            Object scheduled = method.invoke(scheduler, combineArgs(plugin, consumer, extraArgs));
            tasks.add(scheduled);
            return scheduled;
        } catch (Exception e) {
            throw new RuntimeException("Error Folia Global Schedule", e);
        }
    }

    private Object runRegion0(String methodName, Runnable task, Location location, Object... extraArgs) {
        try {
            Object scheduler = getRegionScheduler();
            Class<?> clazz = scheduler.getClass();
            Class<?> consumerClass = Consumer.class;
            Object consumer = Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class[]{consumerClass},
                    (proxy, m, args) -> {
                        if (m.getName().equals("accept")) task.run();
                        return null;
                    }
            );

            List<Class<?>> params = new ArrayList<>();
            params.add(Plugin.class);
            params.add(Location.class);
            params.add(consumerClass);
            for (Object arg : extraArgs) params.add(long.class);

            Method method = findMethod(clazz, methodName, params.size());
            Object scheduled = method.invoke(scheduler, combineArgs(plugin, location, consumer, extraArgs));
            tasks.add(scheduled);
            return scheduled;
        } catch (Exception e) {
            throw new RuntimeException("Error Folia Region Schedule", e);
        }
    }

    private Method findMethod(Class<?> clazz, String name, int paramCount) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) return m;
        }
        throw new RuntimeException("Method not found: " + name + " with " + paramCount + " params");
    }

    private Object[] combineArgs(Object... args) {
        return args;
    }

    @Nullable
    private Location getRandomLocation() {
        if (Bukkit.getWorlds().isEmpty()) return null;

        Random random = new Random();
        World world = Bukkit.getWorlds().get(0);
        int radius = 160000;

        int blockX = random.nextInt(radius * 2) - radius;
        int blockZ = random.nextInt(radius * 2) - radius;
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();
        int blockY = random.nextInt(maxY - minY) + minY;

        return new Location(world, blockX + 0.5, blockY, blockZ + 0.5);
    }
}
