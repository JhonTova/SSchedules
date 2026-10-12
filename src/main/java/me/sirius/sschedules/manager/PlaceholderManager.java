package me.sirius.sschedules.manager;

import me.sirius.sschedules.SSchedules;
import me.sirius.sschedules.model.Schedule;
import me.sirius.sschedules.model.TimeFrame;
import me.sirius.sschedules.util.ColorUtil;

import java.util.Calendar;
import java.util.Map;

public class PlaceholderManager {

    private final SSchedules plugin;

    public PlaceholderManager(SSchedules plugin) {
        this.plugin = plugin;
    }

    public String getScheduleCountdown(String scheduleName) {
        Schedule schedule = plugin.getScheduleManager().getSchedule(scheduleName);

        if (schedule == null) {
            return ColorUtil.translate(plugin.getConfig().getString("countdown-format.not-found", "&7No encontrado"));
        }

        if (!schedule.isEnabled()) {
            return ColorUtil.translate(plugin.getConfig().getString("countdown-format.disabled", "&cDeshabilitado"));
        }

        if (schedule.isDelayBased()) {
            return getDelayCountdown(schedule);
        } else if (schedule.isContinuous()) {
            return getContinuousCountdown(schedule);
        } else if (schedule.isTimeBased()) {
            return getTimeBasedCountdown(schedule);
        }

        return "N/A";
    }

    private String getDelayCountdown(Schedule schedule) {
        try {
            Long lastExecution = plugin.getScheduleManager().lastDelayExecution(schedule.getName());
            if (lastExecution == null) {
                return ColorUtil.translate(plugin.getConfig().getString("countdown-format.starting", "&eIniciando..."));
            }

            long currentTime = System.currentTimeMillis();
            long elapsed = (currentTime - lastExecution) / 1000;
            long remaining = schedule.getDelay() - elapsed;

            if (remaining <= 0) {
                return ColorUtil.translate(plugin.getConfig().getString("countdown-format.executing", "&6⚡ Ejecutando..."));
            }

            return formatCountdown(remaining * 1000);
        } catch (Exception e) {
            plugin.getLogger().warning("Error obteniendo countdown para schedule delay: " + schedule.getName());
            return ColorUtil.translate(plugin.getConfig().getString("countdown-format.not-found", "&7Error"));
        }
    }

    private String getContinuousCountdown(Schedule schedule) {
        try {
            Long lastExecution = plugin.getScheduleManager().lastContinuousExecution(schedule.getName());
            if (lastExecution == null) {
                return ColorUtil.translate(plugin.getConfig().getString("countdown-format.starting", "&eIniciando..."));
            }

            int intervalSeconds = schedule.getPerformOn().values().iterator().next().intervalSeconds();

            long currentTime = System.currentTimeMillis();
            long elapsed = (currentTime - lastExecution) / 1000;
            long remaining = intervalSeconds - elapsed;

            if (remaining <= 0) {
                return ColorUtil.translate(plugin.getConfig().getString("countdown-format.executing", "&6⚡ Ejecutando..."));
            }

            return formatCountdown(remaining * 1000);
        } catch (Exception e) {
            plugin.getLogger().warning("Error obteniendo countdown para schedule continuo: " + schedule.getName());
            return ColorUtil.translate(plugin.getConfig().getString("countdown-format.not-found", "&7Error"));
        }
    }

    private String getTimeBasedCountdown(Schedule schedule) {
        Calendar now = Calendar.getInstance(plugin.getTimeZone());
        long currentTime = now.getTimeInMillis();

        long nearestTime = Long.MAX_VALUE;

        for (Map.Entry<String, TimeFrame> entry : schedule.getPerformOn().entrySet()) {
            TimeFrame timeFrame = entry.getValue();
            Calendar target = getNextOccurrence(timeFrame, now);

            if (target != null) {
                long targetTime = target.getTimeInMillis();
                if (targetTime > currentTime && targetTime < nearestTime) {
                    nearestTime = targetTime;
                }
            }
        }

        if (nearestTime == Long.MAX_VALUE) {
            return ColorUtil.translate(plugin.getConfig().getString("countdown-format.no-schedule", "&7Sin programar"));
        }

        long diff = nearestTime - currentTime;

        if (diff <= 0) {
            return ColorUtil.translate(plugin.getConfig().getString("countdown-format.executing", "&6⚡ Ejecutando..."));
        }

        return formatCountdown(diff);
    }

