package me.orineko.pluginspigottools.api.headdb;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class HeadDBSetup {

    private static HeadDBManager headDBManager;
    @Getter
    private static boolean setup;

    @Nullable
    public static HeadDBManager getHeadDBManager(@NonNull Plugin plugin) {
        if(!setup && headDBManager == null) {
            if(Bukkit.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                headDBManager = new HeadDBManager();
                headDBManager.setup(plugin);
            }
            setup = true;
        }
        return headDBManager;
    }

}
