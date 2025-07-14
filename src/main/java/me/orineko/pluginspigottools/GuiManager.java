package me.orineko.pluginspigottools;

import lombok.Getter;
import lombok.NonNull;
import me.orineko.pluginspigottools.api.placeholder.PlaceholderManager;
import me.orineko.pluginspigottools.api.placeholder.PlaceholderSetup;
import me.orineko.pluginspigottools.scheduler.SchedulerProvider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class for managing GUI inventories in the plugin.
 * Handles inventory creation, item placement, and item management logic.
 * Extend this class to implement custom GUI behaviors.
 *
 * @author OriNeko
 * @version 1.0
 * @see ItemManager
 */
@Getter
public abstract class GuiManager {

    /**
     * The plugin instance for scheduling tasks and command execution.
     */
    protected final Plugin plugin;
    /**
     * The configuration file used to define the GUI layout and items.
     */
    protected final FileConfiguration file;
    /**
     * The list of ItemManager objects representing items in the GUI.
     */
    protected final List<ItemManager> itemManagerList;
    /**
     * The Bukkit inventory instance managed by this GUI.
     */
    protected Inventory inventory;

    /**
     * Constructor for GuiManager with only a configuration file.
     *
     * @param plugin the plugin instance.
     * @param file   the configuration file for inventory management.
     */
    public GuiManager(@Nonnull Plugin plugin, @Nonnull FileConfiguration file) {
        this(plugin, file, null, null);
    }

    /**
     * Constructor for GuiManager with configuration file, inventory holder, and player.
     *
     * @param plugin          the plugin instance.
     * @param file            the configuration file for inventory management.
     * @param inventoryHolder the inventory holder (null if inventory is for everyone).
     * @param player          the player for placeholder replacement in the title (can be null).
     */
    @SuppressWarnings("deprecation")
    public GuiManager(@Nonnull Plugin plugin, @Nonnull FileConfiguration file, @Nullable InventoryHolder inventoryHolder, @Nullable Player player) {
        this.plugin = plugin;
        this.file = file;
        String title = MethodDefault.formatColor(file.getString("Title", ""));
        int line = Math.min(file.getStringList("Format").size(), 6);
        if (player != null) {
            PlaceholderManager pm = PlaceholderSetup.getPlaceholderManager();
            if (pm != null)
                this.inventory = Bukkit.createInventory(inventoryHolder, line * 9, pm.setPlaceholders(player, title));
        }
        if (this.inventory == null) this.inventory = Bukkit.createInventory(inventoryHolder, line * 9, title);
        this.itemManagerList = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Renew the inventory using the default (null) player.
     * Equivalent to calling {@link #renew(Player)} with null.
     *
     * @return true if the inventory was refreshed successfully, false otherwise.
     */
    public boolean renew() {
        return renew(null);
    }

    /**
     * <pre>
     * Renew inventory.
     * Put items from file back into inventory.
     * </pre>
     *
     * @param player the player for placeholder replacement in item names/lore (can be null).
     * @return true if the refresh is complete, otherwise false.
     */
    public boolean renew(@Nullable Player player) {
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
            String key = sectionItem.getString(c + ".Key");
            List<String> commandList = sectionItem.getStringList(c + ".Commands");
            ItemStack itemStack = MethodDefault.getItemStackByFile(this.file, "Items." + c);
            ItemManager itemManager = new ItemManager(this.plugin, c, key, itemStack, commandList);
            this.itemManagerList.add(itemManager);
        });
        for (int i = 0; i < formatString.length(); i++) {
            ItemManager itemManager = getItemManager(formatString.charAt(i));
            if (itemManager == null) continue;
            itemManager.getSlotList().add(i);
            ItemStack itemStack = itemManager.getItemStack();
            if (player != null) MethodDefault.modifyItemPlaceholder(itemStack, player);
            itemStack = MethodDefault.getItemForGui(itemStack);
            this.inventory.setItem(i, itemStack);
        }
        return true;
    }

    /**
     * Get all items whose key is not null.
     *
     * @return The list of {@link ItemManager} with non-null keys.
     */
    public List<ItemManager> getItemManagerListHasKey() {
        return itemManagerList.stream().filter(i -> i.getKey() != null).collect(Collectors.toList());
    }

