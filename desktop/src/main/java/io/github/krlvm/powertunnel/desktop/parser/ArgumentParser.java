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

package io.github.krlvm.powertunnel.desktop.parser;

import io.github.krlvm.powertunnel.desktop.BuildConstants;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArgumentParser {

    private CommandLine cli;
    private final Options options;

    public ArgumentParser(Options options) {
        this.options = options;
    }

    public boolean parse(final String[] args) {
        final CommandLineParser parser = new DefaultParser();
        try {
            cli = parser.parse(options, args);
            return true;
        } catch (ParseException ex) {
            printHelp();
            return false;
        }
    }

    public void printHelp() {
        new HelpFormatter().printHelp(BuildConstants.NAME, options);
    }

    public boolean hasOption(String key) {
        return cli.hasOption(key);
    }

    public String getOption(String key, String defaultValue) {
        return cli.getOptionValue(key, defaultValue);
    }

    public int getIntOption(String key, int defaultValue) {
        final String value = getOption(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            System.err.printf("Invalid numeric value for option '%s'%n", key);
            System.exit(1);
            return -1;
        }
    }

    public float getFloat(String key, float defaultValue) {
        final String value = getOption(key, String.valueOf(defaultValue));
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            System.err.printf("Invalid float value for option '%s'%n", key);
            System.exit(1);
            return -1;
        }
    }

    public static class Builder {
        private final Options options = new Options();

        public Builder argument(@NotNull String longKey, @NotNull String description) {
            return argument(longKey, description, false);
        }
        public Builder argument(@NotNull String longKey, @NotNull String description, boolean required) {
            return option(longKey, description, required, true);
        }

        public Builder option(@NotNull String longKey, @NotNull String description) {
            return option(longKey, description, false);
        }

        public Builder option(@NotNull String longKey, @NotNull String description, boolean required) {
            return option(longKey, description, required, false);
        }

        public Builder option(@NotNull String longKey, @NotNull String description, boolean required, boolean withArguments) {
            return option(null, longKey, description, required, withArguments);
        }

        public Builder option(
                @Nullable String shortKey,
                @NotNull String longKey,
                @NotNull String description,
                boolean required,
                boolean withArguments
        ) {
            final boolean hasShortKey = shortKey != null;

            final Option option = new Option(shortKey, longKey, withArguments, description);
            option.setRequired(required);

            options.addOption(option);
            return this;
        }

        public ArgumentParser build() {
            final Option option = new Option("cfg", "set preference value (for plugins use ID as prefix)");
            return new ArgumentParser(options);
        }
    }

    public static class Arguments {
        public static final String HELP = "help";
        public static final String VERSION = "version";

        public static final String START = "start";
        public static final String IP = "ip";
        public static final String PORT = "port";

        public static final String DISABLE_AUTO_PROXY_STARTUP = "disable-auto-proxy-setup";
        public static final String AUTO_PROXY_STARTUP_IE = "auto-proxy-setup-ie";

        public static final String CONSOLE = "console";
        public static final String MINIMIZED = "minimized";
        public static final String DISABLE_TRAY = "disable-tray";
        public static final String DISABLE_NATIVE_SKIN = "disable-native-skin";
        public static final String SET_UI_SCALE_FACTOR = "set-ui-scale-factor";
        public static final String DISABLE_UI_SCALING = "disable-ui-scaling";

        public static final String DISABLE_UPDATER = "disable-updater";
    }
}
