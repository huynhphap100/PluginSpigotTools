package me.orineko.pluginspigottools.api.vault;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class VaultSetup {

    @Nullable
    @Getter
    private static VaultManager vaultManager;

    @Nullable
    public static VaultManager setupVault(){
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        if(plugin != null) vaultManager = new VaultManager();
        return vaultManager;
    }

}
