package me.sirius.sschedules.model;

import java.util.List;
import java.util.Map;

public class Schedule {
    
    private final String name;
    private boolean enabled;
    private Integer delay;
    private boolean randomize;
    private boolean duplicateRandomize;
    private boolean singleLinear;
    private Map<String, TimeFrame> performOn;
    private boolean repeat;
    private Integer minPlayers;
    private Integer maxPlayers;
    private List<String> minPlayersCommands;
    private boolean feedback;
    private boolean dontTranslatePlaceholders;
    private List<String> commands;
    private Boolean notifyInsufficientPlayers;
    
    private int currentLinearIndex = 0;
    private List<String> usedRandomCommands;
    
    public Schedule(String name) {
        this.name = name;
        this.enabled = true;
        this.repeat = true;
        this.feedback = true;
        this.dontTranslatePlaceholders = false;
        this.notifyInsufficientPlayers = null;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Integer getDelay() {
        return delay;
    }
    
    public void setDelay(Integer delay) {
        this.delay = delay;
    }
    
    public boolean isRandomize() {
        return randomize;
    }
    
    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }
    
    public boolean isDuplicateRandomize() {
        return duplicateRandomize;
    }
    
    public void setDuplicateRandomize(boolean duplicateRandomize) {
        this.duplicateRandomize = duplicateRandomize;
    }
    
    public boolean isSingleLinear() {
        return singleLinear;
    }
    
    public void setSingleLinear(boolean singleLinear) {
        this.singleLinear = singleLinear;
    }
    
    public Map<String, TimeFrame> getPerformOn() {
        return performOn;
    }
    
    public void setPerformOn(Map<String, TimeFrame> performOn) {
        this.performOn = performOn;
    }
    
    public boolean isRepeat() {
        return repeat;
    }
    
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
    
    public Integer getMinPlayers() {
        return minPlayers;
    }
    
    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }
    
    public Integer getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public List<String> getMinPlayersCommands() {
        return minPlayersCommands;
    }
    
    public void setMinPlayersCommands(List<String> minPlayersCommands) {
        this.minPlayersCommands = minPlayersCommands;
    }
    
    public boolean isFeedback() {
        return feedback;
    }
    
    public void setFeedback(boolean feedback) {
        this.feedback = feedback;
    }
    
    public boolean isDontTranslatePlaceholders() {
        return dontTranslatePlaceholders;
    }
    
    public void setDontTranslatePlaceholders(boolean dontTranslatePlaceholders) {
        this.dontTranslatePlaceholders = dontTranslatePlaceholders;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
    
    public Boolean getNotifyInsufficientPlayers() {
        return notifyInsufficientPlayers;
    }
    
    public void setNotifyInsufficientPlayers(Boolean notifyInsufficientPlayers) {
        this.notifyInsufficientPlayers = notifyInsufficientPlayers;
    }
    
    public int getCurrentLinearIndex() {
        return currentLinearIndex;
    }
    
    public void setCurrentLinearIndex(int currentLinearIndex) {
        this.currentLinearIndex = currentLinearIndex;
    }
    
    public List<String> getUsedRandomCommands() {
        return usedRandomCommands;
    }
    
    public void setUsedRandomCommands(List<String> usedRandomCommands) {
        this.usedRandomCommands = usedRandomCommands;
    }
    
    public boolean isDelayBased() {
        return delay != null;
    }
    
    public boolean isTimeBased() {
        if (performOn == null || performOn.isEmpty()) {
            return false;
        }
        
        for (TimeFrame frame : performOn.values()) {
            if (frame.getDay() != null) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isContinuous() {
        if (performOn == null || performOn.isEmpty()) {
            return false;
        }
        
        for (TimeFrame frame : performOn.values()) {
            if (frame.getDay() == null) {
                return true;
            }
        }
        
        return false;
    }
}