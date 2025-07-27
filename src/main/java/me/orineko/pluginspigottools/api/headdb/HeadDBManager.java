package me.orineko.pluginspigottools.api.headdb;

import lombok.NonNull;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class HeadDBManager implements Listener {

    private HeadDatabaseAPI headDatabaseAPI;

    public void setup(@NonNull Plugin plugin) {
        try {
            headDatabaseAPI = new HeadDatabaseAPI();
        } catch (Exception ignored) {}
        if(headDatabaseAPI == null) {
            Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler
    public void onHeadDBSetup(DatabaseLoadEvent e) {
        headDatabaseAPI = new HeadDatabaseAPI();
    }

    @Nullable
    public ItemStack getItemHead(@NonNull String id) {
        return headDatabaseAPI.getItemHead(id);
    }

}
