package me.orineko.pluginspigottools.api.vault;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class VaultSetup {

    private static VaultManager vaultManager;

    @Nullable
    public static VaultManager setupVault(){
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        if(plugin != null) vaultManager = new VaultManager();
        return vaultManager;
    }

    @Nullable
    public static VaultManager getVaultManager() {
        return vaultManager;
    }
}
