package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;
import org.bukkit.Location;

public interface Scheduler {

    // --- Global Scheduler ---
    Object runTaskTimer(@NonNull Runnable task, long delay, long period);
    Object runTaskTimerAsync(@NonNull Runnable task, long delay, long period);
    Object runTaskLater(@NonNull Runnable task, long delay);
    Object runTaskLaterAsync(@NonNull Runnable task, long delay);
    Object runTask(@NonNull Runnable task);
    Object runTaskAsync(@NonNull Runnable task);

    // --- Region Scheduler (có Location) ---
    Object runTaskTimerAsync(@NonNull Runnable task, @NonNull Location location, long delay, long period);
    Object runTaskLaterAsync(@NonNull Runnable task, @NonNull Location location, long delay);
    Object runTaskAsync(@NonNull Runnable task, @NonNull Location location);

    // --- Cancel ---
    void cancelAllTasks();
    void cancelTask(@NonNull Object taskHandle);
} 