package me.orineko.pluginspigottools.utils;

import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimized utility class for common operations
 * Provides cached and performance-optimized alternatives to common operations
 * 
 * @author OriNeko
 * @version 1.1
 */
public final class OptimizedUtils {
    
    // Cache for version information
    private static final int CACHED_MINECRAFT_VERSION = parseMinecraftVersion();
    private static final String CACHED_SERVER_VERSION = Bukkit.getServer().getClass().getPackage().getName();
    
    // Cache for commonly used collections
    private static final Map<String, List<String>> PERMISSION_CACHE = new ConcurrentHashMap<>();
    
    private OptimizedUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Get cached Minecraft version (major version number)
     * @return Minecraft version (e.g., 20 for 1.20.x)
     */
    public static int getMinecraftVersion() {
        return CACHED_MINECRAFT_VERSION;
    }
    
    /**
     * Get cached server version string
     * @return Server version package name
     */
    public static String getServerVersion() {
        return CACHED_SERVER_VERSION;
    }
    
    /**
     * Parse Minecraft version once and cache it
     */
    private static int parseMinecraftVersion() {
        try {
            String version = Bukkit.getBukkitVersion();
            String[] parts = version.split("-")[0].split("\\.");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (Exception e) {
            // Log error but don't crash
            Bukkit.getLogger().warning("Failed to parse Minecraft version: " + e.getMessage());
        }
        return 20; // Default to latest known version
    }
    
    /**
     * Efficiently combine multiple string arrays into a single list
     * Optimized alternative to Stream.concat operations
     * 
     * @param arrays Variable number of string arrays to combine
     * @return Combined list of all strings
     */
    @Nonnull
    public static List<String> combineStringArrays(@Nonnull String[]... arrays) {
        if (arrays.length == 0) return new ArrayList<>();
        if (arrays.length == 1) return Arrays.asList(arrays[0]);
        
        // Calculate total size to avoid list resizing
        int totalSize = 0;
        for (String[] array : arrays) {
            totalSize += array.length;
        }
        
        List<String> result = new ArrayList<>(totalSize);
        for (String[] array : arrays) {
            Collections.addAll(result, array);
        }
        return result;
    }
    
    /**
     * Efficiently combine string arrays with existing list
     * 
     * @param existingList Existing list to add to
     * @param arrays Arrays to add
     * @return The modified existing list
     */
    @Nonnull
    public static List<String> addArraysToList(@Nonnull List<String> existingList, @Nonnull String[]... arrays) {
        for (String[] array : arrays) {
            Collections.addAll(existingList, array);
        }
        return existingList;
    }
    
    /**
     * Get or create cached permission list
     * Useful for frequently accessed permission combinations
     * 
     * @param key Cache key
     * @param permissionArrays Permission arrays to combine
     * @return Cached or newly created permission list
     */
    @Nonnull
    public static List<String> getCachedPermissions(@Nonnull String key, @Nonnull String[]... permissionArrays) {
        return PERMISSION_CACHE.computeIfAbsent(key, k -> combineStringArrays(permissionArrays));
    }
    
    /**
     * Clear permission cache (useful for plugin reload)
     */
    public static void clearPermissionCache() {
        PERMISSION_CACHE.clear();
    }
    
    /**
     * Safely format a string with color codes
     * Optimized version that handles null/empty strings efficiently
     * 
     * @param text Text to format
     * @return Formatted text or empty string if input is null/empty
     */
    @Nonnull
    public static String safeFormatColor(String text) {
        if (text == null || text.isEmpty()) return "";
        return MethodDefault.formatColor(text);
    }
    
    /**
     * Safely format a list of strings with color codes
     * Optimized version that handles null/empty lists efficiently
     * 
     * @param textList List of texts to format
     * @return Formatted list or empty list if input is null/empty
     */
    @Nonnull
    public static List<String> safeFormatColor(List<String> textList) {
        if (textList == null || textList.isEmpty()) return new ArrayList<>();
        return MethodDefault.formatColor(textList);
    }
    
    /**
     * Check if a string is null or empty efficiently
     * 
     * @param str String to check
     * @return true if string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Check if a collection is null or empty efficiently
     * 
     * @param collection Collection to check
     * @return true if collection is null or empty
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * Create a thread-safe list with initial capacity
     * 
     * @param initialCapacity Initial capacity
     * @param <T> Type parameter
     * @return Thread-safe list
     */
    @Nonnull
    public static <T> List<T> createSafeList(int initialCapacity) {
        return Collections.synchronizedList(new ArrayList<>(initialCapacity));
    }
    
    /**
     * Create a thread-safe set with initial capacity
     * 
     * @param initialCapacity Initial capacity
     * @param <T> Type parameter
     * @return Thread-safe set
     */
    @Nonnull
    public static <T> Set<T> createSafeSet(int initialCapacity) {
        return Collections.synchronizedSet(new HashSet<>(initialCapacity));
    }
}
