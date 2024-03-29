package shx.kimono;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import shx.kimono.libs.FileIO;

import javax.script.ScriptException;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ScriptManager {
    private final Kimono kimono;
    private final HashMap<Class<?>, Listener> scriptListeners;
    private final HashMap<Class<?>, ScriptCommandWrapper> scriptCommands;
    private final Engine engine;
    private SimpleCommandMap reflectedCommandMap;
    private Map<String, Command> knownCommands;

    public ScriptManager(Kimono kimono) {
        this.kimono = kimono;
        this.engine = new Engine();
        scriptListeners = new HashMap<>();
        scriptCommands = new HashMap<>();
    }

    public boolean loadScript(File script) {
        String scriptName = script.getName().substring(0, script.getName().length() - ".java".length());
        String contents = FileIO.readData(script);
        try {
            kimono.getLogger().info("Compiling " + scriptName + "...");
            Script skript = engine.compile(script, contents);

            Object instance = skript.getInstance();
            if (instance instanceof Listener) {
                registerListener(skript);
            }

            if (instance instanceof CommandExecutor) {
                registerCommand(skript);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean loadScript(String name) throws ScriptException {
        Script script = engine.get(name);
        if (script != null) return false;
        File newScript = new File(kimono.getScriptsFolder(), name + ".java");
        if (!newScript.exists()) return false;
        return loadScript(newScript);
    }

    public boolean unloadScript(String name) {
        Script script = engine.get(name);
        if (script == null) return false;
        unloadScript(script);
        engine.removeScript(name);
        return true;
    }

    public boolean reloadScript(String name) throws ScriptException{
        unloadScript(name);
        return loadScript(name);
    }

    public void unloadScript(Script script) {
        try {
            Object instance = script.getInstance();
            if (instance instanceof Listener) {
                unregisterListener(script);
            }

            if (instance instanceof CommandExecutor) {
                unregisterCommand(script);
            }

            script.unload();

            engine.removeScript(script);
        } catch (Exception e) {
            Kimono.getPlugin().getLogger().warning("Script " + script.getCompiledClass() + " does not have an unload method!");
        }
    }

    public void load() {
        try {
            Field cmdMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            cmdMapField.setAccessible(true);
            reflectedCommandMap = (SimpleCommandMap) cmdMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bukkit.getServer().getPluginCommand("skriptjava").setExecutor(new ScriptInternalCommand(this));

        File[] fileScripts = kimono.getScriptsFolder().listFiles((f, n) -> n.toLowerCase().endsWith(".java"));
        if (fileScripts == null) return;
        try {
            List<Script> skripts = engine.compileAll(Arrays.asList(fileScripts));
            for (Script skript : skripts) {
                Object instance = skript.getInstance();
                if (instance instanceof Listener) {
                    registerListener(skript);

                }
                if (instance instanceof CommandExecutor) {
                    registerCommand(skript);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Engine getEngine() {
        return engine;
    }

    public void unload() {
        for (Listener listener : scriptListeners.values()) {
            unregisterListener(listener);
        }
        scriptListeners.clear();
        for (ScriptCommandWrapper cmd : scriptCommands.values()) {
            unregisterCommand(cmd);
        }
        scriptCommands.clear();
        engine.removeAll();
        Bukkit.getServer().getPluginCommand("skriptjava").setExecutor(null);
    }

    private void registerListener(Script skript) {
        Bukkit.getPluginManager().registerEvents((Listener) skript.getInstance(), kimono);
        scriptListeners.put(skript.getCompiledClass(), kimono);
    }

    private void unregisterListener(Script skript) {
        HandlerList.unregisterAll((Listener) skript.getInstance());
        scriptListeners.remove(skript.getCompiledClass());
    }

    private void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    private void unregisterCommand(Script skript) {
        ScriptCommandWrapper cmd = scriptCommands.get(skript.getCompiledClass());
        cmd.setExecutor(null);
        unregisterCommand(cmd);
        scriptCommands.remove(skript.getCompiledClass());
    }

    private void unregisterCommand(ScriptCommandWrapper cmd) {
        if (reflectedCommandMap == null) return;
        try {
            try {
                cmd.unregister(reflectedCommandMap);
            } catch (Exception ignored) {}

            if (knownCommands == null) {
                try {
                    Field knownCmdsField = reflectedCommandMap.getClass().getDeclaredField("knownCommands");
                    knownCmdsField.setAccessible(true);
                    knownCommands = (Map<String, Command>) knownCmdsField.get(reflectedCommandMap);
                } catch (Exception ignored1) {}

                if (knownCommands == null) {
                    try {
                        Field knownCmdsField = reflectedCommandMap.getClass().getField("knownCommands");
                        knownCmdsField.setAccessible(true);
                        knownCommands = (Map<String, Command>) knownCmdsField.get(reflectedCommandMap);
                    } catch (Exception ignored2) {}
                }

                if (knownCommands == null) {
                    try {
                        Method knownCmdsMethod = reflectedCommandMap.getClass().getDeclaredMethod("getKnownCommands");
                        knownCmdsMethod.setAccessible(true);
                        knownCommands = (Map<String, Command>) knownCmdsMethod.invoke(reflectedCommandMap);
                    } catch (Exception ignored3) {}
                }

                if (knownCommands == null) {
                    try {
                        Method knownCmdsMethod = reflectedCommandMap.getClass().getMethod("getKnownCommands");
                        knownCmdsMethod.setAccessible(true);
                        knownCommands = (Map<String, Command>) knownCmdsMethod.invoke(reflectedCommandMap);
                    } catch (Exception ignored4) {}
                }
            }

            // At this point, the knownCommand map should not be null.
            knownCommands.remove(cmd.getName());
            knownCommands.remove("skriptjava:" + cmd.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerCommand(Script instance) {
        if (reflectedCommandMap == null) return;
        try {
            Method getCommand = instance.getCompiledClass().getDeclaredMethod("getCommand");
            getCommand.setAccessible(true);
            if (getCommand.getReturnType() != String.class) throw new ScriptException("getCommand does not return a String");
            String name = (String) getCommand.invoke(instance.getInstance());
            ScriptCommandWrapper cmd = new ScriptCommandWrapper(name, (CommandExecutor) instance.getInstance());
            reflectedCommandMap.register("skriptjava", cmd);
            scriptCommands.put(instance.getCompiledClass(), cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        engine.removeAll();
        File[] fileScripts = kimono.getScriptsFolder().listFiles((f, n) -> n.endsWith(".java"));
        if (fileScripts == null) return;
        try {
            List<Script> skripts = engine.compileAll(Arrays.asList(fileScripts));
            for (Script skript : skripts) {
                Object instance = skript.getInstance();
                if (instance instanceof Listener) {
                    registerListener(skript);
                }

                if (instance instanceof CommandExecutor) {
                    registerCommand(skript);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}