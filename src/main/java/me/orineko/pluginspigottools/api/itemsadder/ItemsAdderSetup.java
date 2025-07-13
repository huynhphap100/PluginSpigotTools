package me.orineko.pluginspigottools.api.itemsadder;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public class ItemsAdderSetup {

    private static ItemsAdderManager itemsAdderManager;
    private static boolean setup;

    @Nullable
    public static ItemsAdderManager getItemsAdderManager() {
        if(!setup && itemsAdderManager == null) {
            if(Bukkit.getServer().getPluginManager().isPluginEnabled("ItemsAdder"))
                itemsAdderManager = new ItemsAdderManager();
            setup = true;
        }
        return itemsAdderManager;
    }

}
