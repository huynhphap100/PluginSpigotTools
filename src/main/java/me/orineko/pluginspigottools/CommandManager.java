package me.orineko.pluginspigottools;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

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
public abstract class CommandManager extends Command {

    private final CommandInfo commandInfo;
    private final List<DataCommand> dataCommandList = new ArrayList<>();
    private final List<CommandTabComplete> tabList = new ArrayList<>();
    private CommandMap commandMap;

    public CommandManager(@Nonnull String name) {
        super(name);
        commandInfo = getClass().getAnnotation(CommandInfo.class);
        this.setAliases(Arrays.asList(commandInfo.aliases()));
    }

    public void register() {
        for (Method method : getClass().getMethods()) {
            CommandSub annotation = method.getAnnotation(CommandSub.class);
            if (annotation == null) continue;
            int length = annotation.length();
            String[] names = annotation.names();
            String[] permissions = annotation.permissions();
            String[] permissionsMain = commandInfo.permissions();
            List<String> permList = Stream.concat(Stream.of(commandInfo.permissions()),
                    Stream.of(annotation.permissions())).collect(Collectors.toList());
            boolean justPlayerUseCommand = annotation.justPlayerUseCmd();
            DataCommand dataCommand = new DataCommand(length, method);
            dataCommand.addNames(names);
            dataCommand.addPermissions(permissions);
            dataCommand.setJustPlayerUseCmd(justPlayerUseCommand);
            for (int i = 0; i < names.length; i++){
                CommandTabComplete ctc = new CommandTabComplete(i, names[i]);
                if(i > 0){
                    String[] arr = Arrays.copyOfRange(names, 0, i);
                    ctc.setCmdBefore(arr);
                }
                ctc.setPermission(permissions);
                tabList.add(ctc);
            }
            dataCommandList.add(dataCommand);
        }
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        String errorCommandMessage = getErrorCommandMessage();
        String errorPermissionMessage = getErrorPermissionMessage();
        String justPlayerCanUseMessage = getJustPlayerCanUseMessage();
        if (errorPermissionMessage == null) errorPermissionMessage = errorCommandMessage;
        if (justPlayerCanUseMessage == null) justPlayerCanUseMessage = errorCommandMessage;
        DataCommand dataCommand = getDataCommand(args);
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
        if(list == null){
            list = tabList.stream().filter(t -> t.getIndice() == args.length-1)
                    /*.filter(t -> (t.getPermission() == null) || Arrays.stream(t.getPermission()).anyMatch(p -> {
                        Bukkit.broadcastMessage("tabC 1: "+!(sender instanceof Player));
                        Bukkit.broadcastMessage("tabC 2: "+sender.isOp());
                        Bukkit.broadcastMessage("tabC 3: "+sender.hasPermission(p));
                            return !(sender instanceof Player) || (sender.isOp() || sender.hasPermission(p));
                        }))
                    */.filter(t -> {
                        if(t.getCmdBefore() == null) return true;
                        for(int i = 0; i < t.getCmdBefore().length; i++)
                            if(!args[i].equalsIgnoreCase(t.getCmdBefore()[i]))
                                return false;
                        return true;
                    }).map(CommandTabComplete::getCmd).collect(Collectors.toList());
        }
        if (list.isEmpty()) return super.tabComplete(sender, label, args);
        list = new ArrayList<>(list).stream()
                .filter(s -> s.toLowerCase().contains(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
        Collections.sort(list);
        return new ArrayList<>(new HashSet<>(list));
    }

    @Nullable
    public abstract List<String> executeTabCompleter(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args);

    private List<DataCommand> getDataCommandListInLength(@Nonnull String[] args) {
        return dataCommandList.stream().filter(d -> d.getLength() >= args.length ||
                (d.getNameList().size() > 0 && args.length > 0 && d.getNameList().get(0).equalsIgnoreCase(args[0]))
        ).collect(Collectors.toList());
    }

    @Nullable
    private DataCommand getDataCommand(@Nonnull String[] args) {
        List<DataCommand> dataCommandList = getDataCommandListInLength(args);
        if (dataCommandList.isEmpty()) return null;
        return dataCommandList.stream().filter(d -> {
            if (d.length == 0 && args.length == 0) return true;
            if (args.length <= 0) return false;
            for (int i = 0; i < d.getNameList().size(); i++) {
                if (i >= args.length || i >= d.getNameList().size()) return false;
                if (!args[i].equalsIgnoreCase(d.getNameList().get(i))) return false;
            }
            return true;
        }).findAny().orElse(null);
    }

    private boolean getPlayerHasPermission(@Nonnull DataCommand dataCommand, @Nonnull CommandSender sender, @Nonnull String[] args) {
        if (sender instanceof Player || sender.isOp()) return true;
        return dataCommand.getPermissionList().stream().anyMatch(sender::hasPermission);
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
    protected Player findPlayerOnline(@Nonnull String player, @Nonnull CommandSender sender, @Nonnull String message){
        Player p = Bukkit.getOnlinePlayers().stream().filter(pl -> pl.getName().equals(player)).findAny().orElse(null);
        if(p != null) return p;
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

    public CommandMap getCommandMap() {
        return commandMap;
    }

    public void setCommandMap(CommandMap commandMap) {
        this.commandMap = commandMap;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandInfo {
        String[] aliases() default {};

        String[] permissions() default {};
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandSub {
        int length();

        String[] names() default {};

        String[] permissions() default {};

        boolean justPlayerUseCmd() default false;

    }

    public static class DataCommand {
        private final int length;
        private final List<String> nameList;
        private final List<String> permissionList;
        private final Method method;
        private boolean justPlayerUseCmd;

        public DataCommand(int length, @Nonnull Method method) {
            this.length = length;
            this.method = method;
            this.nameList = new ArrayList<>();
            this.permissionList = new ArrayList<>();
        }

        public int getLength() {
            return length;
        }

        public List<String> getNameList() {
            return nameList;
        }

        public List<String> getPermissionList() {
            return permissionList;
        }

        public boolean isJustPlayerUseCmd() {
            return justPlayerUseCmd;
        }

        public void setJustPlayerUseCmd(boolean justPlayerUseCmd) {
            this.justPlayerUseCmd = justPlayerUseCmd;
        }

        public Method getMethod() {
            return method;
        }

        public void addNames(String... names) {
            nameList.addAll(Arrays.stream(names).collect(Collectors.toList()));
        }

        public void addPermissions(String... permissions) {
            permissionList.addAll(Arrays.stream(permissions).collect(Collectors.toList()));
        }

        public void callMethod(@Nonnull CommandManager commandManager, @Nonnull CommandSender sender, @Nonnull String[] args) {
            try {
                method.invoke(commandManager, sender, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public void callMethod(@Nonnull CommandManager commandManager, @Nonnull Player player, @Nonnull String[] args) {
            try {
                method.invoke(commandManager, player, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static class CommandTabComplete {
        private final int indice;
        private final String cmd;
        private String[] cmdBefore;
        private String[] permission;

        public CommandTabComplete(int indice, String cmd){
            this.indice = indice;
            this.cmd = cmd;
        }

        public int getIndice() {
            return indice;
        }

        public String getCmd() {
            return cmd;
        }

        public String[] getCmdBefore() {
            return cmdBefore;
        }

        public String[] getPermission() {
            return permission;
        }

        public void setCmdBefore(String[] cmdBefore) {
            this.cmdBefore = cmdBefore;
        }

        public void setPermission(String[] permission) {
            this.permission = permission;
        }
    }

    public static class CommandRegistry {

        public static final List<CommandManager> commandManagerList = new ArrayList<>();

        public static void register(@Nonnull CommandManager... commandManagers) {
            SimpleCommandMap scm = getSimpleCommandMap();
            if (scm == null) return;
            for (CommandManager cmd : commandManagers) {
                scm.register(cmd.getName(), cmd);
                cmd.register();
                cmd.setCommandMap(scm);
                commandManagerList.add(cmd);
            }
        }

        public static void unregister() {
            SimpleCommandMap scm = getSimpleCommandMap();
            if (scm == null) return;
            Object map = getPrivateField(scm, "knownCommands");
            if(map == null) return;
            @SuppressWarnings("unchecked")
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            for (CommandManager commandManager : commandManagerList) {
                knownCommands.remove(commandManager.getName());
                commandManager.getAliases().forEach(a -> {
                    if(knownCommands.containsKey(a) && knownCommands.get(a).toString().contains(commandManager.getName()))
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
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Nullable
        private static Object getPrivateField(Object object, String field) {
            try {
                Class<?> clazz = object.getClass();
                Field objectField = clazz.getDeclaredField(field);
                objectField.setAccessible(true);
                Object result = objectField.get(object);
                objectField.setAccessible(false);
                return result;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
