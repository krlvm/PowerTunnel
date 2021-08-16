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
import io.github.krlvm.powertunnel.desktop.application.DesktopApp;
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import io.github.krlvm.powertunnel.desktop.configuration.AdvancedConfiguration;
import io.github.krlvm.powertunnel.desktop.configuration.ServerConfiguration;
import io.github.krlvm.powertunnel.desktop.parser.ArgumentParser;
import io.github.krlvm.powertunnel.desktop.types.AutoSetupMode;
import io.github.krlvm.powertunnel.desktop.utilities.SystemUtility;
import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        System.out.printf("" +
                        "%s version %s%n" +
                        "%s%n" +
                        "(c) krlvm, 2019-2021%n",
                BuildConstants.NAME, BuildConstants.VERSION, BuildConstants.DESCRIPTION
        );
        System.out.println();
        if (!BuildConstants.IS_RELEASE) {
            System.out.println("WARNING: Running a pre-release version of " + BuildConstants.NAME);
            System.out.println();
        }

        final ArgumentParser.Builder builder = new ArgumentParser.Builder()
                .option(ArgumentParser.Arguments.HELP, "display help")
                .option(ArgumentParser.Arguments.VERSION, "display version details")
                .option(ArgumentParser.Arguments.START, "start proxy server immediately after load")
                .argument(ArgumentParser.Arguments.IP, "sets proxy server IP address [127.0.0.1]")
                .argument(ArgumentParser.Arguments.PORT, "sets proxy server port [8085]");
        if (SystemUtility.IS_WINDOWS) {
            builder
                    .option(ArgumentParser.Arguments.DISABLE_AUTO_PROXY_STARTUP, "disables auto proxy setup (Windows)")
                    .option(ArgumentParser.Arguments.AUTO_PROXY_STARTUP_IE, "setup proxy using IE instead of native API (Windows)");
        }
        if (!SystemUtility.IS_TERMINAL) {
            builder
                    .option(ArgumentParser.Arguments.CONSOLE, "run application in console mode without user interface")
                    .option(ArgumentParser.Arguments.MINIMIZED, "minimize UI to tray after start")
                    .option(ArgumentParser.Arguments.DISABLE_TRAY, "disables tray mode")
                    .argument(ArgumentParser.Arguments.DISABLE_NATIVE_SKIN, "disables platform native UI skin")
                    .argument(ArgumentParser.Arguments.SET_UI_SCALE_FACTOR, "sets UI scale factor")
                    .argument(ArgumentParser.Arguments.DISABLE_UI_SCALING, "disables UI scaling");
        }
        builder.option(ArgumentParser.Arguments.DISABLE_UPDATER, "disables the Update Notifier");

        final ArgumentParser cli = builder.build();
        if (!cli.parse(args)) {
            System.exit(1);
            return;
        }

        if (cli.has(ArgumentParser.Arguments.HELP)) {
            cli.printHelp();
            System.exit(0);
            return;
        }
        if (cli.has(ArgumentParser.Arguments.VERSION)) {
            System.out.printf("[Application] Version: %s, code: %s%n", BuildConstants.VERSION, BuildConstants.VERSION_CODE);
            System.out.printf("[Core] Version: %s, code: %s%n", io.github.krlvm.powertunnel.BuildConstants.VERSION, io.github.krlvm.powertunnel.BuildConstants.VERSION_CODE);
            System.exit(0);
            return;
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

        final boolean consoleMode = cli.has(ArgumentParser.Arguments.CONSOLE) || SystemUtility.IS_TERMINAL;

        if (cli.has(ArgumentParser.Arguments.IP)) {
            configuration.protect("ip", cli.get(ArgumentParser.Arguments.IP));
        }
        if (cli.has(ArgumentParser.Arguments.PORT)) {
            configuration.protectInt("port", cli.getInt(ArgumentParser.Arguments.PORT));
        }
        if (cli.has(ArgumentParser.Arguments.DISABLE_AUTO_PROXY_STARTUP)) {
            configuration.protect("auto-proxy-setup",
                    cli.has(ArgumentParser.Arguments.DISABLE_AUTO_PROXY_STARTUP) || consoleMode ? "false" :
                            (cli.has(ArgumentParser.Arguments.AUTO_PROXY_STARTUP_IE) ?
                                    AutoSetupMode.IE.name() : AutoSetupMode.NATIVE.name())
            );
        }
        if(consoleMode) {
            new ConsoleApp(configuration);
        } else {
            if(!cli.has(ArgumentParser.Arguments.DISABLE_NATIVE_SKIN)) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    System.err.println("Failed to set native Look and Feel: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            if(!cli.has(ArgumentParser.Arguments.DISABLE_UI_SCALING)) {
                if(cli.has(ArgumentParser.Arguments.SET_UI_SCALE_FACTOR)) {
                    SwingDPI.setScaleFactor(cli.getFloat(ArgumentParser.Arguments.SET_UI_SCALE_FACTOR, 1));
                    SwingDPI.setScaleApplied(true);
                } else {
                    SwingDPI.applyScalingAutomatically();
                }
            }
            if(!cli.has(ArgumentParser.Arguments.DISABLE_NATIVE_SKIN)) {
                UIUtility.tweakLook();
            }

            new GraphicalApp(
                    configuration,
                    cli.has(ArgumentParser.Arguments.START),
                    cli.has(ArgumentParser.Arguments.MINIMIZED),
                    !cli.has(ArgumentParser.Arguments.DISABLE_TRAY)
            );
        }
    }
}