    private Calendar getNextOccurrence(TimeFrame timeFrame, Calendar now) {
        Calendar target = (Calendar) now.clone();

        target.set(Calendar.MILLISECOND, 0);

        if (timeFrame.getSecond() != null) {
            target.set(Calendar.SECOND, timeFrame.getSecond());
        } else {
            target.set(Calendar.SECOND, 0);
        }

        if (timeFrame.getMinute() != null) {
            target.set(Calendar.MINUTE, timeFrame.getMinute());
        } else if (timeFrame.getSecond() == null) {
            target.set(Calendar.MINUTE, 0);
        }

        if (timeFrame.getHour() != null) {
            target.set(Calendar.HOUR_OF_DAY, timeFrame.getHour());
        } else if (timeFrame.getMinute() == null && timeFrame.getSecond() == null) {
            target.set(Calendar.HOUR_OF_DAY, 0);
        }

        if (timeFrame.getDay() != null) {
            if (timeFrame.getDayAsNumber() != null) {
                target.set(Calendar.DAY_OF_MONTH, timeFrame.getDayAsNumber());
            } else if (timeFrame.getDayAsString() != null) {
                int dayOfWeek = TimeFrame.dayOfWeek(timeFrame.getDayAsString());
                if (dayOfWeek != -1) {
                    int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
                    int daysToAdd = dayOfWeek - currentDayOfWeek;

                    if (daysToAdd < 0) {
                        daysToAdd += 7;
                    } else if (daysToAdd == 0) {
                        if (target.getTimeInMillis() <= now.getTimeInMillis()) {
                            daysToAdd = 7;
                        }
                    }

                    target.add(Calendar.DAY_OF_MONTH, daysToAdd);
                }
            }
        }

        if (timeFrame.getMonth() != null) {
            target.set(Calendar.MONTH, timeFrame.getMonth() - 1);
        }

        if (target.before(now) || target.equals(now)) {
            if (timeFrame.getDay() != null && timeFrame.getDayAsString() != null) {
                target.add(Calendar.WEEK_OF_YEAR, 1);
            } else if (timeFrame.getMonth() != null) {
                target.add(Calendar.YEAR, 1);
            } else if (timeFrame.getDay() != null && timeFrame.getDayAsNumber() != null) {
                target.add(Calendar.MONTH, 1);
            } else if (timeFrame.getHour() != null) {
                target.add(Calendar.DAY_OF_YEAR, 1);
            } else if (timeFrame.getMinute() != null) {
                target.add(Calendar.HOUR_OF_DAY, 1);
            } else if (timeFrame.getSecond() != null) {
                target.add(Calendar.MINUTE, 1);
            }
        }

        return target;
    }

    private String formatCountdown(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;
        long totalHours = totalMinutes / 60;
        long hours = totalHours % 24;
        long days = totalHours / 24;

        String format;

        if (days > 0) {
            format = plugin.getConfig().getString("countdown-format.with-days", "&e{days}d &f/ &b{hours}h &f/ &a{minutes}m");
            format = format.replace("{days}", String.valueOf(days))
                          .replace("{d}", String.valueOf(days))
                          .replace("{hours}", String.format("%02d", hours))
                          .replace("{h}", String.format("%02d", hours))
                          .replace("{minutes}", String.format("%02d", minutes))
                          .replace("{m}", String.format("%02d", minutes));
        } else if (hours > 0) {
            format = plugin.getConfig().getString("countdown-format.with-hours", "&b{hours}h &f/ &a{minutes}m &f/ &c{seconds}s");
            format = format.replace("{hours}", String.valueOf(hours))
                          .replace("{h}", String.valueOf(hours))
                          .replace("{minutes}", String.format("%02d", minutes))
                          .replace("{m}", String.format("%02d", minutes))
                          .replace("{seconds}", String.format("%02d", seconds))
                          .replace("{s}", String.format("%02d", seconds));
        } else if (minutes > 0) {
            format = plugin.getConfig().getString("countdown-format.with-minutes", "&a{minutes}m &f/ &c{seconds}s");
            format = format.replace("{minutes}", String.valueOf(minutes))
                          .replace("{m}", String.valueOf(minutes))
                          .replace("{seconds}", String.format("%02d", seconds))
                          .replace("{s}", String.format("%02d", seconds));
        } else {
            format = plugin.getConfig().getString("countdown-format.with-seconds", "&c{seconds}s");
            format = format.replace("{seconds}", String.valueOf(seconds))
                          .replace("{s}", String.valueOf(seconds));
        }

        return ColorUtil.translate(format);
    }
}