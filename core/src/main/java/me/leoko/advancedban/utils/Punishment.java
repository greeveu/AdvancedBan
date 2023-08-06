package me.leoko.advancedban.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
@AllArgsConstructor
@Getter
public class Punishment {

    private static final MethodInterface UNIVERSAL_METHODS = Universal.get().getMethods();
    private final String name;
    private final String uuid;
    private final Optional<String> ip;
    private String reason;
    private final String operator;
    private final PunishmentType type;
    private final long start;
    private final long end;
    private final String calculation;
    private int id;
    private boolean archived;

    public static void create(String name, String uuid, String ip, String reason, String operator, PunishmentType type, Long end, String calculation, boolean silent) {
        new Punishment(name, uuid, Optional.ofNullable(ip), reason, operator, end == -1 ? type.getPermanent() : type, TimeManager.getTime(), end, calculation, -1, false).create(silent);
    }

    public String getReason() {
        return (reason == null ? UNIVERSAL_METHODS.getString(UNIVERSAL_METHODS.getConfig(), "DefaultReason", "none") : reason).replace("'", "");
    }

    public String getHexId() {
        return Integer.toHexString(id).toUpperCase();
    }

    public String getDate(long date) {
        SimpleDateFormat format = new SimpleDateFormat(UNIVERSAL_METHODS.getString(UNIVERSAL_METHODS.getConfig(), "DateFormat", "dd.MM.yyyy-HH:mm"));
        return format.format(new Date(date));
    }

    public void create() {
        create(false);
    }

    public void create(boolean silent) {
        if (id != -1) {
            Universal.get().log("!! Failed! AB tried to overwrite the punishment:");
            Universal.get().log("!! Failed at: " + this);
            return;
        }

        if (uuid == null) {
            Universal.get().log("!! Failed! AB has not saved the " + getType().getName() + " because there is no fetched UUID");
            Universal.get().log("!! Failed at: " + this);
            return;
        }

        int currentWarningCount = getType().getBasic() == PunishmentType.WARNING ? (PunishmentManager.get().getCurrentWarns(getUuid()) + 1) : 0;

        try {
            DatabaseManager.get().executeStatement(SQLQuery.INSERT_PUNISHMENT, getName(), getUuid(), getIp().orElse(null), getReason(), getOperator(), getType().name(), getStart(), getEnd(), getCalculation());
            try (ResultSet rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_EXACT_PUNISHMENT, getUuid(), getStart(), getType().name())) {
                if (rs.next()) {
                    id = rs.getInt("id");
                } else {
                    Universal.get().log("!! Not able to update ID of punishment! Please restart the server to resolve this issue!");
                    Universal.get().log("!! Failed at: " + this);
                }
            }
        } catch (SQLException ex) {
            Universal.get().debugSqlException(ex);
        }

        if (!silent) {
            announce(currentWarningCount);
        }

