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

package io.github.krlvm.powertunnel.desktop.managers;

import io.github.krlvm.powertunnel.desktop.types.ConsoleCommand;

import java.util.*;
import java.util.function.Consumer;

public class ConsoleReader {

    private final Map<String, ConsoleCommand> appCommands = new TreeMap<>((String::compareTo));
    private final Map<String, ConsoleCommand> commands = new TreeMap<>(String::compareTo);

    public ConsoleReader() {
        final Thread thread = new Thread(() -> {
            final Scanner scanner = new Scanner(System.in);
            String input;
            while ((input = scanner.nextLine()) != null) {
                lookupCommand(input.trim());
            }
        }, "Console Reader Thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void lookupCommand(String input) {
        if ("help".equalsIgnoreCase(input)) {
            int longestCommand = Math.max(6, getLongestCommand(appCommands.keySet()) + getLongestCommand(commands.keySet()) + 3);
            int longestUsage = Math.max(6, getLongestUsage(appCommands.values()) + getLongestUsage(commands.values()) + 3);

            System.out.println();
            System.out.println("List of available commands:");
            printHelp(appCommands, longestCommand, longestUsage);
            printHelp(commands, longestCommand, longestUsage);
            System.out.println();
        } else {
            final String[] arr = input.split(" ");
            for (Map.Entry<String, ConsoleCommand> entry : appCommands.entrySet()) {
                if (arr[0].toLowerCase().equals(entry.getKey())) {
                    entry.getValue().getHandler().accept(Arrays.copyOfRange(arr, 1, arr.length));
                    return;
                }
            }
            if (arr.length > 1) {
                for (Map.Entry<String, ConsoleCommand> entry : commands.entrySet()) {
                    if ((arr[0] + " " + arr[1]).toLowerCase().equals(entry.getKey())) {
                        entry.getValue().getHandler().accept(Arrays.copyOfRange(arr, 2, arr.length));
                        return;
                    }
                }
            }
            System.err.println(" Unknown command. Type 'help' to view available commands.");
        }
    }

    public void printHelp(Map<String, ConsoleCommand> commands, int longestCommand, int longestUsage) {
        for (Map.Entry<String, ConsoleCommand> entry : commands.entrySet()) {
            final String cmd = entry.getKey() + String.join("",
                    Collections.nCopies(Math.max(0, longestCommand - entry.getKey().length()), " "));
            final String usage = entry.getValue().getUsage() + String.join("",
                    Collections.nCopies(Math.max(0, longestUsage - entry.getValue().getUsage().length()), " "));
            System.out.println("   " + cmd + usage + entry.getValue().getDescription());
        }
    }

    public void registerAppCommand(
            String command,
            Consumer<String[]> handler,
            String usage,
            String description) {
        appCommands.put(command, new ConsoleCommand("powertunnel", command, usage, description, handler));
    }

    public void registerCommand(
            String pluginId,
            String command,
            Consumer<String[]> handler,
            String usage,
            String description) {
        if (!command.isEmpty()) throw new IllegalArgumentException("Command cannot be empty");
        commands.put(pluginId + " " + command, new ConsoleCommand(pluginId, command, usage, description, handler));
    }

    public void reset() {
        commands.clear();
        System.gc();
    }

    private int getLongestCommand(Set<String> commands) {
        return commands.stream().max(Comparator.comparingInt(String::length)).orElse("").length();
    }
    private int getLongestUsage(Collection<ConsoleCommand> commands) {
        return commands.stream().map(ConsoleCommand::getUsage).max(Comparator.comparingInt(String::length)).orElse("").length();
    }
}
