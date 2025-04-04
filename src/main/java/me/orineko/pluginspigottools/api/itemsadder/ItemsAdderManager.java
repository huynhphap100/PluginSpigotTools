package me.orineko.pluginspigottools.api.itemsadder;

import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class ItemsAdderManager {

    @Nullable
    public ItemStack getItem(@NonNull String id) {
        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object customStack = customStackClass.getMethod("getInstance", String.class).invoke(null, id);
            if(customStack == null) return null;
            return (ItemStack) customStack.getClass().getMethod("getItemStack").invoke(customStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        /*CustomStack customStack = CustomStack.getInstance(id);
        if(customStack == null) return null;
        return customStack.getItemStack();*/
    }

}
