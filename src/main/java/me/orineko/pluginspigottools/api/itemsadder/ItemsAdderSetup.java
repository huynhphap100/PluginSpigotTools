package me.orineko.pluginspigottools.api.itemsadder;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class ItemsAdderSetup {

    @Nullable
    @Getter
    private static ItemsAdderManager itemsAdderManager;

    @Nullable
    public static ItemsAdderManager setupItemsAdder() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("ItemsAdder");
        if(plugin == null) return null;
        itemsAdderManager = new ItemsAdderManager();
        return itemsAdderManager;
    }

}
