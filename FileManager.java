package me.orineko.hologramsnametags.tools;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class FileManager extends YamlConfiguration{

    private final String fileName;
    protected File file;
    private final Plugin plugin;
    private final List<File> folderList;

    public FileManager(@Nonnull String fileName, @Nonnull Plugin plugin) {
        super();
        this.fileName = fileName;
        this.plugin = plugin;
        this.folderList = new ArrayList<>();
    }

    @SuppressWarnings("all")
    public FileManager createFolder(@Nonnull String... folderName) {
        StringBuilder path = new StringBuilder(plugin.getDataFolder().toString());
        for (File folder : folderList) path.append("/").append(folder.getName());
        for (String folderString : folderName) {
            path.append("/").append(folderString);
            File folder = new File(path.toString());
            folderList.add(folder);
            if (folder.exists()) continue;
            if (folder.length() == 1) folder.mkdir();
            else folder.mkdirs();
        }
        return this;
    }

    /**
     * Create a file with fileName
     *
     * @return FileManager
     */
    @SuppressWarnings("all")
    public FileManager createFile() {
        if (file == null) this.file = new File(getPathFile());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reload();
        return this;
    }

    /**
     * Copy everything in resource file to the plugin file
     *
     * @return FileManager
     * @throws IllegalArgumentException if the resource path is null, empty, or points to a nonexistent resource
     */
    public FileManager copyDefault() {
        if (file == null) this.file = new File(getPathFile());
        if (!file.exists()) plugin.saveResource(getPathResourceFile(), false);
        reload();
        return this;
    }

    /**
     * Reload the plugin file
     *
     * @return FileManager
     */
    public FileManager reload() {
        if (file == null) this.file = new File(getPathFile());
        if (!file.exists())
            try {
                copyDefault();
            } catch (IllegalArgumentException e){
                createFile();
            }
        reloadWithoutCreateFile();
        return this;
    }

    public FileManager reloadWithoutCreateFile() {
        if(file == null || !file.exists()) return this;
        try {
            load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        InputStream resource = plugin.getResource(file.getName());
        if(resource == null) return this;
        InputStreamReader defConfigStream = new InputStreamReader(resource, StandardCharsets.UTF_8);
        setDefaults(YamlConfiguration.loadConfiguration(defConfigStream));
        return this;
    }

    public FileManager save() {
        try {
            save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public FileManager setAndSave(String path, Object obj){
        this.set(path, obj);
        save();
        return this;
    }

    public String getPathFile() {
        StringBuilder path = new StringBuilder(plugin.getDataFolder().toString());
        for (File folder : folderList) path.append("/").append(folder.getName());
        path.append("/").append(fileName);
        return path.toString();
    }

    public String getPathResourceFile() {
        StringBuilder path = new StringBuilder();
        for (File folder : folderList) path.append(folder.getName()).append("/");
        path.append(fileName);
        return path.toString();
    }

    public String getFileName() {
        return fileName;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Nullable
    public List<File> getFolderList() {
        return folderList;
    }

    @Nullable
    public File getFile() {
        return file;
    }

}