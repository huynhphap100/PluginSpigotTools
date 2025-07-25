package me.orineko.pluginspigottools.api.nbt;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public class NBTApiSetup {

    private static NBTApiTool nbtApiTool = null;
    private static boolean setup = false;

    @Nullable
    public static NBTApiTool getNbtApiTool() {
        if(!setup && nbtApiTool == null) {
            if(Bukkit.getServer().getPluginManager().isPluginEnabled("NBTAPI")) {
                nbtApiTool = new NBTApiTool();
            }
            setup = true;
        }
        return nbtApiTool;
    }

}
