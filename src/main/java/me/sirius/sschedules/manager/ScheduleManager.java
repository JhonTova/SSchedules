package me.sirius.sschedules.manager;

import me.sirius.core.text.Text;

import me.sirius.sschedules.SSchedules;
import me.sirius.sschedules.model.Schedule;
import me.sirius.sschedules.model.TimeFrame;
import me.sirius.sschedules.executor.CommandExecutor;
import me.sirius.sschedules.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduleManager {
    
    private final SSchedules plugin;
    private final Map<String, Schedule> schedules;
    private final Map<String, Long> delayScheduleTimers;
    private final Map<String, Long> timeBasedScheduleExecuted;
    private final Map<String, Long> continuousScheduleTimers;
    private BukkitTask schedulerTask;
    private final CommandExecutor commandExecutor;
    private String excludeTimeStart;
    private String excludeTimeEnd;
    private boolean insufficientPlayersEnabled;
    private String insufficientPlayersType;
    private String insufficientPlayersMessage;
    private String insufficientPlayersActionbarMessage;
    private String insufficientPlayersTitleMain;
    private String insufficientPlayersTitleSub;
    private int insufficientPlayersTitleFadeIn;
    private int insufficientPlayersTitleStay;
    private int insufficientPlayersTitleFadeOut;
    
    public ScheduleManager(SSchedules plugin) {
        this.plugin = plugin;
        this.schedules = new ConcurrentHashMap<>();
        this.delayScheduleTimers = new ConcurrentHashMap<>();
        this.timeBasedScheduleExecuted = new ConcurrentHashMap<>();
        this.continuousScheduleTimers = new ConcurrentHashMap<>();
        this.commandExecutor = new CommandExecutor(plugin);
    }
    
    public void loadSchedules() {
        schedules.clear();
        delayScheduleTimers.clear();
        timeBasedScheduleExecuted.clear();
        continuousScheduleTimers.clear();
        
        String excludeTimes = plugin.getConfig().getString("exclude-times", "");
        if (!excludeTimes.isEmpty() && excludeTimes.contains("-")) {
            String[] parts = excludeTimes.split("-");
            excludeTimeStart = parts[0].trim();
            excludeTimeEnd = parts[1].trim();
        } else {
            excludeTimeStart = null;
            excludeTimeEnd = null;
        }
        
        loadInsufficientPlayersConfig();
        
        ConfigurationSection schedulesSection = plugin.getConfig().getConfigurationSection("schedules");
        if (schedulesSection == null) return;
        
        for (String key : schedulesSection.getKeys(false)) {
            ConfigurationSection section = schedulesSection.getConfigurationSection(key);
            if (section == null) continue;
            
            Schedule schedule = new Schedule(key);
            schedule.setEnabled(section.getBoolean("Enabled", true));
            
            if (section.contains("Delay")) {
                schedule.setDelay(section.getInt("Delay"));
            }
            
            schedule.setRandomize(section.getBoolean("Randomize", false));
            schedule.setDuplicateRandomize(section.getBoolean("DuplicateRandomize", false));
            schedule.setSingleLinear(section.getBoolean("SingleLinear", false));
            schedule.setRepeat(section.getBoolean("Repeat", true));
            schedule.setFeedback(section.getBoolean("Feedback", true));
            schedule.setDontTranslatePlaceholders(section.getBoolean("DontTranslatePlaceholders", false));
            
            if (section.contains("MinPlayers")) {
                schedule.setMinPlayers(section.getInt("MinPlayers"));
            }
            
            if (section.contains("MaxPlayers")) {
                schedule.setMaxPlayers(section.getInt("MaxPlayers"));
            }
            
            if (section.contains("MinPlayersCommands")) {
                schedule.setMinPlayersCommands(section.getStringList("MinPlayersCommands"));
            }
            
            if (section.contains("NotifyInsufficientPlayers")) {
                schedule.setNotifyInsufficientPlayers(section.getBoolean("NotifyInsufficientPlayers"));
            }
            
            if (section.contains("Commands")) {
                schedule.setCommands(section.getStringList("Commands"));
            }
            
            if (section.contains("PerformOn")) {
                ConfigurationSection performOnSection = section.getConfigurationSection("PerformOn");
                if (performOnSection != null) {
                    Map<String, TimeFrame> performOn = new HashMap<>();
                    for (String frameKey : performOnSection.getKeys(false)) {
                        ConfigurationSection frameSection = performOnSection.getConfigurationSection(frameKey);
                        if (frameSection != null) {
                            TimeFrame timeFrame = new TimeFrame();
                            
                            if (frameSection.contains("Month")) {
                                timeFrame.setMonth(frameSection.getInt("Month"));
                            }
                            
                            if (frameSection.contains("Day")) {
                                Object dayValue = frameSection.get("Day");
                                timeFrame.setDay(dayValue);
                            }
                            
                            if (frameSection.contains("Hour")) {
                                timeFrame.setHour(frameSection.getInt("Hour"));
                            }
                            
                            if (frameSection.contains("Minute")) {
                                timeFrame.setMinute(frameSection.getInt("Minute"));
                            }
                            
                            if (frameSection.contains("Second")) {
                                timeFrame.setSecond(frameSection.getInt("Second"));
                            }
                            
                            performOn.put(frameKey, timeFrame);
                        }
                    }
                    schedule.setPerformOn(performOn);
                }
            }
            
            schedules.put(key, schedule);
            
            if (schedule.isEnabled() && schedule.isDelayBased()) {
                delayScheduleTimers.put(key, System.currentTimeMillis());
            }
            
            if (schedule.isEnabled() && schedule.isContinuous()) {
                continuousScheduleTimers.put(key, System.currentTimeMillis());
            }
        }
        
        plugin.getLogger().info("Cargados " + schedules.size() + " schedules");
        if (excludeTimeStart != null && excludeTimeEnd != null) {
            plugin.getLogger().info("Horario de exclusión activo: " + excludeTimeStart + " - " + excludeTimeEnd);
        }
        if (insufficientPlayersEnabled) {
            plugin.getLogger().info("Sistema de notificación de jugadores insuficientes: ACTIVADO");
        }
    }
    
    private void loadInsufficientPlayersConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("insufficient-players-message");
        if (config != null) {
            insufficientPlayersEnabled = config.getBoolean("enabled", true);
            insufficientPlayersType = config.getString("type", "broadcast").toLowerCase();
            insufficientPlayersMessage = config.getString("message", 
                "&c&l⚠ &fEl evento &e{schedule} &fno se activó por falta de jugadores &8(&c{current}&7/&a{required}&8)");
            insufficientPlayersActionbarMessage = config.getString("actionbar-message", 
                "&c⚠ {schedule} &7cancelado &8• &c{current}&7/&a{required} jugadores");
            insufficientPlayersTitleMain = config.getString("title-main", 
                "&c&l⚠ EVENTO CANCELADO ⚠");
            insufficientPlayersTitleSub = config.getString("title-sub", 
                "&f{schedule} &8• &c{current}&7/&a{required} jugadores");
            insufficientPlayersTitleFadeIn = config.getInt("title-fade-in", 10);
            insufficientPlayersTitleStay = config.getInt("title-stay", 70);
            insufficientPlayersTitleFadeOut = config.getInt("title-fade-out", 20);
        } else {
            insufficientPlayersEnabled = true;
            insufficientPlayersType = "broadcast";
            insufficientPlayersMessage = "&c&l⚠ &fEl evento &e{schedule} &fno se activó por falta de jugadores &8(&c{current}&7/&a{required}&8)";
            insufficientPlayersActionbarMessage = "&c⚠ {schedule} &7cancelado &8• &c{current}&7/&a{required} jugadores";
            insufficientPlayersTitleMain = "&c&l⚠ EVENTO CANCELADO ⚠";
            insufficientPlayersTitleSub = "&f{schedule} &8• &c{current}&7/&a{required} jugadores";
            insufficientPlayersTitleFadeIn = 10;
            insufficientPlayersTitleStay = 70;
            insufficientPlayersTitleFadeOut = 20;
        }
    }
    
    public void startScheduler() {
        schedulerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Calendar calendar = Calendar.getInstance(plugin.getTimeZone());
            
            for (Schedule schedule : schedules.values()) {
                if (!schedule.isEnabled()) continue;
                
                if (schedule.isDelayBased()) {
                    handleDelaySchedule(schedule);
                } else if (schedule.isContinuous()) {
                    handleContinuousSchedule(schedule, calendar);
                } else if (schedule.isTimeBased()) {
                    handleTimeBasedSchedule(schedule, calendar);
                }
            }
        }, 20L, 20L);
    }
    
    public void stopScheduler() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
        }
    }
    
    private void handleDelaySchedule(Schedule schedule) {
        Long lastExecution = delayScheduleTimers.get(schedule.getName());
        if (lastExecution == null) return;
        
        long currentTime = System.currentTimeMillis();
        long elapsed = (currentTime - lastExecution) / 1000;
        
        if (elapsed >= schedule.getDelay()) {
            executeSchedule(schedule);
            
            if (schedule.isRepeat()) {
                delayScheduleTimers.put(schedule.getName(), currentTime);
            } else {
                delayScheduleTimers.remove(schedule.getName());
                schedule.setEnabled(false);
            }
        }
    }
    
    private void handleContinuousSchedule(Schedule schedule, Calendar calendar) {
        if (isInExcludedTime(calendar)) {
            return;
        }
        
        Long lastExecution = continuousScheduleTimers.get(schedule.getName());
        if (lastExecution == null) {
            continuousScheduleTimers.put(schedule.getName(), System.currentTimeMillis());
            return;
        }
        
        TimeFrame timeFrame = schedule.getPerformOn().values().iterator().next();
        int intervalSeconds = calculateIntervalSeconds(timeFrame);
        
        long currentTime = System.currentTimeMillis();
        long elapsed = (currentTime - lastExecution) / 1000;
        
        if (elapsed >= intervalSeconds) {
            executeSchedule(schedule);
            
            if (schedule.isRepeat()) {
                continuousScheduleTimers.put(schedule.getName(), currentTime);
            } else {
                continuousScheduleTimers.remove(schedule.getName());
                schedule.setEnabled(false);
            }
        }
    }
    
    private void handleTimeBasedSchedule(Schedule schedule, Calendar calendar) {
        for (Map.Entry<String, TimeFrame> entry : schedule.getPerformOn().entrySet()) {
            TimeFrame timeFrame = entry.getValue();
            
            if (!matchesTimeFrame(timeFrame, calendar)) continue;
            
            String executionKey = schedule.getName() + "_" + entry.getKey();
            Long lastExecution = timeBasedScheduleExecuted.get(executionKey);
            long currentMinute = calendar.getTimeInMillis() / 60000;
            
            if (lastExecution == null || lastExecution != currentMinute) {
                executeSchedule(schedule);
                
                if (schedule.isRepeat()) {
                    timeBasedScheduleExecuted.put(executionKey, currentMinute);
                } else {
                    schedule.setEnabled(false);
                }
                
                break;
            }
        }
    }
    
    private boolean isInExcludedTime(Calendar calendar) {
        if (excludeTimeStart == null || excludeTimeEnd == null) {
            return false;
        }
        
        try {
            String[] startParts = excludeTimeStart.split(":");
            String[] endParts = excludeTimeEnd.split(":");
            
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            
            int currentTotalMinutes = currentHour * 60 + currentMinute;
            int startTotalMinutes = startHour * 60 + startMinute;
            int endTotalMinutes = endHour * 60 + endMinute;
            
            if (startTotalMinutes > endTotalMinutes) {
                return currentTotalMinutes >= startTotalMinutes || currentTotalMinutes < endTotalMinutes;
            } else {
                return currentTotalMinutes >= startTotalMinutes && currentTotalMinutes < endTotalMinutes;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error procesando horario de exclusión: " + e.getMessage());
            return false;
        }
    }
    
    private int calculateIntervalSeconds(TimeFrame timeFrame) {
        int seconds = 0;
        
        if (timeFrame.getHour() != null) {
            seconds += timeFrame.getHour() * 3600;
        }
        if (timeFrame.getMinute() != null) {
            seconds += timeFrame.getMinute() * 60;
        }
        if (timeFrame.getSecond() != null) {
            seconds += timeFrame.getSecond();
        }
        
        return seconds > 0 ? seconds : 1;
    }
    
    private boolean matchesTimeFrame(TimeFrame timeFrame, Calendar calendar) {
        if (timeFrame.getMonth() != null && calendar.get(Calendar.MONTH) + 1 != timeFrame.getMonth()) {
            return false;
        }
        
        if (timeFrame.getDay() != null) {
            if (timeFrame.getDayAsNumber() != null) {
                if (calendar.get(Calendar.DAY_OF_MONTH) != timeFrame.getDayAsNumber()) {
                    return false;
                }
            } else if (timeFrame.getDayAsString() != null) {
                String dayName = timeFrame.getDayAsString().toLowerCase();
                int dayOfWeek = getDayOfWeekFromName(dayName);
                if (dayOfWeek != -1 && calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                    return false;
                }
            }
        }
        
        if (timeFrame.getHour() != null && calendar.get(Calendar.HOUR_OF_DAY) != timeFrame.getHour()) {
            return false;
        }
        
        if (timeFrame.getMinute() != null && calendar.get(Calendar.MINUTE) != timeFrame.getMinute()) {
            return false;
        }
        
        if (timeFrame.getSecond() != null && calendar.get(Calendar.SECOND) != timeFrame.getSecond()) {
            return false;
        }
        
        return true;
    }
    
    private int getDayOfWeekFromName(String day) {
        switch (day) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: return -1;
        }
    }
    
    private void executeSchedule(Schedule schedule) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        
        if (schedule.getMaxPlayers() != null && onlinePlayers > schedule.getMaxPlayers()) {
            return;
        }
        
        if (schedule.getMinPlayers() != null && onlinePlayers < schedule.getMinPlayers()) {
            boolean shouldNotify = shouldNotifyInsufficientPlayers(schedule);
            
            if (shouldNotify) {
                sendInsufficientPlayersMessage(schedule, onlinePlayers);
            }
            
            if (schedule.getMinPlayersCommands() != null && !schedule.getMinPlayersCommands().isEmpty()) {
                commandExecutor.executeCommands(schedule, schedule.getMinPlayersCommands());
            } else if (schedule.isFeedback() && !shouldNotify) {
                plugin.getLogger().info("Schedule '" + schedule.getName() + "' omitido: jugadores insuficientes (" + onlinePlayers + "/" + schedule.getMinPlayers() + ")");
            }
            return;
        }
        
        if (schedule.getCommands() != null && !schedule.getCommands().isEmpty()) {
            commandExecutor.executeCommands(schedule, schedule.getCommands());
        }
    }
    
    private boolean shouldNotifyInsufficientPlayers(Schedule schedule) {
        if (schedule.getNotifyInsufficientPlayers() != null) {
            return schedule.getNotifyInsufficientPlayers();
        }
        
        return insufficientPlayersEnabled;
    }
    
    private void sendInsufficientPlayersMessage(Schedule schedule, int currentPlayers) {
        String message;
        
        switch (insufficientPlayersType) {
            case "broadcast":
                message = insufficientPlayersMessage
                    .replace("{schedule}", schedule.getName())
                    .replace("{current}", String.valueOf(currentPlayers))
                    .replace("{required}", String.valueOf(schedule.getMinPlayers()));
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ColorUtil.translate(message));
                }
                break;
                
            case "actionbar":
                message = insufficientPlayersActionbarMessage
                    .replace("{schedule}", schedule.getName())
                    .replace("{current}", String.valueOf(currentPlayers))
                    .replace("{required}", String.valueOf(schedule.getMinPlayers()));
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        Text.actionBar(player, message);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error enviando actionbar de jugadores insuficientes: " + e.getMessage());
                    }
                }
                break;
                
            case "title":
                String titleMain = insufficientPlayersTitleMain
                    .replace("{schedule}", schedule.getName())
                    .replace("{current}", String.valueOf(currentPlayers))
                    .replace("{required}", String.valueOf(schedule.getMinPlayers()));
                
                String titleSub = insufficientPlayersTitleSub
                    .replace("{schedule}", schedule.getName())
                    .replace("{current}", String.valueOf(currentPlayers))
                    .replace("{required}", String.valueOf(schedule.getMinPlayers()));
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        player.sendTitle(
                            ColorUtil.translate(titleMain),
                            ColorUtil.translate(titleSub),
                            insufficientPlayersTitleFadeIn,
                            insufficientPlayersTitleStay,
                            insufficientPlayersTitleFadeOut
                        );
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error enviando título de jugadores insuficientes: " + e.getMessage());
                    }
                }
                break;
                
            case "subtitle":
                String subTitle = insufficientPlayersTitleSub
                    .replace("{schedule}", schedule.getName())
                    .replace("{current}", String.valueOf(currentPlayers))
                    .replace("{required}", String.valueOf(schedule.getMinPlayers()));
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        player.sendTitle(
                            "",
                            ColorUtil.translate(subTitle),
                            insufficientPlayersTitleFadeIn,
                            insufficientPlayersTitleStay,
                            insufficientPlayersTitleFadeOut
                        );
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error enviando subtítulo de jugadores insuficientes: " + e.getMessage());
                    }
                }
                break;
                
            default:
                plugin.getLogger().warning("Tipo de mensaje de jugadores insuficientes no válido: " + insufficientPlayersType);
                break;
        }
        
        if (schedule.isFeedback()) {
            plugin.getLogger().info("Schedule '" + schedule.getName() + "' cancelado por jugadores insuficientes (" + currentPlayers + "/" + schedule.getMinPlayers() + ") - Mensaje enviado a los jugadores");
        }
    }
    
    public Map<String, Schedule> getSchedules() {
        return schedules;
    }
    
    public Schedule getSchedule(String name) {
        return schedules.get(name);
    }
    
    public void reloadSchedules() {
        stopScheduler();
        loadSchedules();
        startScheduler();
    }
}