        UNIVERSAL_METHODS.isOnline(getName(), isPlayerOnline -> executePunishment(currentWarningCount, isPlayerOnline));
    }

    private void executePunishment(int currentWarningCount, boolean isPlayerOnline) {
        if (isPlayerOnline) {
            if (getType().getBasic() == PunishmentType.BAN || getType() == PunishmentType.KICK) {
                UNIVERSAL_METHODS.runSync(() -> UNIVERSAL_METHODS.kickPlayer(getName(), getLayoutBSN()));
            } else {
                if (getType().getBasic() != PunishmentType.NOTE && UNIVERSAL_METHODS.isOnlineOnThisServer(getName())) {
                    UNIVERSAL_METHODS.sendMessage(UNIVERSAL_METHODS.getPlayer(getName()), getLayout());
                }
                UNIVERSAL_METHODS.requestGlobalRefresh(getUuid());
            }
        }

        PunishmentManager.get().getLoadedPunishments(false).add(this);

        UNIVERSAL_METHODS.callPunishmentEvent(this);

        if (getType().getBasic() == PunishmentType.WARNING) {
            String cmd = null;
            for (int i = 1; i <= currentWarningCount; i++) {
                if (UNIVERSAL_METHODS.contains(UNIVERSAL_METHODS.getConfig(), "WarnActions." + i)) {
                    cmd = UNIVERSAL_METHODS.getString(UNIVERSAL_METHODS.getConfig(), "WarnActions." + i);
                }
            }
            if (cmd != null) {
                String finalCmd = cmd
                    .replace("%PLAYER%", getName())
                    .replaceAll("%COUNT%", currentWarningCount + "")
                    .replaceAll("%REASON%", getReason());
                UNIVERSAL_METHODS.runSync(() -> {
                    UNIVERSAL_METHODS.executeCommand(finalCmd);
                    Universal.get().log("Executing command: " + finalCmd);
                });
            }
        }
    }

    public void updateReason(String reason) {
        this.reason = reason;

        if (id != -1) {
            DatabaseManager.get().executeStatement(SQLQuery.UPDATE_PUNISHMENT_REASON, reason, id);
        }
    }

    private void announce(int currentWarningCount) {
        String notification = MessageManager.getLayout(UNIVERSAL_METHODS.getMessages(), getType().getName() + ".Notification", "OPERATOR", getOperator(), "PREFIX", UNIVERSAL_METHODS.getBoolean(UNIVERSAL_METHODS.getConfig(), "Disable Prefix", false) ? "" : MessageManager.getMessage("General.Prefix"), "DURATION", getDuration(true), "REASON", getReason(), "NAME", getName(), "ID", String.valueOf(id), "HEXID", getHexId(), "DATE", getDate(start), "COUNT", currentWarningCount + "");

        UNIVERSAL_METHODS.notify("ab.notify." + getType().getName(), notification);
    }

    public void delete() {
        this.delete(null, false, true);
    }

    public void delete(String who, boolean massClear, boolean removeCache) {
        if (getType() == PunishmentType.KICK) {
            return;
        }

        if (id == -1) {
            Universal.get().log("!! Failed deleting! The Punishment is not created yet!");
            Universal.get().log("!! Failed at: " + this);
            return;
        }

        DatabaseManager.get().executeStatement(SQLQuery.DELETE_PUNISHMENT, getId());

        if (removeCache) {
            PunishmentManager.get().getLoadedPunishments(false).remove(this);
        }

        if (who != null) {
            String message = MessageManager.getMessage("Un" + getType().getBasic().getConfSection("Notification"), true, "OPERATOR", who, "NAME", getName());
            UNIVERSAL_METHODS.notify("ab.undoNotify." + getType().getBasic().getName(), message);

            Universal.get().debug(who + " is deleting a punishment");
        }

        Universal.get().debug("Deleted punishment " + getId() + " from " + getName() + " punishment reason: " + getReason());
        UNIVERSAL_METHODS.callRevokePunishmentEvent(this, massClear);
    }

    public String getLayout() {
        boolean isLayout = getReason().startsWith("@") || getReason().startsWith("~");

        return MessageManager.getLayout(isLayout ? UNIVERSAL_METHODS.getLayouts() : UNIVERSAL_METHODS.getMessages(), isLayout ? "Message." + getReason().split(" ")[0].substring(1) : getType().getName() + ".Layout", "OPERATOR", getOperator(), "PREFIX", UNIVERSAL_METHODS.getBoolean(UNIVERSAL_METHODS.getConfig(), "Disable Prefix", false) ? "" : MessageManager.getMessage("General.Prefix"), "DURATION", getDuration(false), "REASON", isLayout ? (getReason().split(" ").length < 2 ? "" : getReason().substring(getReason().split(" ")[0].length() + 1)) : getReason(), "HEXID", getHexId(), "ID", String.valueOf(id), "DATE", getDate(start), "COUNT", getType().getBasic() == PunishmentType.WARNING ? (PunishmentManager.get().getCurrentWarns(getUuid()) + 1) + "" : "0");
    }

    public String getDuration(boolean fromStart) {
        String duration = "permanent";
        if (getType().isTemp()) {
            long diff = ceilDiv(getEnd() - (fromStart ? start : TimeManager.getTime()), 1000L);
            if (diff > 60 * 60 * 24) {
                duration = MessageManager.getMessage("General.TimeLayoutD", getDurationParameter("D", diff / 60 / 60 / 24 + "", "H", diff / 60 / 60 % 24 + "", "M", diff / 60 % 60 + "", "S", diff % 60 + ""));
            } else if (diff > 60 * 60) {
                duration = MessageManager.getMessage("General.TimeLayoutH", getDurationParameter("H", diff / 60 / 60 + "", "M", diff / 60 % 60 + "", "S", diff % 60 + ""));
            } else if (diff > 60) {
                duration = MessageManager.getMessage("General.TimeLayoutM", getDurationParameter("M", diff / 60 + "", "S", diff % 60 + ""));
            } else {
                duration = MessageManager.getMessage("General.TimeLayoutS", getDurationParameter("S", diff + ""));
            }
        }
        return duration;
    }

    long ceilDiv(long x, long y) {
        return -Math.floorDiv(-x, y);
    }

    private String[] getDurationParameter(String... parameter) {
        int length = parameter.length;
        String[] newParameter = new String[length * 2];
        for (int i = 0; i < length; i += 2) {
            String parameterName = parameter[i];
            String parameterCount = parameter[i + 1];

            newParameter[i] = parameterName;
            newParameter[i + 1] = parameterCount;
            newParameter[length + i] = parameterName + parameterName;
            newParameter[length + i + 1] = (parameterCount.length() <= 1 ? "0" : "") + parameterCount;
        }

        return newParameter;
    }

    public String getLayoutBSN() {
        return this.getLayout();
    }

    public boolean isExpired() {
        return getType().isTemp() && getEnd() <= TimeManager.getTime();
    }

    public String toString() {
        return "Punishment(name=" + this.getName() + ", uuid=" + this.getUuid() + ", operator=" + this.getOperator() + ", calculation=" + this.getCalculation() + ", start=" + this.getStart() + ", end=" + this.getEnd() + ", type=" + this.getType() + ", reason=" + this.getReason() + ", id=" + this.getId() + ")";
    }
}