    /**
     * Find the {@link ItemManager} whose character matches the given character.
     *
     * @param character the character to find.
     * @return The {@link ItemManager} with the specified character, or null if not found.
     */
    @Nullable
    public ItemManager getItemManager(char character) {
        return itemManagerList.stream().filter(i -> i.getCharacter() == character).findAny().orElse(null);
    }

    /**
     * Find the {@link ItemManager} whose key matches the given key (case-insensitive).
     *
     * @param key the key to find.
     * @return The {@link ItemManager} with the specified key, or null if not found.
     */
    @Nullable
    public ItemManager getItemManager(@Nonnull String key) {
        if (key.isEmpty()) return null;
        return itemManagerList.stream().filter(i -> i.getKey() != null)
                .filter(i -> i.getKey().equalsIgnoreCase(key)).findAny().orElse(null);
    }

    /**
     * Find the {@link ItemManager} that contains the specified slot.
     *
     * @param slot the slot to find.
     * @return The {@link ItemManager} containing the slot, or null if not found.
     */
    @Nullable
    public ItemManager getItemManager(int slot) {
        return itemManagerList.stream().filter(i -> i.getSlotList().contains(slot)).findAny().orElse(null);
    }

    /**
     * Set all slots managed by the given {@link ItemManager} to the specified item.
     *
     * @param itemManager the {@link ItemManager} whose slots will be set.
     * @param item        the item to set in all slots.
     */
    public void convertAllItemManager(ItemManager itemManager, ItemStack item) {
        itemManager.getSlotList().forEach(i -> inventory.setItem(i, item));
    }

    /**
     * Represents an item in the GUI, including its character, key, slots, item stack, and commands.
     */
    @Getter
    public static class ItemManager {

        /**
         * The plugin instance for scheduling tasks and command execution (injected from GuiManager).
         */
        private final Plugin plugin;
        /**
         * The character representing this item in the format string.
         */
        private final char character;
        /**
         * The key for this item (can be null).
         */
        @Nullable
        private final String key;
        /**
         * The list of slot indices where this item appears.
         */
        private final List<Integer> slotList;
        /**
         * The Bukkit ItemStack for this item.
         */
        private final ItemStack itemStack;
        /**
         * The list of commands associated with this item.
         */
        private final List<String> commandList;

        /**
         * Constructor for ItemManager.
         *
         * @param plugin      the plugin instance for scheduling tasks.
         * @param character   the character representing this item.
         * @param key         the key for this item (can be null).
         * @param itemStack   the Bukkit ItemStack for this item.
         * @param commandList the list of commands associated with this item.
         */
        public ItemManager(@Nonnull Plugin plugin, char character, @Nullable String key, @Nonnull ItemStack itemStack, @NonNull List<String> commandList) {
            this.plugin = plugin;
            this.character = character;
            this.key = key == null ? null : key.toUpperCase();
            this.slotList = new ArrayList<>();
            this.itemStack = itemStack;
            this.commandList = commandList;
        }

        /**
         * Run all commands associated with this item.
         * <p>
         * If a player is provided, replaces placeholders using PlaceholderAPI if available,
         * or replaces <player> with the player's name. If player is null, runs the command as is.
         * All commands are scheduled to run on the main thread using Bukkit's scheduler.
         *
         * @param player the player to use for placeholder replacement (can be null).
         */
        public void runCommand(@Nullable Player player) {
            SchedulerProvider.get(plugin).runTask(() -> {
                for (String cmd : commandList) {
                    String commandToRun = cmd;
                    if (player != null) {
                        PlaceholderManager pm = PlaceholderSetup.getPlaceholderManager();
                        if (pm != null) {
                            commandToRun = pm.setPlaceholders(player, commandToRun);
                        } else {
                            commandToRun = commandToRun.replace("<player>", player.getName());
                        }
                    }
                    final String finalCommand = commandToRun;
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                    Bukkit.dispatchCommand(console, finalCommand);
                }
            });
        }
    }
}
