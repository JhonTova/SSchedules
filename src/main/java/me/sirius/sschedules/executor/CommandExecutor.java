package me.sirius.sschedules.executor;

import me.sirius.core.text.Text;

import me.sirius.sschedules.SSchedules;
import me.sirius.sschedules.model.Schedule;
import me.sirius.sschedules.util.ColorUtil;
import me.sirius.sschedules.util.RandomPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.*;

public class CommandExecutor {

    private final SSchedules plugin;

    public CommandExecutor(SSchedules plugin) {
        this.plugin = plugin;
    }

    public void executeCommands(Schedule schedule, List<String> commands) {
        List<String> commandsToExecute = getCommandsBasedOnMode(schedule, commands);

        executeCommandList(commandsToExecute, schedule, 0);
    }

    private List<String> getCommandsBasedOnMode(Schedule schedule, List<String> commands) {
        if (schedule.isRandomize()) {
            return getRandomCommand(schedule, commands, false);
        } else if (schedule.isDuplicateRandomize()) {
            return getRandomCommand(schedule, commands, true);
        } else if (schedule.isSingleLinear()) {
            return getLinearCommand(schedule, commands);
        }
        return new ArrayList<>(commands);
    }

    private List<String> getRandomCommand(Schedule schedule, List<String> commands, boolean allowDuplicates) {
        if (allowDuplicates) {
            Random random = new Random();
            return Collections.singletonList(commands.get(random.nextInt(commands.size())));
        }

        List<String> used = schedule.getUsedRandomCommands();
        if (used == null) {
            used = new ArrayList<>();
            schedule.setUsedRandomCommands(used);
        }

        List<String> available = new ArrayList<>(commands);
        available.removeAll(used);

        if (available.isEmpty()) {
            used.clear();
            available = new ArrayList<>(commands);
        }

        Random random = new Random();
        String selected = available.get(random.nextInt(available.size()));
        used.add(selected);

        return Collections.singletonList(selected);
    }

    private List<String> getLinearCommand(Schedule schedule, List<String> commands) {
        int index = schedule.getCurrentLinearIndex();
        if (index >= commands.size()) {
            index = 0;
        }

        String command = commands.get(index);
        schedule.setCurrentLinearIndex(index + 1);

        return Collections.singletonList(command);
    }

