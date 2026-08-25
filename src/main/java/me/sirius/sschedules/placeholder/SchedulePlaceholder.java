package me.sirius.sschedules.placeholder;

import me.sirius.sschedules.SSchedules;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SchedulePlaceholder extends PlaceholderExpansion {
    
    private final SSchedules plugin;
    
    public SchedulePlaceholder(SSchedules plugin) {
        this.plugin = plugin;
    }
    
    @Override
    @NotNull
    public String getIdentifier() {
        return "schedule";
    }
    
    @Override
    @NotNull
    public String getAuthor() {
        return "Sirius";
    }
    
    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public boolean canRegister() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        
        if (params == null || params.isEmpty()) {
            return "Sin parametros";
        }
        
        String result = plugin.getPlaceholderManager().getScheduleCountdown(params);
        return result != null ? result : "Error";
    }
}