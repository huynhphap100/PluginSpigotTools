package me.orineko.pluginspigottools;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public abstract class CommandManager extends BukkitCommand implements CommandExecutor, TabCompleter {

    private final CommandInfo commandInfo;
    private final List<DataCommand> dataCommandList = new ArrayList<>();
    private final List<CommandTabComplete> tabList = new ArrayList<>();
    private final HashMap<CommandTabComplete, List<String>> tabMap = new HashMap<>();
    @Getter
    @Setter
    private CommandMap commandMap;
    private final Set<String> labelTempList = new HashSet<>();

    public CommandManager(@Nonnull Plugin plugin) {
        super(plugin.getName(), "", "/"+plugin.getName(), Collections.emptyList());
        commandInfo = getClass().getAnnotation(CommandInfo.class);
        if (commandInfo == null) {
            throw new IllegalStateException("@CommandInfo annotation is required for " + getClass().getName());
        }
        this.setAliases(Arrays.asList(commandInfo.aliases()));
    }

    public void register() {
        boolean hasSubCommand = false;
        Set<String> firstLevelNames = new HashSet<>();
        for (Method method : getClass().getMethods()) {
            CommandSub annotation = method.getAnnotation(CommandSub.class);
            if (annotation == null) continue;
            hasSubCommand = true;
            int length = annotation.length();
            String[] commands = annotation.command();
            String[] names = annotation.names();
            String[] permissions = annotation.permissions();
            List<String> permList = Stream.concat(Stream.of(commandInfo.permissions()),
                    Stream.of(annotation.permissions())).collect(Collectors.toList());
            boolean justPlayerUseCommand = annotation.justPlayerUseCmd();
            DataCommand dataCommand = new DataCommand(length, method);
            dataCommand.addCommands(commands);
            dataCommand.addNames(names);
            dataCommand.addPermissions(permissions);
            dataCommand.setJustPlayerUseCmd(justPlayerUseCommand);
            if(commands.length > 0)
                labelTempList.addAll(Arrays.asList(commands));
            // Always add tab complete for the first name (first-level subcommand)
            if (names.length > 0 && !firstLevelNames.contains(names[0])) {
                CommandTabComplete ctc = new CommandTabComplete(0, names[0]);
                ctc.addLabels(commands);
                ctc.setPermission(permissions);
                tabList.add(ctc);
                tabMap.put(ctc, dataCommand.getCommandList());
                firstLevelNames.add(names[0]);
            }
            // Add tab complete for deeper levels
            for (int i = 1; i < names.length; i++) {
                CommandTabComplete ctc = new CommandTabComplete(i, names[i]);
                ctc.addLabels(commands);
                String[] arr = Arrays.copyOfRange(names, 0, i);
                ctc.setCmdBefore(arr);
                ctc.setPermission(permissions);
                tabList.add(ctc);
                tabMap.put(ctc, dataCommand.getCommandList());
            }
            dataCommandList.add(dataCommand);
        }
        // Ensure a DataCommand for the main command if no subcommands exist
        if (!hasSubCommand) {
            DataCommand mainCommand = new DataCommand(0, null);
            mainCommand.addCommands(commandInfo.aliases());
            mainCommand.addPermissions(commandInfo.permissions());
            dataCommandList.add(mainCommand);
        }
        List<String> labelList = Arrays.stream(commandInfo.aliases()).collect(Collectors.toList());
        labelList.removeAll(labelTempList);
        dataCommandList.stream().filter(d -> d.getCommandList().isEmpty()).forEach(d -> d.addCommands(labelList));
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, @NonNull String@NonNull [] args) {
        return execute(sender, label, args);
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, @NonNull String@NonNull [] args) {
        return tabComplete(sender, label, args);
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        String errorCommandMessage = getErrorCommandMessage();
        String errorPermissionMessage = getErrorPermissionMessage();
        String justPlayerCanUseMessage = getJustPlayerCanUseMessage();
        if (errorPermissionMessage == null) errorPermissionMessage = errorCommandMessage;
        if (justPlayerCanUseMessage == null) justPlayerCanUseMessage = errorCommandMessage;
        DataCommand dataCommand = getDataCommand(label, args);
        if(sender instanceof Player) {
            Player player = (Player) sender;
            List<String> permList = Collections.emptyList();
            if(dataCommand == null) permList = Arrays.asList(commandInfo.permissions());
            else permList = Stream.concat(Stream.of(commandInfo.permissions()),
                    dataCommand.getPermissionList().stream()).collect(Collectors.toList());
            // If permission list is empty, always allow
            if(!permList.isEmpty() && !player.isOp() && permList.stream().noneMatch(player::hasPermission)) return true;
        }
        if (checkObjectIsNull(dataCommand, sender, errorCommandMessage) || dataCommand == null) return true;
        if (checkObjectIsFalse(getPlayerHasPermission(dataCommand, sender, args), sender, errorPermissionMessage))
            return true;
        if (dataCommand.isJustPlayerUseCmd()) {
            if (checkObjectIsFalse((sender instanceof Player), sender, justPlayerCanUseMessage)
                    || !(sender instanceof Player)) return true;
            Player player = (Player) sender;
            dataCommand.callMethod(this, player, args);
        } else {
            dataCommand.callMethod(this, sender, args);
        }
        return true;
    }

    @Override
    @Nonnull
    public List<String> tabComplete(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) throws IllegalArgumentException {
        List<String> list = executeTabCompleter(sender, label, args);
        if (list == null) {
            String[] cmdArr = label.split(":");
            String cmd = (cmdArr.length > 1) ? cmdArr[1] : label;
            list = tabList.stream().filter(t -> t.getIndice() == args.length - 1)
                    .filter(t ->{
                        if(!(sender instanceof Player)) return true;
                        Player player = (Player) sender;
                        if(player.isOp()) return true;
                        List<String> permList = Stream.concat(Stream.of(commandInfo.permissions()),
                                Stream.of(t.getPermission())).collect(Collectors.toList());
                        return permList.isEmpty() || permList.stream().anyMatch(player::hasPermission);
                    }).filter(t -> tabMap.getOrDefault(t, new ArrayList<>()).stream()
                            .anyMatch(c -> c.equalsIgnoreCase(cmd)))
                    .filter(t -> {
                        if (t.getCmdBefore() == null) return true;
                        for (int i = 0; i < t.getCmdBefore().length; i++)
                            if (!args[i].equalsIgnoreCase(t.getCmdBefore()[i]))
                                return false;
                        return true;
                    }).map(CommandTabComplete::getCmd).collect(Collectors.toList());
        }
        if (list.isEmpty()) return super.tabComplete(sender, label, args);
        // Use LinkedHashSet to remove duplicates and keep order
        list = new ArrayList<>(new LinkedHashSet<>(list));
        list = list.stream()
                .filter(s -> s.toLowerCase().contains(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
        Collections.sort(list);
        return list;
    }

    @Nullable
    public abstract List<String> executeTabCompleter(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args);

    // Change to protected/private as appropriate
    protected List<DataCommand> getDataCommandListInLength(@Nonnull String[] args) {
        return dataCommandList.stream().filter(d -> d.getLength() >= args.length ||
                (!d.getNameList().isEmpty() && args.length > 0 && d.getNameList().get(0).equalsIgnoreCase(args[0]))
        ).collect(Collectors.toList());
    }

    @Nullable
    protected DataCommand getDataCommand(@Nonnull String label, @Nonnull String[] args) {
        List<DataCommand> dataCommandList = getDataCommandListInLength(args);
        if (dataCommandList.isEmpty()) return null;
        String[] labelArr = label.split(":");
        String cmd = (labelArr.length > 1) ? labelArr[1] : label;
        return dataCommandList.stream().filter(d -> {
            if(d.getCommandList().stream().noneMatch(data -> data.equalsIgnoreCase(cmd))) return false;
            if (d.length == 0 && args.length == 0) return true;
            if (args.length <= 0) return false;
            for (int i = 0; i < d.getNameList().size(); i++) {
                if (i >= args.length) return false;
                if (!args[i].equalsIgnoreCase(d.getNameList().get(i))) return false;
            }
            return true;
        }).findAny().orElse(null);
    }

    protected boolean getPlayerHasPermission(@Nonnull DataCommand dataCommand, @Nonnull CommandSender sender, @Nonnull String[] args) {
        if (sender instanceof Player) {
            if (sender.isOp()) return true;
            if (dataCommand.getPermissionList().isEmpty()) return true;
            return dataCommand.getPermissionList().stream().anyMatch(sender::hasPermission);
        }
        return true;
    }

    public void sendMessageSender(@Nonnull CommandSender sender, @Nullable String text) {
        if (text == null || text.isEmpty()) return;
        sender.sendMessage(text);
    }

    protected boolean checkObjectIsNull(Object object, @Nonnull CommandSender sender, @Nonnull String message) {
        if (object != null) return false;
        sender.sendMessage(message);
        return true;
    }

    protected boolean checkObjectIsFalse(Boolean bool, @Nonnull CommandSender sender, @Nonnull String message) {
        if (bool) return false;
        sender.sendMessage(message);
        return true;
    }

    protected boolean checkLackOfLength(@Nonnull String[] args, int length, @Nonnull CommandSender sender, @Nonnull String message) {
        if (args.length >= length) return false;
        sender.sendMessage(message);
        return true;
    }

    @Nullable
    protected Player findPlayerOnline(@Nonnull String player, @Nonnull CommandSender sender, @Nonnull String message) {
        Player p = Bukkit.getOnlinePlayers().stream().filter(pl -> pl.getName().equals(player)).findAny().orElse(null);
        if (p != null) return p;
        sender.sendMessage(message);
        return null;
    }

    protected void sendMessageSenderPlayer(@Nonnull CommandSender sender, @Nonnull Player player, @Nonnull String text) {
        if (!(sender instanceof Player) || !sender.getName().equals(player.getName())) sender.sendMessage(text);
        player.sendMessage(text);
    }

    protected boolean checkEqualArgs(@Nonnull String[] args, int indicate, @Nonnull String... cmdList) {
        if (indicate >= args.length) return false;
        String cmd = args[indicate].toUpperCase();
        return Arrays.stream(cmdList).map(String::toUpperCase).anyMatch((c) -> c.equals(cmd));
    }

    @Nonnull
    protected abstract String getErrorCommandMessage();

    @Nullable
    protected String getErrorPermissionMessage() {
        return null;
    }

    @Nullable
    protected String getJustPlayerCanUseMessage() {
        return null;
    }

    /**
     * Annotation for main command info.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandInfo {
        String[] aliases() default {};
        String[] permissions() default {};
    }

    /**
     * Annotation for subcommand info.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandSub {
        int length();
        String[] command() default {};
        String[] names() default {};
        String[] permissions() default {};
        boolean justPlayerUseCmd() default false;
    }

    /**
     * Represents a command or subcommand, its metadata, and method to invoke.
     */
    @Getter
    public static class DataCommand {
        private final int length;
        private final List<String> commandList;
        private final List<String> nameList;
        private final List<String> permissionList;
        private final Method method;
        @Setter
        private boolean justPlayerUseCmd;

        public DataCommand(int length, @Nullable Method method) {
            this.length = length;
            this.method = method;
            this.commandList = new ArrayList<>();
            this.nameList = new ArrayList<>();
            this.permissionList = new ArrayList<>();
        }

        public void addCommands(String... commands) {
            commandList.addAll(Arrays.stream(commands).collect(Collectors.toList()));
        }

        public void addCommands(List<String> commands) {
            commandList.addAll(commands);
        }

        public void addNames(String... names) {
            nameList.addAll(Arrays.stream(names).collect(Collectors.toList()));
        }

        public void addPermissions(String... permissions) {
            permissionList.addAll(Arrays.stream(permissions).collect(Collectors.toList()));
        }

        public void callMethod(@Nonnull CommandManager commandManager, @Nonnull CommandSender sender, @Nonnull String[] args) {
            if (method == null) return;
            try {
                method.invoke(commandManager, sender, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        public void callMethod(@Nonnull CommandManager commandManager, @Nonnull Player player, @Nonnull String[] args) {
            if (method == null) return;
            try {
                method.invoke(commandManager, player, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Represents tab completion info for a command argument.
     */
    @Getter
    public static class CommandTabComplete {
        private final int indice;
        private final List<String> labelList;
        private final String cmd;
        @Setter
        private String[] cmdBefore;
        @Setter
        private String[] permission;

        public CommandTabComplete(int indice, @Nonnull String cmd) {
            this.indice = indice;
            this.labelList = new ArrayList<>();
            this.cmd = cmd;
        }

        public void addLabels(String... labels) {
            labelList.addAll(Arrays.stream(labels).collect(Collectors.toList()));
        }
    }

    public static class CommandRegistry {

        public static final List<CommandManager> commandManagerList = new ArrayList<>();

        public static void register(boolean useFilePlugin, JavaPlugin plugin, @Nonnull CommandManager... commandManagers) {
            if(useFilePlugin) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    for (CommandManager cmd : commandManagers) {
                        List<String> list = cmd.getAliases();
                        list.add(cmd.getName());
                        cmd.register();
                        list.forEach(l -> {
                            PluginCommand command = plugin.getCommand(l);
                            if(command == null) return;
                            command.setExecutor(cmd);
                            command.setTabCompleter(cmd);
                        });
                    }
                });
            } else {
                SimpleCommandMap scm = getSimpleCommandMap();
                if (scm == null) return;
                for (CommandManager cmd : commandManagers) {
                    cmd.register();
                    //cmd.register(scm);
                    scm.register(cmd.getName(), cmd);
                    //cmd.setCommandMap(scm);
                    commandManagerList.add(cmd);
                }
            }
        }

        public static void unregister(boolean useFilePlugin) {
            if(useFilePlugin) return;
            SimpleCommandMap scm = getSimpleCommandMap();
            if (scm == null) return;
            Object map = getPrivateField(scm);
            if (map == null) return;
            @SuppressWarnings("unchecked")
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            for (CommandManager commandManager : commandManagerList) {
                knownCommands.remove(commandManager.getName());
                commandManager.getAliases().forEach(a -> {
                    if (knownCommands.containsKey(a) && knownCommands.get(a).toString().contains(commandManager.getName()))
                        knownCommands.remove(a);
                });
            }
            commandManagerList.clear();

        }

        @Nullable
        private static SimpleCommandMap getSimpleCommandMap() {
            try {
                String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
                Class<?> craftServerClass = Class.forName("org.bukkit.craftbukkit." + version + ".CraftServer");
                Object craftServer = craftServerClass.cast(Bukkit.getServer());
                Object simpleCommandMap = craftServer.getClass().getMethod("getCommandMap").invoke(craftServer);
                return ((SimpleCommandMap) simpleCommandMap);
            } catch (IllegalAccessException | ClassNotFoundException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @Nullable
        private static Object getPrivateField(Object object) {
            try {
                Class<?> clazz = object.getClass();
                Field objectField = clazz.getDeclaredField("knownCommands");
                objectField.setAccessible(true);
                Object result = objectField.get(object);
                objectField.setAccessible(false);
                return result;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