    private void executeCommandList(List<String> commands, Schedule schedule, int index) {
        if (index >= commands.size()) return;

        String command = commands.get(index);

        if (command.startsWith("delay!")) {
            String[] parts = command.split(" ", 2);
            if (parts.length > 1) {
                try {
                    int delay = Integer.parseInt(translatePlaceholders(parts[1].trim(), null));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        executeCommandList(commands, schedule, index + 1);
                    }, delay * 20L);
                    return;
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Delay inválido en schedule '" + schedule.getName() + "': " + parts[1]);
                }
            }
        } else {
            executeCommand(command, schedule);
        }

        executeCommandList(commands, schedule, index + 1);
    }

    private void executeCommand(String command, Schedule schedule) {
        if (command.contains(";;")) {
            String[] multiCommands = command.split(";;");
            for (String cmd : multiCommands) {
                executeSingleCommand(cmd.trim(), schedule);
            }
        } else {
            executeSingleCommand(command, schedule);
        }
    }

    private void executeSingleCommand(String command, Schedule schedule) {
        if (command.startsWith("msg!")) {
            handleMessageCommand(command);
        } else if (command.startsWith("broadcast!")) {
            handleBroadcastCommand(command);
        } else if (command.startsWith("actionbar!")) {
            handleActionbarCommand(command);
        } else if (command.startsWith("title!")) {
            handleTitleCommand(command);
        } else if (command.startsWith("subtitle!")) {
            handleSubtitleCommand(command);
        } else if (command.startsWith("kickall!")) {
            handleKickallCommand(command);
        } else if (command.startsWith("perm:")) {
            handlePermCommand(command, schedule);
        } else if (command.startsWith("moneycost:")) {
            handleMoneyCostCommand(command, schedule);
        } else if (command.startsWith("expcost:")) {
            handleExpCostCommand(command, schedule);
        } else if (command.startsWith("allPlayers!")) {
            handleAllPlayersCommand(command, schedule);
        } else {
            String processedCommand = schedule.isDontTranslatePlaceholders() ?
                command : translatePlaceholders(command, null);
            processedCommand = processRandomPlayer(processedCommand);

            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                if (schedule.isFeedback()) {
                    plugin.getLogger().info("Ejecutando comando: " + processedCommand);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error ejecutando comando '" + processedCommand + "': " + e.getMessage());
            }
        }
    }

    private void handleMessageCommand(String command) {
        String[] parts = command.substring(4).split(" ", 2);
        if (parts.length < 2) return;

        String playerName = parts[0].trim();
        String message = parts[1].trim();

        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            player.sendMessage(ColorUtil.translate(translatePlaceholders(message, player)));
        }
    }

    private void handleBroadcastCommand(String command) {
        String message = command.substring(10).trim();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ColorUtil.translate(translatePlaceholders(message, player)));
        }
    }

    private void handleActionbarCommand(String command) {
        String message = command.substring(10).trim();
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                Text.actionBar(player, translatePlaceholders(message, player));
            } catch (Exception e) {
                plugin.getLogger().warning("Error enviando actionbar: " + e.getMessage());
            }
        }
    }

    private void handleTitleCommand(String command) {
        String message = command.substring(6).trim();
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.sendTitle(ColorUtil.translate(translatePlaceholders(message, player)), "", 10, 70, 20);
            } catch (Exception e) {
                plugin.getLogger().warning("Error enviando title: " + e.getMessage());
            }
        }
    }

    private void handleSubtitleCommand(String command) {
        String message = command.substring(9).trim();
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.sendTitle("", ColorUtil.translate(translatePlaceholders(message, player)), 10, 70, 20);
            } catch (Exception e) {
                plugin.getLogger().warning("Error enviando subtitle: " + e.getMessage());
            }
        }
    }

    private void handleKickallCommand(String command) {
        String message = command.substring(8).trim();
        String translatedMessage = ColorUtil.translate(message);

        List<Player> playersToKick = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : playersToKick) {
            try {
                player.kickPlayer(translatedMessage);
            } catch (Exception e) {
                plugin.getLogger().warning("Error expulsando jugador " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    private void handlePermCommand(String command, Schedule schedule) {
        int endPerm = command.indexOf("!", 5);
        if (endPerm == -1) return;

        String permission = command.substring(5, endPerm);
        String actualCommand = command.substring(endPerm + 1).trim();

        boolean foundPlayer = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                foundPlayer = true;
                break;
            }
        }

        if (foundPlayer) {
            executeSingleCommand(actualCommand, schedule);
        }
    }

    private void handleMoneyCostCommand(String command, Schedule schedule) {
        int endCost = command.indexOf("!", 10);
        if (endCost == -1) return;

        try {
            double cost = Double.parseDouble(command.substring(10, endCost));
            String actualCommand = command.substring(endCost + 1).trim();

            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                boolean foundPlayer = false;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (hasEnoughMoney(player, cost)) {
                        foundPlayer = true;
                        break;
                    }
                }

                if (foundPlayer) {
                    executeSingleCommand(actualCommand, schedule);
                }
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Costo de dinero inválido en comando");
        }
    }

    private void handleExpCostCommand(String command, Schedule schedule) {
        int endCost = command.indexOf("!", 8);
        if (endCost == -1) return;

        try {
            int cost = Integer.parseInt(command.substring(8, endCost));
            String actualCommand = command.substring(endCost + 1).trim();

            boolean foundPlayer = false;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getTotalExperience() >= cost) {
                    foundPlayer = true;
                    break;
                }
            }

            if (foundPlayer) {
                executeSingleCommand(actualCommand, schedule);
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Costo de exp inválido en comando");
        }
    }

    private void handleAllPlayersCommand(String command, Schedule schedule) {
        String actualCommand = command.substring(11).trim();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String processedCommand = actualCommand.replace("[allPlayers]", player.getName());
            processedCommand = translatePlaceholders(processedCommand, player);

            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            } catch (Exception e) {
                plugin.getLogger().warning("Error ejecutando comando para " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    private String processRandomPlayer(String command) {
        if (command.contains("[randomPlayer]")) {
            List<Player> eligible = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("cmi.scheduler.exclude")) {
                    eligible.add(player);
                }
            }

            if (!eligible.isEmpty()) {
                Random random = new Random();
                Player randomPlayer = eligible.get(random.nextInt(eligible.size()));
                return command.replace("[randomPlayer]", randomPlayer.getName());
            }
        }
        return command;
    }

    private String translatePlaceholders(String text, Player player) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && player != null) {
            try {
                return PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                plugin.getLogger().warning("Error traduciendo placeholders: " + e.getMessage());
            }
        }

        if (text.contains("%rand/")) {
            text = RandomPlaceholder.apply(text, java.util.concurrent.ThreadLocalRandom.current());
        }

        return text;
    }

    private boolean hasEnoughMoney(Player player, double amount) {
        return true;
    }
}