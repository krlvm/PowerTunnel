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

package io.github.krlvm.powertunnel.desktop;

import io.github.krlvm.powertunnel.desktop.application.ConsoleApp;
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import io.github.krlvm.powertunnel.desktop.configuration.ServerConfiguration;
import io.github.krlvm.powertunnel.desktop.managers.ApplicationManager;
import io.github.krlvm.powertunnel.desktop.parser.ArgumentParser;
import io.github.krlvm.powertunnel.desktop.system.windows.WindowsProxyHandler;
import io.github.krlvm.powertunnel.desktop.ui.I18N;
import io.github.krlvm.powertunnel.desktop.updater.UpdateNotifier;
import io.github.krlvm.powertunnel.desktop.utilities.SystemUtility;
import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static io.github.krlvm.powertunnel.desktop.parser.ArgumentParser.Arguments;

public class Main {

    public static void main(String[] args) {
        System.out.printf("" +
                        "%s version %s%n" +
                        "%s%n" +
                        "(c) krlvm, 2019-2022%n",
                BuildConstants.NAME, BuildConstants.VERSION, BuildConstants.DESCRIPTION
        );
        System.out.println();
        if (!BuildConstants.IS_RELEASE) {
            System.out.println("WARNING: Running a pre-release version of " + BuildConstants.NAME);
            System.out.println();
        }
        SwingDPI.disableJava9NativeScaling();

        final ArgumentParser.Builder builder = new ArgumentParser.Builder()
                .option(Arguments.HELP, "display help")
                .option(Arguments.VERSION, "print version details")
                .option(Arguments.START, "start proxy server after load")
                .option(Arguments.LOGGING, "enable logging to file")

                .argument(Arguments.IP, "set proxy server IP address")
                .argument(Arguments.PORT, "set proxy server port")

                .argument(Arguments.AUTH_USERNAME, "set proxy authorization username")
                .argument(Arguments.AUTH_PASSWORD, "set proxy authorization password")

                .argument(Arguments.UPSTREAM_PROXY_HOST, "set upstream proxy host")
                .argument(Arguments.UPSTREAM_PROXY_PORT, "set upstream proxy port")

                .argument(Arguments.UPSTREAM_AUTH_USERNAME, "set upstream proxy username")
                .argument(Arguments.UPSTREAM_AUTH_PASSWORD, "set upstream proxy password");
        if (SystemUtility.IS_WINDOWS) {
            builder
                    .option(Arguments.DISABLE_AUTO_PROXY_STARTUP, "disable auto proxy setup")
                    .option(Arguments.AUTO_PROXY_STARTUP_IE, "setup proxy using Internet Explorer");
        }
        if (!SystemUtility.IS_TERMINAL) {
            builder
                    .option(Arguments.CONSOLE, "run application in console mode")
                    .option(Arguments.MINIMIZED, "minimize UI to tray after start")
                    .option(Arguments.DISABLE_TRAY, "disable tray mode")
                    .option(Arguments.DISABLE_NATIVE_SKIN, "disable platform native UI skin")
                    .option(Arguments.DISABLE_UI_SCALING, "disable UI scaling")

                    .argument(Arguments.SET_UI_SCALE_FACTOR, "set UI scale factor")
                    .argument(Arguments.LANGUAGE, "set UI language");
        }
        builder.option(Arguments.DISABLE_UPDATER, "disable Update Notifier");

        final ArgumentParser cli = builder.build();
        if (!cli.parse(args)) {
            System.exit(1);
            return;
        }

        if (cli.has(Arguments.HELP)) {
            cli.printHelp();
            System.exit(0);
            return;
        }
        if (cli.has(Arguments.VERSION)) {
            System.out.printf("[Application] Version: %s, code: %s%n", BuildConstants.VERSION, BuildConstants.VERSION_CODE);
            System.out.printf("[Core] Version: %s, code: %s%n", io.github.krlvm.powertunnel.BuildConstants.VERSION, io.github.krlvm.powertunnel.BuildConstants.VERSION_CODE);
            System.exit(0);
            return;
        }

        if(!cli.has(Arguments.LOGGING)) {
            final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            final Configuration config = ctx.getConfiguration();
            config.getRootLogger().removeAppender("LogFile");
            ctx.updateLoggers();
        }

        final ServerConfiguration configuration = new ServerConfiguration();
        try {
            configuration.read();
        } catch (IOException ex) {
            System.err.println("Failed to read configuration: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
            return;
        }
        for (Map.Entry<String, String> entry : cli.getTemporaryConfiguration().entrySet()) {
            configuration.protect(entry.getKey(), entry.getValue());
        }

        if(BuildConstants.VERSION_CODE != configuration.getInt("version", 0)) {
            try {
                ApplicationManager.extractPlugins();
                configuration.setInt("version", BuildConstants.VERSION_CODE);
                try {
                    configuration.save();
                } catch (IOException ignore) {}
            } catch (IOException ex) {
                System.err.println("Failed to install default plugins: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        if(cli.has(Arguments.DISABLE_UPDATER)) {
            UpdateNotifier.ENABLED = false;
        }

        final boolean consoleMode = cli.has(Arguments.CONSOLE) || SystemUtility.IS_TERMINAL;

        if (cli.has(Arguments.IP)) {
            configuration.protect("ip", cli.get(Arguments.IP));
        }
        if (cli.has(Arguments.PORT)) {
            configuration.protectInt("port", cli.getInt(Arguments.PORT));
        }

        if(cli.has(Arguments.AUTH_USERNAME)) {
            if(cli.has(Arguments.AUTH_PASSWORD)) {
                configuration.protectBoolean("proxy_auth_enabled", true);
                configuration.protect("proxy_auth_username", cli.get(Arguments.AUTH_USERNAME));
                configuration.protect("proxy_auth_password", cli.get(Arguments.AUTH_PASSWORD));
            } else {
                System.err.printf("Missing '%s' option", Arguments.AUTH_PASSWORD);
            }
        }

        if(cli.has(Arguments.UPSTREAM_PROXY_HOST)) {
            if(cli.has(Arguments.UPSTREAM_PROXY_PORT)) {
                configuration.protectBoolean("upstream_proxy_enabled", true);
                configuration.protect("upstream_proxy_host", cli.get(Arguments.UPSTREAM_PROXY_HOST));
                configuration.protect("upstream_proxy_port", cli.get(Arguments.UPSTREAM_PROXY_PORT));
            } else {
                System.err.printf("Missing '%s' option", Arguments.UPSTREAM_PROXY_PORT);
            }
        }
        
        if(cli.has(Arguments.UPSTREAM_AUTH_USERNAME)) {
            if(cli.has(Arguments.UPSTREAM_AUTH_PASSWORD)) {
                configuration.protectBoolean("upstream_proxy_auth_enabled", true);
                configuration.protect("upstream_proxy_auth_username", cli.get(Arguments.UPSTREAM_AUTH_USERNAME));
                configuration.protect("upstream_proxy_auth_password", cli.get(Arguments.UPSTREAM_AUTH_PASSWORD));
            } else {
                System.err.printf("Missing '%s' option", Arguments.UPSTREAM_AUTH_PASSWORD);
            }
        }

        if(consoleMode) {
            new ConsoleApp(configuration).start();
        } else {
            if (cli.has(Arguments.DISABLE_AUTO_PROXY_STARTUP)) {
                configuration.protectBoolean("auto_proxy_setup", false);
            } else if(cli.has(Arguments.AUTO_PROXY_STARTUP_IE)) {
                WindowsProxyHandler.USE_IE = true;
            }
            if(!cli.has(Arguments.DISABLE_NATIVE_SKIN)) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    System.err.println("Failed to set native Look and Feel: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            if(!cli.has(Arguments.DISABLE_UI_SCALING)) {
                if(cli.has(Arguments.SET_UI_SCALE_FACTOR)) {
                    SwingDPI.setScaleFactor(cli.getFloat(Arguments.SET_UI_SCALE_FACTOR, 1));
                    SwingDPI.setScaleApplied(true);
                } else {
                    SwingDPI.applyScalingAutomatically();
                }
            }
            if(!cli.has(Arguments.DISABLE_NATIVE_SKIN)) {
                UIUtility.tweakLook();
            }
            UIUtility.setAWTName();

            I18N.load(cli.has(Arguments.LANGUAGE) ?
                    Locale.forLanguageTag(cli.get(Arguments.LANGUAGE)) :
                    Locale.getDefault()
            );

            final GraphicalApp app = new GraphicalApp(
                    configuration,
                    cli.has(Arguments.MINIMIZED),
                    !cli.has(Arguments.DISABLE_TRAY)
            );
            if(cli.has(Arguments.START)) app.start();
        }

        if(UpdateNotifier.ENABLED && UpdateNotifier.isExpired(configuration)) {
            final Thread updateThread = new Thread(
                    () -> UpdateNotifier.checkAndNotify(BuildConstants.NAME, BuildConstants.REPO, false),
                    "Main App Update Checking Thread"
            );
            updateThread.setDaemon(true);
            updateThread.start();
            configuration.setLong("last_update_check", System.currentTimeMillis());
            try {
                configuration.save();
            } catch (IOException ignore) {}
        }
    }
}
