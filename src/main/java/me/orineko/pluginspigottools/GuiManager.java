package me.orineko.pluginspigottools;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gui Manager
 * @author OriNeko
 * @version 1.0
 * @see ItemManager
 **/
@Getter
public abstract class GuiManager {

    protected final FileConfiguration file;
    protected final List<ItemManager> itemManagerList;
    protected Inventory inventory;

    /**
     * <pre>
     * Create a constructor for me.orineko.pluginspigottools.GuiManager.
     * </pre>
     *
     * @param file            the file for inventory management.
     * @param inventoryHolder the player keep inventory, set to null if inventory is for everyone.
     */
    public GuiManager(@Nonnull FileConfiguration file, @Nullable InventoryHolder inventoryHolder) {
        this.file = file;
        String title = MethodDefault.formatColor(file.getString("Title", ""));
        int line = Math.min(file.getStringList("Format").size(), 6);
        this.inventory = Bukkit.createInventory(inventoryHolder, line * 9, title);
        this.itemManagerList = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * <pre>
     * Renew inventory.
     * Put items from file back into inventory.
     * </pre>
     *
     * @return True if the refresh is complete, otherwise it will return false
     */
    public boolean renew() {
        List<String> format = this.file.getStringList("Format");

        this.inventory.clear();
        this.itemManagerList.clear();

        format = format.stream().map(StringBuilder::new).map(s -> {
            if (s.length() > 9) return s.substring(0, 8);
            if (s.length() < 9) for (int i = s.length(); i <= 9; i++) s.append("?");
            return s;
        }).map(Object::toString).collect(Collectors.toList());

        String formatString = String.join("", format);

        ConfigurationSection sectionItem = this.file.getConfigurationSection("Items");
        if (sectionItem == null) return false;
        sectionItem.getKeys(false).forEach(sec -> {
            char c = sec.charAt(0);
            if (!formatString.contains(String.valueOf(c))) return;
            String key = this.file.getString("Items." + c + ".Key");
            ItemStack itemStack = MethodDefault.getItemStackByFile(this.file, "Items." + c);
            ItemManager itemManager = new ItemManager(c, key, itemStack);
            this.itemManagerList.add(itemManager);
        });
        for (int i = 0; i < formatString.length(); i++) {
            ItemManager itemManager = getItemManager(formatString.charAt(i));
            if (itemManager == null) continue;
            itemManager.getSlotList().add(i);
            ItemStack itemStack = itemManager.getItemStack();
            itemStack = MethodDefault.getItemForGui(itemStack);
            this.inventory.setItem(i, itemStack);
        }
        return true;
    }

    /**
     * Get all items whose key is not null.
     *
     * @return The list {@link ItemManager} filtered out.
     */
    public List<ItemManager> getItemManagerListHasKey() {
        return itemManagerList.stream().filter(i -> i.getKey() != null).collect(Collectors.toList());
    }

    /**
     * Find the {@link ItemManager} whose character you need to find.
     *
     * @param character the character to find.
     *
     * @return The list {@link ItemManager} have the character you need to find.
     */
    @Nullable
    public ItemManager getItemManager(char character) {
        return itemManagerList.stream().filter(i -> i.getCharacter() == character).findAny().orElse(null);
    }

    /**
     * Find the {@link ItemManager} whose key you need to find.
     *
     * @param key the key to find.
     *
     * @return The list {@link ItemManager} have the key you need to find.
     */
    @Nullable
    public ItemManager getItemManager(@Nonnull String key) {
        if(key.isEmpty()) return null;
        return itemManagerList.stream().filter(i -> i.getKey() != null)
                .filter(i -> i.getKey().equalsIgnoreCase(key)).findAny().orElse(null);
    }

    /**
     * Find the {@link ItemManager} whose slot you need to find.
     *
     * @param slot the slot to find.
     *
     * @return The list {@link ItemManager} have the slot you need to find.
     */
    @Nullable
    public ItemManager getItemManager(int slot) {
        return itemManagerList.stream().filter(i -> i.getSlotList().contains(slot)).findAny().orElse(null);
    }

    /**
     * Set all {@link ItemManager} slots to an item.
     *
     * @param itemManager the {@link ItemManager} to get list of slots.
     * @param item the item to be converted.
     */
    public void convertAllItemManager(ItemManager itemManager, ItemStack item){
        itemManager.getSlotList().forEach(i -> inventory.setItem(i, item));
    }

    @Getter
    public static class ItemManager {

        private final char character;
        @Nullable
        private final String key;
        private final List<Integer> slotList;
        private final ItemStack itemStack;

        public ItemManager(char character, @Nullable String key, @Nonnull ItemStack itemStack) {
            this.character = character;
            this.key = key == null ? null : key.toUpperCase();
            this.slotList = new ArrayList<>();
            this.itemStack = itemStack;
        }

    }
}
