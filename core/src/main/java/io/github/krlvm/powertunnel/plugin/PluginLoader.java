/*
 * This file is part of PowerTunnel.
 *
 * PowerTunnel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerTunnel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.krlvm.powertunnel.plugin;

import io.github.krlvm.powertunnel.BuildConstants;
import io.github.krlvm.powertunnel.PowerTunnel;
import io.github.krlvm.powertunnel.configuration.ConfigurationStore;
import io.github.krlvm.powertunnel.sdk.exceptions.PluginLoadException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    public static final String PLUGINS_DIR = "plugins";
    public static final String PLUGIN_MANIFEST = "plugin.ini";

    private static final PluginInjector DEFAULT_INJECTOR = PluginLoader::injectPlugin;

    public static File getPluginFile(String fileName) {
        return new File(PluginLoader.PLUGINS_DIR, fileName);
    }
    public static File getPluginFile(File parentDir, String fileName) {
        return new File(new File(parentDir, PluginLoader.PLUGINS_DIR), fileName);
    }

    public static File[] enumeratePlugins() {
        return enumeratePlugins(new File(PLUGINS_DIR));
    }
    public static File[] enumeratePlugins(File dir) {
        if(!dir.exists()) {
            dir.mkdir();
        } else {
            final File[] result = dir.listFiles(file -> file.getName().toLowerCase().endsWith(".jar"));
            if(result != null) return result;
        }
        return new File[] {};
    }

    public static void loadPlugins(PowerTunnel server) throws PluginLoadException {
        loadPlugins(server, DEFAULT_INJECTOR);
    }
    public static void loadPlugins(PowerTunnel server, PluginInjector injector) throws PluginLoadException {
        loadPlugins(enumeratePlugins(), server, injector);
    }

    public static void loadPlugins(File[] files, PowerTunnel server) throws PluginLoadException {
        loadPlugins(files, server, DEFAULT_INJECTOR);
    }
    public static void loadPlugins(File[] files, PowerTunnel server, PluginInjector injector) throws PluginLoadException {
        for (File file : files) {
            if(file.isDirectory()) continue;
            server.registerPlugin(PluginLoader.loadPlugin(file, injector));
        }
    }

    public static PluginInfo parsePluginInfo(String fileName, InputStream in) throws IOException, PluginLoadException {
        final ConfigurationStore store = new ConfigurationStore();
        store.read(in);

        if (!store.contains(PluginInfoFields.ID) ||
                !store.contains(PluginInfoFields.VERSION) ||
                !store.contains(PluginInfoFields.VERSION_CODE) ||
                !store.contains(PluginInfoFields.NAME) ||
                !store.contains(PluginInfoFields.MAIN_CLASS) ||
                !store.contains(PluginInfoFields.TARGET_VERSION)
        ) throw new PluginLoadException(fileName, "Incomplete manifest");

        String configurationFiles = store.get(PluginInfoFields.CONFIGURATION_FILES, null);

        return new PluginInfo(
                store.get(PluginInfoFields.ID, null),
                store.get(PluginInfoFields.VERSION, null),
                store.getInt(PluginInfoFields.VERSION_CODE, 1),
                store.get(PluginInfoFields.NAME, null),
                store.get(PluginInfoFields.DESCRIPTION, null),
                store.get(PluginInfoFields.AUTHOR, null),
                store.get(PluginInfoFields.HOMEPAGE, null),
                configurationFiles == null ? new String[0] : configurationFiles.split(", "),
                store.get(PluginInfoFields.MAIN_CLASS, null),
                store.getInt(PluginInfoFields.TARGET_VERSION, 0),
                fileName
        );
    }

    public static PowerTunnelPlugin loadPlugin(File file) throws PluginLoadException {
        return loadPlugin(file, DEFAULT_INJECTOR);
    }

    public static PowerTunnelPlugin loadPlugin(File file, PluginInjector injector) throws PluginLoadException {
        final String jarName = file.getName();

        final PluginInfo info;

        try(JarFile jar = new JarFile(file)) {
            final JarEntry manifest = jar.getJarEntry(PLUGIN_MANIFEST);
            if (manifest == null)
                throw new PluginLoadException(jarName, "Plugin .jar file doesn't have manifest (" + PLUGIN_MANIFEST + ")");
            try(final InputStream in = jar.getInputStream(manifest)) {
                info = parsePluginInfo(file.getName(), in);
            } catch (IOException ex) {
                throw new PluginLoadException(jarName, "Failed to read plugin manifest");
            }
        } catch (IOException ex) {
            throw new PluginLoadException(jarName, "Failed to read plugin .jar file", ex);
        }

        if(info.getTargetSdkVersion() > BuildConstants.SDK)
            throw new PluginLoadException(jarName, "Plugin requires a newer PowerTunnel version to run");

        final Class<?> clazz;
        try {
            clazz = injector.inject(file, info.getMainClass());
        } catch (MalformedURLException ex) {
            throw new PluginLoadException(jarName, "Failed to load plugin .jar file", ex);
        } catch (ClassNotFoundException ex) {
            throw new PluginLoadException(jarName, "Can't load plugin main class", ex);
        //} catch (ReflectiveOperationException ex) {
        //    throw new PluginLoadException(jarName, "Failed to inject plugin .jar file", ex);
        } catch (Exception ex) {
            throw new PluginLoadException(jarName, "Unexpected error: " + ex.getMessage(), ex);
        }
        final Object instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new PluginLoadException(jarName, "Can't instantiate plugin main class", ex);
        }
        //} catch (ReflectiveOperationException ex) {
        //    throw new PluginLoadException(jarName, "Can't instantiate plugin main class", ex);
        //}
        if(!(instance instanceof PowerTunnelPlugin)) {
            throw new PluginLoadException(jarName, "Plugin main class doesn't extend PowerTunnelPlugin");
        }
        final PowerTunnelPlugin plugin = ((PowerTunnelPlugin) instance);
        plugin.attachInfo(info);
        return plugin;
    }

        private static final boolean USE_JAVA_11_LOAD_METHOD = true;
    private static synchronized Class<?> injectPlugin(File jar, String mainClass)
            throws MalformedURLException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException { //, ReflectiveOperationException {
        if(USE_JAVA_11_LOAD_METHOD) {
            return injectJarJava11(jar, mainClass);
        } else {
            injectJar(jar);
            return Class.forName(mainClass);
        }
    }

    // https://stackoverflow.com/a/27187663
    private static synchronized void injectJar(File jar)
            throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException { //, ReflectiveOperationException {
        URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL url = jar.toURI().toURL();
        for (URL it : loader.getURLs()) {
            if (it.equals(url)) return;
        }
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(loader, new Object[]{url});
    }

    private static synchronized Class<?> injectJarJava11(File jar, String mainClass)
            throws MalformedURLException, ClassNotFoundException { //, ReflectiveOperationException {
        final ClassLoader loader = URLClassLoader.newInstance(new URL[]{ jar.toURI().toURL() },
                PluginLoader.class.getClassLoader());
        return Class.forName(mainClass, true, loader);
    }


    static class PluginInfoFields {
        static final String ID = "id";
        static final String VERSION = "version";
        static final String VERSION_CODE = "versionCode";
        static final String NAME = "name";
        static final String DESCRIPTION = "description";
        static final String AUTHOR = "author";
        static final String HOMEPAGE = "homepage";
        static final String CONFIGURATION_FILES = "configurationFiles";
        static final String MAIN_CLASS = "mainClass";
        static final String TARGET_VERSION = "targetSdkVersion";
    }
}