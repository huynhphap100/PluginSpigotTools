package me.orineko.pluginspigottools.scheduler;

public interface Scheduler {
    void runTaskTimer(Runnable task, long delay, long period);
    void runTaskTimerAsync(Runnable task, long delay, long period);
    void runTaskLater(Runnable task, long delay);
    void runTaskAsync(Runnable task);
    void runTask(Runnable task);
} 