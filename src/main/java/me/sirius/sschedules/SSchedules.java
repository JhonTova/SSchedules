package me.sirius.sschedules;

import org.bukkit.Bukkit;
import me.sirius.core.SiriusPlugin;
import me.sirius.sschedules.manager.ScheduleManager;
import me.sirius.sschedules.manager.PlaceholderManager;
import me.sirius.sschedules.commands.ScheduleCommand;

import java.util.TimeZone;

public class SSchedules extends SiriusPlugin {
    
    private static SSchedules instance;
    private ScheduleManager scheduleManager;
    private PlaceholderManager placeholderManager;
    private TimeZone timeZone;
    private me.sirius.sschedules.placeholder.SchedulePlaceholder expansion;
    
    @Override
    protected void onStart() {
        instance = this;
        
        saveDefaultConfig();
        
        String timezone = getConfig().getString("timezone", "America/Bogota");
        timeZone = TimeZone.getTimeZone(timezone);
        
        getLogger().info("===========================================");
        getLogger().info("  SSchedules - Sistema Avanzado");
        getLogger().info("  Zona horaria: " + timezone);
        getLogger().info("  Hora actual: " + java.time.LocalDateTime.now(timeZone.toZoneId()));
        getLogger().info("===========================================");
        
        placeholderManager = new PlaceholderManager(this);
        scheduleManager = new ScheduleManager(this);
        
        scheduleManager.loadSchedules();
        scheduleManager.startScheduler();
        
        ScheduleCommand scheduleCommand = new ScheduleCommand(this);
        getCommand("sschedules").setExecutor(scheduleCommand);
        getCommand("sschedules").setTabCompleter(scheduleCommand);
        
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                try {
                    expansion = new me.sirius.sschedules.placeholder.SchedulePlaceholder(this);
                    boolean registered = expansion.register();
                    if (registered) {
                        getLogger().info("✓ PlaceholderAPI detectado - Placeholders registrados correctamente");
                        getLogger().info("  Usa: %schedule_<nombre-del-schedule>% en tus hologramas");
                        getLogger().info("  Ejemplo: %schedule_koth-sakura-diamante%");
                    } else {
                        getLogger().warning("✗ Error al registrar placeholders con PlaceholderAPI");
                    }
                } catch (Exception e) {
                    getLogger().warning("✗ Error registrando placeholders: " + e.getMessage());
                }
            } else {
                getLogger().warning("⚠ PlaceholderAPI no encontrado - Los placeholders no funcionarán");
                getLogger().warning("  Descarga PlaceholderAPI para usar los placeholders");
            }
        }, 20L);
        
        getLogger().info("✓ SSchedules habilitado correctamente");
    }
    
    @Override
    protected void onStop() {
        if (scheduleManager != null) {
            scheduleManager.stopScheduler();
        }
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
        }
        getLogger().info("SSchedules deshabilitado correctamente");
    }
    
    public static SSchedules getInstance() {
        return instance;
    }
    
    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
}