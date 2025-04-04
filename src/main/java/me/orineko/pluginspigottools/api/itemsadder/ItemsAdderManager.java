package me.orineko.pluginspigottools.api.itemsadder;

import dev.lone.itemsadder.api.CustomStack;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class ItemsAdderManager {

    @Nullable
    public ItemStack getItem(@NonNull String id) {
        CustomStack customStack = CustomStack.getInstance(id);
        if(customStack == null) return null;
        return customStack.getItemStack();
    }

}
