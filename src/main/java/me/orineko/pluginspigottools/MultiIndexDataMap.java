package me.orineko.pluginspigottools;

import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Multi-index data storage for O(1) lookups on multiple fields
 * Pre-indexes commonly searched fields to avoid O(n) searches
 * 
 * Example: PlayerData can be searched by name, id, uuid, level, etc. - all O(1)
 * 
 * @param <V> Value type (PlayerData, GuildData, etc.)
 * 
 * @author OriNeko
 * @version 1.0
 */
@Getter
public abstract class MultiIndexDataMap<V> {

    // Primary storage
    private final Map<Object, V> primaryMap;
    
    // Secondary indexes for fast lookups
    private final Map<String, Map<Object, V>> secondaryIndexes;
    
    // Index definitions
    private final Map<String, Function<V, Object>> indexExtractors;

    public MultiIndexDataMap() {
        this.primaryMap = new ConcurrentHashMap<>();
        this.secondaryIndexes = new ConcurrentHashMap<>();
        this.indexExtractors = new ConcurrentHashMap<>();
        
        // Setup indexes in subclass
        setupIndexes();
    }

    /**
     * Abstract method to setup indexes
     * Define which fields should be indexed for fast lookup
     */
    protected abstract void setupIndexes();

    /**
     * Abstract method to get primary key
     */
    @Nonnull
    protected abstract Object getPrimaryKey(@Nonnull V data);

    /**
     * Add an index for fast lookup
     * Call this in setupIndexes() method
     * 
     * @param indexName Name of the index (e.g., "name", "id", "level")
     * @param extractor Function to extract the indexed value from data
     */
    protected void addIndex(@Nonnull String indexName, @Nonnull Function<V, Object> extractor) {
        indexExtractors.put(indexName, extractor);
        secondaryIndexes.put(indexName, new ConcurrentHashMap<>());
    }

    /**
     * Add data with automatic indexing
     * O(1) complexity for all future lookups
     * 
     * @param newData Data to add
     * @param getData Existing data (if not null, returns this instead)
     * @return Added data or existing data
     */
    public V addData(@Nonnull V newData, V getData) {
        if (getData != null) return getData;

        Object primaryKey = getPrimaryKey(newData);
        if (primaryKey == null) {
            throw new IllegalArgumentException("Primary key cannot be null for data: " + newData);
        }

        // Check if already exists
        V existing = primaryMap.get(primaryKey);
        if (existing != null) return existing;

        // Add to primary map
        primaryMap.put(primaryKey, newData);

        // Add to all secondary indexes
        for (Map.Entry<String, Function<V, Object>> entry : indexExtractors.entrySet()) {
            String indexName = entry.getKey();
            Function<V, Object> extractor = entry.getValue();
            Object indexValue = extractor.apply(newData);
            
            if (indexValue != null) {
                secondaryIndexes.get(indexName).put(indexValue, newData);
            }
        }

        return newData;
    }

    /**
     * Add data (convenience method)
     */
    public V addData(@Nonnull V newData) {
        return addData(newData, null);
    }

    /**
     * Get data by primary key - O(1)
     */
    @Nullable
    public V getDataByPrimaryKey(@Nonnull Object primaryKey) {
        return primaryMap.get(primaryKey);
    }

    /**
     * Get data by indexed field - O(1)
     * Super fast lookup for pre-indexed fields
     * 
     * @param indexName Name of the index
     * @param value Value to search for
     * @return Data if found, null otherwise
     */
    @Nullable
    public V getDataByIndex(@Nonnull String indexName, @Nonnull Object value) {
        Map<Object, V> index = secondaryIndexes.get(indexName);
        if (index == null) {
            throw new IllegalArgumentException("Index '" + indexName + "' not found. Available indexes: " + secondaryIndexes.keySet());
        }
        return index.get(value);
    }

    /**
     * Get data by custom condition - O(n) fallback
     * Use only when no suitable index exists
     */
    @Nullable
    public V getData(@Nonnull GetData<V> getData) {
        for (V data : primaryMap.values()) {
            if (getData.getData(data)) {
                return data;
            }
        }
        return null;
    }

    /**
     * Remove data by value
     * Removes from all indexes automatically
     */
    public void removeData(@Nonnull V data) {
        Object primaryKey = getPrimaryKey(data);
        if (primaryKey == null) return;

        // Remove from primary map
        V removed = primaryMap.remove(primaryKey);
        if (removed == null) return;

        // Remove from all secondary indexes
        for (Map.Entry<String, Function<V, Object>> entry : indexExtractors.entrySet()) {
            String indexName = entry.getKey();
            Function<V, Object> extractor = entry.getValue();
            Object indexValue = extractor.apply(removed);
            
            if (indexValue != null) {
                secondaryIndexes.get(indexName).remove(indexValue);
            }
        }
    }

    /**
     * Remove data by primary key
     */
    @Nullable
    public V removeDataByPrimaryKey(@Nonnull Object primaryKey) {
        V data = primaryMap.get(primaryKey);
        if (data != null) {
            removeData(data);
        }
        return data;
    }

    /**
     * Remove data by indexed field
     */
    @Nullable
    public V removeDataByIndex(@Nonnull String indexName, @Nonnull Object value) {
        V data = getDataByIndex(indexName, value);
        if (data != null) {
            removeData(data);
        }
        return data;
    }

    /**
     * Update data (handles index updates automatically)
     */
    public void updateData(@Nonnull V oldData, @Nonnull V newData) {
        removeData(oldData);
        addData(newData);
    }

    /**
     * Check if data exists by primary key
     */
    public boolean containsByPrimaryKey(@Nonnull Object primaryKey) {
        return primaryMap.containsKey(primaryKey);
    }

    /**
     * Check if data exists by indexed field
     */
    public boolean containsByIndex(@Nonnull String indexName, @Nonnull Object value) {
        Map<Object, V> index = secondaryIndexes.get(indexName);
        return index != null && index.containsKey(value);
    }

    /**
     * Get all data
     */
    @Nonnull
    public Collection<V> getAllData() {
        return new ArrayList<>(primaryMap.values());
    }

    /**
     * Get data count
     */
    public int size() {
        return primaryMap.size();
    }

    /**
     * Check if empty
     */
    public boolean isEmpty() {
        return primaryMap.isEmpty();
    }

    /**
     * Clear all data and indexes
     */
    public void clear() {
        primaryMap.clear();
        for (Map<Object, V> index : secondaryIndexes.values()) {
            index.clear();
        }
    }

    /**
     * Get available index names
     */
    @Nonnull
    public Set<String> getAvailableIndexes() {
        return new HashSet<>(secondaryIndexes.keySet());
    }

    /**
     * Interface for custom search conditions
     */
    public interface GetData<V> {
        boolean getData(@Nonnull V data);
    }

    /**
     * Interface for adding data
     */
    public interface AddData<V> {
        V addData();
    }
}
