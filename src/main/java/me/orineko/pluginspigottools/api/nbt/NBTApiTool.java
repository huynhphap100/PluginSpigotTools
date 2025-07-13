package me.orineko.pluginspigottools.api.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class NBTApiTool {

    public ItemStack removeAllNbtItem(ItemStack itemStack) {
        if(itemStack == null) return null;
        if(itemStack.getType().equals(Material.AIR)) return itemStack;
        NBT.modify(itemStack, nbt -> {
            Set<String> keySet = new HashSet<>(nbt.getKeys());
            keySet.forEach(nbt::removeKey);
        });
        return itemStack;
    }

    public ItemStack setNbtItemGui(ItemStack itemStack) {
        if(itemStack == null) return null;
        if(itemStack.getType().equals(Material.AIR)) return itemStack;
        if(itemStack.getAmount() <= 0) return itemStack;
        if(itemStack.getItemMeta() == null) return itemStack;
        NBT.modify(itemStack, nbt -> {
            nbt.setString("ItemGui", "This is bug inventory!!!!!!");
        });
        return itemStack;
    }

}
