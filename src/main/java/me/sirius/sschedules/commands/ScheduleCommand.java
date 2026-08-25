package me.sirius.sschedules.commands;

import me.sirius.sschedules.SSchedules;
import me.sirius.sschedules.model.Schedule;
import me.sirius.sschedules.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleCommand implements CommandExecutor, TabCompleter {
    
    private final SSchedules plugin;
    
    public ScheduleCommand(SSchedules plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sschedules.admin")) {
            sender.sendMessage(ColorUtil.translate("&#FF5555No tienes permisos para usar este comando"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "list":
                handleList(sender);
                break;
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.translate("&#FF5555Uso: /sschedules info <schedule>"));
                } else {
                    handleInfo(sender, args[1]);
                }
                break;
            case "enable":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.translate("&#FF5555Uso: /sschedules enable <schedule>"));
                } else {
                    handleEnable(sender, args[1], true);
                }
                break;
            case "disable":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.translate("&#FF5555Uso: /sschedules disable <schedule>"));
                } else {
                    handleEnable(sender, args[1], false);
                }
                break;
            case "execute":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.translate("&#FF5555Uso: /sschedules execute <schedule>"));
                } else {
                    handleExecute(sender, args[1]);
                }
                break;
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.translate("&#5555FF&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        sender.sendMessage(ColorUtil.translate("&#55FFFFSSchedules &#FFFFFF- Comandos"));
        sender.sendMessage(ColorUtil.translate(""));
        sender.sendMessage(ColorUtil.translate("&#55FFFF/sschedules reload &#FFFFFF- Recarga el plugin"));
        sender.sendMessage(ColorUtil.translate("&#55FFFF/sschedules list &#FFFFFF- Lista todos los schedules"));
        sender.sendMessage(ColorUtil.translate("&#55FFFF/sschedules info <schedule> &#FFFFFF- Info de un schedule"));
        sender.sendMessage(ColorUtil.translate("&#55FFFF/sschedules enable <schedule> &#FFFFFF- Habilita un schedule"));
        sender.sendMessage(ColorUtil.translate("&#55FFFF/sschedules disable <schedule> &#FFFFFF- Deshabilita un schedule"));
        sender.sendMessage(ColorUtil.translate("&#55FFFF/sschedules execute <schedule> &#FFFFFF- Ejecuta un schedule"));
        sender.sendMessage(ColorUtil.translate("&#55FFFFSirius El Mas Pro :jeje:"));
        sender.sendMessage(ColorUtil.translate("&#5555FF&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }
    
    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getScheduleManager().reloadSchedules();
        sender.sendMessage(ColorUtil.translate("&#55FF55Plugin recargado correctamente"));
    }
    
    private void handleList(CommandSender sender) {
        sender.sendMessage(ColorUtil.translate("&#5555FF&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        sender.sendMessage(ColorUtil.translate("&#55FFFFSchedules Cargados:"));
        sender.sendMessage(ColorUtil.translate(""));
        
        for (Schedule schedule : plugin.getScheduleManager().getSchedules().values()) {
            String status = schedule.isEnabled() ? "&#55FF55✓ Habilitado" : "&#FF5555✘ Deshabilitado";
            String type;
            
            if (schedule.isDelayBased()) {
                type = "Delay: " + schedule.getDelay() + "s";
            } else if (schedule.isContinuous()) {
                type = "Continuo";
            } else if (schedule.isTimeBased()) {
                type = "Time-Based";
            } else {
                type = "Unknown";
            }
            
            sender.sendMessage(ColorUtil.translate("&#FFFF55• &#FFFFFF" + schedule.getName() + " " + status + " &#AAAAAA(" + type + ")"));
        }
        
        sender.sendMessage(ColorUtil.translate(""));
        sender.sendMessage(ColorUtil.translate("&#AAAAAATotal: &#FFFFFF" + plugin.getScheduleManager().getSchedules().size()));
        sender.sendMessage(ColorUtil.translate("&#5555FF&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }
    
    private void handleInfo(CommandSender sender, String scheduleName) {
        Schedule schedule = plugin.getScheduleManager().getSchedule(scheduleName);
        if (schedule == null) {
            sender.sendMessage(ColorUtil.translate("&#FF5555Schedule no encontrado: " + scheduleName));
            return;
        }
        
        sender.sendMessage(ColorUtil.translate("&#5555FF&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        sender.sendMessage(ColorUtil.translate("&#55FFFFInformación de: &#FFFFFF" + schedule.getName()));
        sender.sendMessage(ColorUtil.translate(""));
        sender.sendMessage(ColorUtil.translate("&#FFFFAAEstado: " + (schedule.isEnabled() ? "&#55FF55Habilitado" : "&#FF5555Deshabilitado")));
        
        if (schedule.isDelayBased()) {
            sender.sendMessage(ColorUtil.translate("&#FFFFAATipo: &#FFFFFFDelay-Based"));
            sender.sendMessage(ColorUtil.translate("&#FFFFAADelay: &#FFFFFF" + schedule.getDelay() + " segundos"));
        } else if (schedule.isContinuous()) {
            sender.sendMessage(ColorUtil.translate("&#FFFFAATipo: &#FFFFFFContinuo (sin día específico)"));
            sender.sendMessage(ColorUtil.translate("&#FFFFAAFrames: &#FFFFFF" + schedule.getPerformOn().size()));
        } else if (schedule.isTimeBased()) {
            sender.sendMessage(ColorUtil.translate("&#FFFFAATipo: &#FFFFFFTime-Based (día específico)"));
            sender.sendMessage(ColorUtil.translate("&#FFFFAAFrames: &#FFFFFF" + schedule.getPerformOn().size()));
        }
        
        sender.sendMessage(ColorUtil.translate("&#FFFFAARepeat: &#FFFFFF" + schedule.isRepeat()));
        sender.sendMessage(ColorUtil.translate("&#FFFFAAComandos: &#FFFFFF" + (schedule.getCommands() != null ? schedule.getCommands().size() : 0)));
        
        if (schedule.getMinPlayers() != null) {
            sender.sendMessage(ColorUtil.translate("&#FFFFAAMin Players: &#FFFFFF" + schedule.getMinPlayers()));
        }
        
        if (schedule.getMaxPlayers() != null) {
            sender.sendMessage(ColorUtil.translate("&#FFFFAAMax Players: &#FFFFFF" + schedule.getMaxPlayers()));
        }
        
        sender.sendMessage(ColorUtil.translate("&#5555FF&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }
    
    private void handleEnable(CommandSender sender, String scheduleName, boolean enable) {
        Schedule schedule = plugin.getScheduleManager().getSchedule(scheduleName);
        if (schedule == null) {
            sender.sendMessage(ColorUtil.translate("&#FF5555Schedule no encontrado: " + scheduleName));
            return;
        }
        
        schedule.setEnabled(enable);
        String status = enable ? "&#55FF55habilitado" : "&#FF5555deshabilitado";
        sender.sendMessage(ColorUtil.translate("&#FFFFFFSchedule " + status + ": &#FFFFFF" + scheduleName));
    }
    
    private void handleExecute(CommandSender sender, String scheduleName) {
        Schedule schedule = plugin.getScheduleManager().getSchedule(scheduleName);
        if (schedule == null) {
            sender.sendMessage(ColorUtil.translate("&#FF5555Schedule no encontrado: " + scheduleName));
            return;
        }
        
        if (schedule.getCommands() == null || schedule.getCommands().isEmpty()) {
            sender.sendMessage(ColorUtil.translate("&#FF5555El schedule no tiene comandos para ejecutar"));
            return;
        }
        
        new me.sirius.sschedules.executor.CommandExecutor(plugin).executeCommands(schedule, schedule.getCommands());
        sender.sendMessage(ColorUtil.translate("&#55FF55Schedule ejecutado: &#FFFFFF" + scheduleName));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("sschedules.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("reload", "list", "info", "enable", "disable", "execute")
                .stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("info") || 
                                  args[0].equalsIgnoreCase("enable") || 
                                  args[0].equalsIgnoreCase("disable") || 
                                  args[0].equalsIgnoreCase("execute"))) {
            return plugin.getScheduleManager().getSchedules().keySet()
                .stream()
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}