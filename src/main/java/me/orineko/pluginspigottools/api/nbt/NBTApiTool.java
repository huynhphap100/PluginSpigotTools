package me.orineko.pluginspigottools.api.nbt;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class NBTApiTool {

    public synchronized ItemStack removeAllNbtItem(ItemStack itemStack) {
        if(itemStack == null) return null;
        if(itemStack.getType().equals(Material.AIR)) return itemStack;
        ItemStack itemClone = itemStack.clone();
        NBTItem nbtItem = new NBTItem(itemClone);
        Set<String> keySet = new HashSet<>(nbtItem.getKeys());
        keySet.forEach(nbtItem::removeKey);
        return nbtItem.getItem();
    }

    public synchronized ItemStack setNbtItemForGui(ItemStack itemStack) {
        if(itemStack == null) return null;
        if(itemStack.getType().equals(Material.AIR)) return itemStack;
        if(itemStack.getAmount() <= 0) return itemStack;
        if(itemStack.getItemMeta() == null) return itemStack;
        ItemStack itemClone = itemStack.clone();
        NBTItem nbtItem = new NBTItem(itemClone);
        nbtItem.setString("ItemGui", "This is bug inventory!!!!!!");
        return nbtItem.getItem();
    }

}
