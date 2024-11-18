package me.orineko.pluginspigottools;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Getter
public class ThreadUtils {

    @Getter
    private static ThreadUtils instance;
    @Getter
    @Setter
    private static boolean debug;
    private final Plugin plugin;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, Future<?>> futureMap = new ConcurrentHashMap<>();

    public ThreadUtils(@NonNull Plugin plugin, int numThread) {
        instance = this;
        this.plugin = plugin;
        executorService = Executors.newFixedThreadPool(numThread);
    }

    /**
     * Run a task in new thread
     *
     * @param id         - ID NOT START WITH "MULTI_"
     * @param runnable-  is task
     * @param idWaitList - will wait to run task (optional)
     * @return this thread
     */
    @SuppressWarnings("all")
    public Future<?> submit(String id, Object objSync, @NonNull Runnable runnable, String... idWaitList) {
        if (id != null) {
            Future<?> futureChecked = getFutureById(id);
            if (futureChecked != null) return futureChecked;
        }

        if (id != null) if (futureMap.containsKey(id)) return futureMap.get(id);
        Future<?> future = executorService.submit(() -> {
            try {
                if (idWaitList != null && idWaitList.length > 0) waitById(idWaitList);
                if (objSync != null) {
                    synchronized (objSync) {
                        runnable.run();
                    }
                } else {
                    runnable.run();
                }
            } catch (Exception e) {
                sendException(e);
            }
        });
        if (id != null) futureMap.put(id, future);
        return future;
    }

    @SuppressWarnings("all")
    public Future<?> submit(String id, @NonNull Runnable runnable, String... idWaitList) {
        return submit(id, null, runnable, idWaitList);
    }

    @SuppressWarnings("all")
    public Future<?> submit(@NonNull Runnable runnable, String... idWaitList) {
        return submit(null, null, runnable, idWaitList);
    }

    @SuppressWarnings("all")
    public Future<?> submit(@NonNull Object objSync, @NonNull Runnable runnable, String... idWaitList) {
        synchronized (objSync) {
            return submit(null, runnable, idWaitList);
        }
    }

    /**
     * JUST USING IT IN THREAD
     *
     * @param idList - is id of thread
     */
    public synchronized void waitById(@NonNull String... idList) {
        for (String t : idList) {
            try {
                Future<?> future = futureMap.getOrDefault(t, null);
                if (future == null) {
                    sendExceptionThreadIdNotExist(t);
                    return;
                }
                future.get();
            } catch (Exception e) {
                sendException(e);
            }
        }
    }

    /**
     * JUST USING IT IN THREAD
     *
     * @param idList - is id of thread
     */
    public synchronized void waitByIdHasMulti(@NonNull String... idList) {
        for (String t : idList) {
            try {
                String key = futureMap.keySet().stream().filter(v -> v.startsWith("MULTI_" + t)).findAny().orElse(null);
                Future<?> future = futureMap.getOrDefault(key, null);
                if (future == null) {
                    sendExceptionThreadIdNotExist(t);
                    return;
                }
                future.get();
            } catch (Exception e) {
                sendException(e, "Id " + Arrays.toString(idList) + " is error");
            }
        }
    }

    public synchronized void sleep(long nanoSecond) {
        try {
            Thread.sleep(nanoSecond);
        } catch (Exception e) {
            sendException(e);
        }
    }

    @Nullable
    public Future<?> getFutureById(@NonNull String id) {
        Future<?> future = futureMap.getOrDefault(id, null);
        if (future != null) {
            sendExceptionThreadIdExited(id);
            return future;
        }
        return null;
    }

    public void sendException(@NonNull Exception e) {
        sendException(e, null);
    }

    public void sendException(@NonNull Exception e, String message) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (message == null) throw new RuntimeException(e);
            else throw new RuntimeException(message, e);
        });
    }

    public void sendExceptionThreadIdExited(@NonNull String id) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new ThreadIdExited("Id " + id + " already exists in thread");
        });
    }

    public void sendExceptionThreadIdNotExist(@NonNull String id) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new ThreadIdExited("Id " + id + " not exist in thread");
        });
    }

    public static synchronized void sendDebug(@NonNull String message) {
        sendDebug((Object) null, null, message);
    }

    public static synchronized void sendDebug(Object clazz, String message) {
        sendDebug(clazz, null, message);
    }

    public static synchronized void sendDebug(String playerName, @NonNull String message) {
        sendDebug((Object) null, playerName, message);
    }

    public static synchronized void sendDebugWait(Object clazz, String message, String... idWait) {
        sendDebug(clazz, null, message, idWait);
    }

    public static synchronized void sendDebugWait(String playerName, @NonNull String message, String... idWait) {
        sendDebug(null, playerName, message, idWait);
    }

    public static synchronized void sendDebug(Object clazz, String playerName, @NonNull String message, String... idWait) {
        ThreadUtils threadUtils = ThreadUtils.getInstance();
        if (threadUtils == null) return;
        threadUtils.submit(() -> {
            if (!ThreadUtils.isDebug()) return;
            ThreadUtils.getInstance().waitById(idWait);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("&a[").append(threadUtils.getPlugin().getName()).append("] ");
            if (clazz != null) {
                String clazzName = clazz.getClass().getSimpleName();
                stringBuilder.append("&7(").append(clazzName).append(") ");
            }
            if (playerName != null) {
                stringBuilder.append("&6<").append(playerName).append("> ");
            }
            stringBuilder.append("&f").append(message);
            Bukkit.getConsoleSender().sendMessage(MethodDefault.formatColor(stringBuilder.toString()));
        });
    }

    public static class ThreadIdExited extends RuntimeException {
        public ThreadIdExited(@NonNull String message) {
            super(message);
        }
    }

    public static class ThreadIdNotExist extends RuntimeException {
        public ThreadIdNotExist(@NonNull String message) {
            super(message);
        }
    }
}
