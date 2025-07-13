package me.orineko.pluginspigottools.api.placeholder;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public class PlaceholderSetup {

    private static PlaceholderManager placeholderManager;
    private static boolean setup;

    @Nullable
    public static PlaceholderManager getPlaceholderManager() {
        if(!setup && placeholderManager == null) {
            if(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                placeholderManager = new PlaceholderManager();
            }
            setup = true;
        }
        return placeholderManager;
    }

}
