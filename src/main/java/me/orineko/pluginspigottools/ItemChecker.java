package me.orineko.pluginspigottools;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
public class ItemChecker {

    private final ItemStack itemCheck;
    private boolean checkIsSimilar;
    private boolean ignoreType;
    private boolean ignoreDisplayName;
    private boolean ignoreLore;

    public ItemChecker(@NonNull ItemStack itemCheck, boolean checkIsSimilar,
                       boolean ignoreType, boolean ignoreDisplayName, boolean ignoreLore) {
        this.itemCheck = itemCheck;
        this.checkIsSimilar = checkIsSimilar;
        this.ignoreType = ignoreType;
        this.ignoreDisplayName = ignoreDisplayName;
        this.ignoreLore = ignoreLore;
    }

    public ItemChecker(@NonNull ItemStack itemCheck) {
        this.itemCheck = itemCheck;
        this.checkIsSimilar = false;
        this.ignoreType = false;
        this.ignoreDisplayName = false;
        this.ignoreLore = false;
    }

    /**
     * Get slot and amount remaining
     *
     * @param inventory is inventory to check
     * @param amount    is amount to check
     * @return a map contain slot and amount remaining, null if false
     */
    @Nullable
    public Map<Integer, Integer> checkAmount(@NonNull Inventory inventory, int amount) {
        int total = 0;
        Map<Integer, Integer> remainingItems = new HashMap<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            boolean check = compareItem(itemStack);
            if (!check) continue;
            int currentAmount = itemStack.getAmount();
            total += currentAmount;
            if (total >= amount) {
                remainingItems.put(i, total - amount);
                return remainingItems;
            } else {
                remainingItems.put(i, 0);
            }
        }
        if (total < amount) return null;
        return remainingItems;
    }

    public void takeItem(@NonNull Inventory inventory, @NonNull Map<Integer, Integer> amountSlotIteInvMap) {
        amountSlotIteInvMap.forEach((k, v) -> {
            if(v <= 0) {
                inventory.setItem(k, null);
                return;
            }
            ItemStack itemStack = inventory.getItem(k);
            if(itemStack == null || itemStack.getType().equals(Material.AIR)) return;
            itemStack.setAmount(v);
            inventory.setItem(k, itemStack);
        });
    }

    @SuppressWarnings("deprecation")
    public boolean compareItem(@NonNull ItemStack itemStack) {
        if (isCheckIsSimilar()) return itemCheck.isSimilar(itemStack);
        if (!ignoreType) {
            String type1 = itemCheck.getType().name();
            String type2 = itemStack.getType().name();
            if (getVersionSpigot() <= 13) {
                org.bukkit.material.MaterialData materialData1 = itemCheck.getData();
                org.bukkit.material.MaterialData materialData2 = itemStack.getData();
                if (materialData1 != null && materialData2 != null) {
                    int data1 = itemCheck.getData().getData();
                    int data2 = itemStack.getData().getData();
                    if (data1 != data2) return false;
                }
            }
            if (!type1.equals(type2)) return false;
        }

        ItemMeta itemMetaCheck = itemCheck.getItemMeta();
        ItemMeta itemMetaItemStack = itemStack.getItemMeta();
        if (itemMetaCheck != null && itemMetaItemStack != null) {
            if (!ignoreDisplayName) {
                String name1 = itemMetaCheck.getDisplayName();
                String name2 = itemMetaItemStack.getDisplayName();
                boolean checkNull1 = name1 != null;
                boolean checkNull2 = name2 != null;
                if (!((!checkNull1 && !checkNull2) || (checkNull1 && checkNull2 && name1.equals(name2)))) return false;
            }
            if (!ignoreLore) {
                List<String> lore1 = itemMetaCheck.getLore();
                List<String> lore2 = itemMetaItemStack.getLore();
                boolean checkNull1 = lore1 != null && !lore1.isEmpty();
                boolean checkNull2 = lore2 != null && !lore2.isEmpty();
                if (!((!checkNull1 && !checkNull2) || (checkNull1 && checkNull2 && lore1.equals(lore2)))) return false;
            }
        }
        return true;
    }

    public int getVersionSpigot() {
        return Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
    }

    public static ItemCheckerBuilder builder(@NonNull ItemStack itemOrigin) {
        ItemCheckerBuilder itemInventoryBuilder = new ItemCheckerBuilder();
        itemInventoryBuilder.itemCheck(itemOrigin);
        return itemInventoryBuilder;
    }

}
