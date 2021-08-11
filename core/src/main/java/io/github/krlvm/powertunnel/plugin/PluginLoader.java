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

import io.github.krlvm.powertunnel.Server;
import io.github.krlvm.powertunnel.configuration.ConfigurationStore;
import io.github.krlvm.powertunnel.sdk.exceptions.PluginLoadException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    private static final String PLUGIN_MANIFEST = "plugin.ini";

    public static void loadPlugins(Server server) throws PluginLoadException {
        File folder = new File("plugins");
        if(!folder.exists()) {
            folder.mkdir();
            return;
        }

        File[] files = folder.listFiles(file -> file.getName().toLowerCase().endsWith(".jar"));
        if(files == null) return;
        for (File file : files) {
            if(file.isDirectory()) continue;
            server.registerPlugin(PluginLoader.loadPlugin(file));
        }
    }

    public static PowerTunnelPlugin loadPlugin(File file) throws PluginLoadException {
        final JarFile jar;
        try {
            jar = new JarFile(file);
        } catch (IOException ex) {
            throw new PluginLoadException(file.getName(), "Failed to read plugin .jar file", ex);
        }

        final JarEntry manifest = jar.getJarEntry(PLUGIN_MANIFEST);
        if (manifest == null)
            throw new PluginLoadException(file.getName(), "Plugin .jar file doesn't have manifest (" + PLUGIN_MANIFEST + ")");

        final ConfigurationStore store = new ConfigurationStore();
        try(final InputStream in = jar.getInputStream(manifest)) {
            store.read(in);
        } catch (IOException ex) {
            throw new PluginLoadException(file.getName(), "Failed to read plugin manifest");
        }

        if (!store.contains(PluginInfoFields.ID) ||
                !store.contains(PluginInfoFields.VERSION) ||
                !store.contains(PluginInfoFields.NAME) ||
                !store.contains(PluginInfoFields.MAIN_CLASS) ||
                !store.contains(PluginInfoFields.TARGET_VERSION)
        ) throw new PluginLoadException(file.getName(), "Incomplete manifest");

        final PluginInfo info = new PluginInfo(
                store.get(PluginInfoFields.ID, null),
                store.get(PluginInfoFields.VERSION, null),
                store.get(PluginInfoFields.NAME, null),
                store.get(PluginInfoFields.DESCRIPTION, null),
                store.get(PluginInfoFields.AUTHOR, null),
                store.get(PluginInfoFields.HOMEPAGE, null),
                store.get(PluginInfoFields.MAIN_CLASS, null),
                store.getInt(PluginInfoFields.TARGET_VERSION, 0)
        );
        try {
            injectPlugin(file);
        } catch (MalformedURLException | ReflectiveOperationException ex) {
            throw new PluginLoadException(file.getName(), "Failed to load plugin .jar file", ex);
        }
        final Class<?> clazz;
        try {
            clazz = Class.forName(info.getMainClass());
        } catch (ClassNotFoundException ex) {
            throw new PluginLoadException(file.getName(), "Can't load plugin main class", ex);
        }
        final Object instance;
        try {
            instance = clazz.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new PluginLoadException(file.getName(), "Can't instantiate plugin main class", ex);
        }
        if(!(instance instanceof PowerTunnelPlugin)) {
            throw new PluginLoadException(file.getName(), "Plugin main class doesn't extend PowerTunnelPlugin");
        }
        final PowerTunnelPlugin plugin = ((PowerTunnelPlugin) instance);
        plugin.attachInfo(info);
        return plugin;
    }

    // https://stackoverflow.com/a/27187663
    private static synchronized void injectPlugin(File jar) throws MalformedURLException, ReflectiveOperationException {
        URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL url = jar.toURI().toURL();
        for (URL it : loader.getURLs()) {
            if (it.equals(url)) return;
        }
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(loader, new Object[]{url});
    }


    static class PluginInfoFields {
        static final String ID = "id";
        static final String VERSION = "version";
        static final String NAME = "name";
        static final String DESCRIPTION = "description";
        static final String AUTHOR = "author";
        static final String HOMEPAGE = "homepage";
        static final String MAIN_CLASS = "mainClass";
        static final String TARGET_VERSION = "targetCoreVersion";
    }
}