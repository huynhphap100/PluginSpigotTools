package me.orineko.hologramsnametags.tools;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GuiManager {

    /* ---------------------------Example File-------------------------
Title: "&bRobot gui"
Format:
  - "abcccbcba"
  - "cb123b4bc"
  - "cbbbbb5bc"
  - "cbb6bb7bc"
  - "abbcbbcba"
Items:
  a:
    Type: "WHITE_STAINED_GLASS_PANE"
  b:
    Type: "BLACK_STAINED_GLASS_PANE"
  c:
    Type: "CYAN_STAINED_GLASS_PANE"
  1:
    Key: "SET_NAME"
    Type: "NAME_TAG"
    Name: "&eThay đổi tên của robot"
    Lore:
      - "&fBấm vào để thay đổi tên."
  2:
    Key: "CHEST"
    Type: "HOPPER"
    Name: "&aTuỳ chọn robot đào vào rương."
    Lore:
      - "&fBấm vào đây, xong click chuột phải vào rương"
      - "&fĐể chọn rương đó làm vật chứa."
  3:
    Key: "INFINITY_GUI"
    Type: "ENDER_CHEST"
    Name: "&aTuỳ chọn robot đào vào infinity gui."
    Lore:
      - "&fBấm vào đây là được."
  4:
    Key: "INFO"
    Type: "OAK_SIGN"
    Name: "&aThông tin robot của: <player>."
    Lore:
      - "&fMức năng lượng hiện tại là: <energy>"
  5:
    Key: "ENERGY"
    Type: "AIR"
  6:
    Key: "REMOVE"
    Type: "NETHER_STAR"
    Name: "&aBấm vào để xoá robot."
    Lore:
      - "&fBấm vào đây là được."
  7:
    Key: "USE_ENERGY"
    Type: "GREEN_WOOL"
    Name: "&aBấm vào để nạp năng lượng cho robot."
    Lore:
      - "&fBấm vào đây là được."
      */
    /*--------------------------------Example Class------------------------------------
public class NpcGui extends GuiManager {

    public NpcGui() {
        super(StorageData.getGuiFile(), null);
    }

    public enum Key{
        SET_NAME, CHEST, INFINITY_GUI, INFO, ENERGY, REMOVE, USE_ENERGY;

        public ItemManager getItemManager(@Nonnull GuiManager guiManager){
            return guiManager.getItemManager(name());
        }
    }

    public ItemStack getInfo(@Nonnull Player player, long energy){
        ItemManager itemManager = Key.INFO.getItemManager(this);
        if(itemManager == null) return new ItemStack(Material.AIR);
        HashMap<String, String> map = new HashMap<>();
        map.put("<player>", player.getName());
        map.put("<energy>", String.valueOf(energy));
        return MethodDefault.getItemReplaceValue(itemManager.getItemStack().clone(), map);
    }

}
*/

    protected final FileManager fileManager;
    protected final List<ItemManager> itemManagerList;
    protected Inventory inventory;

    public GuiManager(@Nonnull FileManager fileManager, InventoryHolder inventoryHolder) {
        this.fileManager = fileManager;
        String title = MethodDefault.formatColor(fileManager.getString("Title", ""));
        int line = Math.min(fileManager.getStringList("Format").size(), 6);
        this.inventory = Bukkit.createInventory(inventoryHolder, line * 9, title);
        this.itemManagerList = new ArrayList<>();
    }

    public void renew() {
        String title = getFileManager().getString("Title", "");
        List<String> format = getFileManager().getStringList("Format");
        int line = Math.min(format.size(), 6);

        this.inventory = Bukkit.createInventory(getInventory().getHolder(), line * 9, title);
        this.itemManagerList.clear();

        format = format.stream().map(StringBuilder::new).map(s -> {
            if (s.length() > 9) return s.substring(0, 8);
            if (s.length() < 9) for (int i = s.length(); i <= 9; i++) s.append("?");
            return s;
        }).map(Object::toString).collect(Collectors.toList());

        String formatString = String.join("", format);

        ConfigurationSection sectionItem = getFileManager().getConfigurationSection("Items");
        if (sectionItem == null) return;
        sectionItem.getKeys(false).forEach(sec -> {
            char c = sec.charAt(0);
            if (!formatString.contains(String.valueOf(c))) return;
            String key = this.fileManager.getString("Items." + c + ".Key");
            ItemStack itemStack = MethodDefault.getItemStackByFile(this.fileManager, "Items." + c);
            ItemManager itemManager = new ItemManager(c, key, itemStack);
            this.itemManagerList.add(itemManager);
        });
        for (int i = 0; i < formatString.length(); i++) {
            ItemManager itemManager = getItemManager(formatString.charAt(i));
            if (itemManager == null) continue;
            itemManager.getSlotList().add(i);
            this.inventory.setItem(i, itemManager.getItemStack());
        }
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<ItemManager> getItemManagerList() {
        return itemManagerList;
    }

    public List<ItemManager> getItemManagerListHasKey() {
        return itemManagerList.stream().filter(i -> i.getKey() != null).collect(Collectors.toList());
    }

    @Nullable
    public ItemManager getItemManager(char character) {
        return itemManagerList.stream().filter(i -> i.getCharacter() == character).findAny().orElse(null);
    }

    @Nullable
    public ItemManager getItemManager(String key) {
        return itemManagerList.stream().filter(i -> i.getKey() != null)
                .filter(i -> i.getKey().equalsIgnoreCase(key)).findAny().orElse(null);
    }

    @Nullable
    public ItemManager getItemManager(int slot) {
        return itemManagerList.stream().filter(i -> i.getSlotList().contains(slot)).findAny().orElse(null);
    }

    public static class ItemManager {

        private final char character;
        private final String key;
        private final List<Integer> slotList;
        private final ItemStack itemStack;

        public ItemManager(char character, String key, @Nonnull ItemStack itemStack) {
            this.character = character;
            this.key = key == null ? null : key.toUpperCase();
            this.slotList = new ArrayList<>();
            this.itemStack = itemStack;
        }

        public char getCharacter() {
            return character;
        }

        @Nullable
        public String getKey() {
            return key;
        }

        public List<Integer> getSlotList() {
            return slotList;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }
    }
}
