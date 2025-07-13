package me.orineko.pluginspigottools.api.vault;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public class VaultSetup {

    private static VaultManager vaultManager;
    private static boolean setup;

    @Nullable
    public static VaultManager getVaultManager() {
        if(!setup && vaultManager != null) {
            if(Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
                vaultManager = new VaultManager();
            }
            setup = true;
        }
        return vaultManager;
    }

}
