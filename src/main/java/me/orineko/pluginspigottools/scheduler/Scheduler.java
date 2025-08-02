package me.orineko.pluginspigottools.scheduler;

import lombok.NonNull;

public interface Scheduler {
    Object runTaskTimer(@NonNull Runnable task, long delay, long period);
    Object runTaskTimerAsync(@NonNull Runnable task, long delay, long period);
    Object runTaskLater(@NonNull Runnable task, long delay);
    Object runTaskLaterAsync(@NonNull Runnable task, long delay);
    Object runTask(@NonNull Runnable task);
    Object runTaskAsync(@NonNull Runnable task);
    void cancelAllTasks();
    void cancelTask(@NonNull Object taskHandle);
} 