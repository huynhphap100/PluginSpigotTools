package me.orineko.pluginspigottools.api.placeholder;

import lombok.NonNull;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaceholderManager {

    public String setPlaceholders(@NonNull Player player, @NonNull String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public List<String> setPlaceholders(@NonNull Player player, @NonNull List<String> text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

}
