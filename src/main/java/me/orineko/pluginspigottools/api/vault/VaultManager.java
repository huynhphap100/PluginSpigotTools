package me.orineko.pluginspigottools.api.vault;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VaultManager {

    private final Economy economy;
    private final Permission permission;
    private final Chat chat;

    public VaultManager(){
        economy = setupEconomy();
        permission = setupPermissions();
        chat = setupChat();
    }

    @Nullable
    public Economy getEconomy() {
        return economy;
    }

    @Nullable
    public Permission getPermission() {
        return permission;
    }

    @Nullable
    public Chat getChat() {
        return chat;
    }

    public boolean customEconomy(@Nonnull Player player, double value){
        Economy economy = getEconomy();
        if(economy == null) return false;
        if(value >= 0){
            return economy.depositPlayer(player, value).transactionSuccess();
        } else {
            return economy.withdrawPlayer(player, -value).transactionSuccess();
        }
    }

    public double getEconomy(@Nonnull Player player){
        return economy.getBalance(player);
    }

    private Economy setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return null;
        return rsp.getProvider();
    }

    private Permission setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if(rsp == null) return null;
        return rsp.getProvider();
    }

    private Chat setupChat() {
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        if(rsp == null) return null;
        return rsp.getProvider();
    }

}
