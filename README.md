# Plugin Spigot Tools

[![Bukkit Version](https://img.shields.io/badge/bukkit-1.20.4-dark_green.svg)](https://shields.io/)
[![Java](https://img.shields.io/badge/java-8-dark_green.svg)](https://shields.io/)

**PluginSpigotTools** is a library designed to provide cross-version support for Minecraft Bukkit plugins, with many extra utilities to help developers design plugins more easily and efficiently.

---

## Features

- **Cross-version support** for Minecraft Bukkit/Spigot plugins.
- **GUI Manager**: Easily create and manage custom GUIs with YAML configuration.
- **NBT API integration**: Manipulate item NBT data safely.
- **PlaceholderAPI support**: Use placeholders in GUI titles, item names, and lores.
- **Vault integration**: Economy and permission utilities.
- **Thread utilities**: Run async tasks with ease.
- **Item utilities**: Advanced item comparison, creation, and manipulation.
- **Command utilities**: Simplified command registration and handling.
- **File utilities**: Easy file and folder management.
- **Math utilities**: Evaluate string expressions.
- **And more!**

---

## Requirements

- Java 8 or higher
- Spigot or Paper server (1.8+ recommended)
- [XSeries](https://github.com/CryptoMorin/XSeries) (included as dependency)
- [Vault](https://www.spigotmc.org/resources/vault.34315/) (for economy/permissions, optional)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (for placeholders, optional)
- [ItemsAdder](https://www.spigotmc.org/resources/itemsadder.73355/) (for custom items, optional)
- [Item-NBT-API](https://github.com/tr7zw/Item-NBT-API) (for NBT manipulation, included as dependency)

---

## Installation

### 1. Add Maven Repository and Dependency

Add the following to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.huynhphap100</groupId>
    <artifactId>PluginSpigotTools</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

### 2. Install Dependencies

- If you use XSeries, clone and install it:
  ```sh
  git clone https://github.com/CryptoMorin/XSeries.git
  cd XSeries
  mvn install
  ```

- Run `mvn install` in your project to build and resolve dependencies.

### 3. Add Optional Plugins

- Place `Vault`, `PlaceholderAPI`, `ItemsAdder`, and `Item-NBT-API` jars in your server's `plugins` folder if you need their features.

---

## Usage

### 1. Creating a GUI

Define your GUI in a YAML file (e.g., `guiExample.yml`). You can assign commands to items using the `Commands` field:

```yaml
Title: "&bExample GUI"
Format:
  - "aaaaaaaaa"
  - "abbbbbbba"
  - "abcccccba"
  - "abbbbbbba"
  - "aaaaaaaaa"
Items:
  a:
    Type: "WHITE_STAINED_GLASS_PANE"
    Name: "&7Border"
    Lore:
      - "&fThis is a border."
  b:
    Key: "KEY_B"
    Type: "STONE"
    Name: "&7Stone Button"
    Lore:
      - "&fClick to get a stone."
    Commands:
      - "give %player% stone 1"
      - "say %player% clicked the stone button!"
  c:
    Key: "KEY_C"
    Type: "DIAMOND"
    Name: "&bSpecial Diamond for <player>"
    Lore:
      - "&fClick to get a diamond."
    Commands:
      - "give %player% diamond 1"
      - "say %player% is now rich!"
```

### 2. Implementing a GUI in Java

Create a class extending `GuiManager`:

```java
import me.orineko.pluginspigottools.GuiManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class GuiExample extends GuiManager {
    /**
     * Constructor for GuiExample.
     *
     * @param plugin          the plugin instance.
     * @param file            the file for inventory management.
     * @param inventoryHolder the player keep inventory, set to null if inventory is for everyone.
     */
    public GuiExample(@Nonnull Plugin plugin, @Nonnull FileConfiguration file, @Nullable InventoryHolder inventoryHolder) {
        super(plugin, file, inventoryHolder, null);
    }

    public enum Key {
        KEY_B, KEY_C;

        public ItemManager getItemManager(@Nonnull GuiManager guiManager) {
            return guiManager.getItemManager(name());
        }
    }

    public ItemStack getItemC(@Nonnull Player player){
        ItemManager itemManager = Key.KEY_C.getItemManager(this);
        if(itemManager == null) return new ItemStack(Material.AIR);
        HashMap<String, String> map = new HashMap<>();
        map.put("<player>", player.getName());
        return me.orineko.pluginspigottools.MethodDefault.getItemReplaceValue(itemManager.getItemStack().clone(), map);
    }
}
```

### 3. Loading and Using the GUI

```java
// plugin: your main plugin instance
// file: your loaded FileConfiguration (e.g. from guiExample.yml)
GuiExample gui = new GuiExample(plugin, file, null);
gui.renew(player); // Optionally pass a Player for placeholder replacement
player.openInventory(gui.getInventory());
```

---

## Command System

PluginSpigotTools provides an annotation-based command system for Bukkit/Spigot plugins. You can easily define commands and subcommands with tab completion and permission support.

### 1. Defining a Command

Create a class that extends `CommandManager` and annotate it with `@CommandInfo`:

```java
@CommandInfo(aliases = {"main"}, permissions = {})
public class ExampleCommand extends CommandManager {
    public ExampleCommand(Plugin plugin) {
        super(plugin);
    }

    // /main help
    @CommandSub(length = 1, names = "help")
    public void help(CommandSender sender, String[] args) {
        sender.sendMessage("This is the help message!");
    }

    // /main reload
    @CommandSub(length = 1, names = "reload")
    public void reload(CommandSender sender, String[] args) {
        sender.sendMessage("Plugin reloaded!");
    }

    // /main admin help (requires 'plugin.admin' permission)
    @CommandSub(length = 2, names = {"admin", "help"}, permissions = "plugin.admin")
    public void adminHelp(CommandSender sender, String[] args) {
        sender.sendMessage("This is the admin help message!");
    }

    @Override
    protected String getErrorCommandMessage() {
        return "§cUnknown command!";
    }

    @Override
    public List<String> executeTabCompleter(CommandSender sender, String label, String[] args) {
        return null; // Use default auto tab complete
    }
}
```

### 2. Registering the Command

```java
ExampleCommand command = new ExampleCommand(this);
CommandManager.CommandRegistry.register(false, this, command);
```

### 3. Tab Completion
- Typing `/main <tab>` will automatically suggest all first-level subcommands like `help`, `reload`, `admin`.
- Typing `/main admin <tab>` will suggest the next level, such as `help`.
- The system automatically handles permissions, only suggesting commands the user can access.

### 4. Notes
- `length` is the number of arguments (not including the main command name).
- `names` is an array of subcommand names in order.
- You can declare multiple `@CommandSub` for different command branches.
- If you want to provide custom tab completion, override `executeTabCompleter`.

---

## Utilities

- **MethodDefault**: Static utility methods for color formatting, item creation, math, file management, and more.
- **FileManager**: Simplifies file and folder operations.
- **ItemChecker**: Advanced item comparison and inventory checking.
- **ThreadUtils**: Run async tasks with thread management.
- **VaultManager**: Economy and permission integration.
- **PlaceholderManager**: PlaceholderAPI integration for dynamic text.

---

## Support

- Facebook: [Phap Purple](https://www.facebook.com/PhapPurple)

---

## License

This project is open source. See [LICENSE](LICENSE) for details.
