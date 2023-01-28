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

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
@AllArgsConstructor
@Getter
public class Punishment {

    private static final MethodInterface mi = Universal.get().getMethods();
    private final String name;
    private final String uuid;
    private String reason;
    private final String operator;
    private final PunishmentType type;
    private final long start;
    private final long end;
    private final String calculation;
    private int id;

    public static void create(String name, String target, String reason, String operator, PunishmentType type, Long end, String calculation, boolean silent) {
        new Punishment(name, target, reason, operator, end == -1 ? type.getPermanent() : type, TimeManager.getTime(), end, calculation, -1)
            .create(silent);
    }

    public String getReason() {
        return (reason == null ? mi.getString(mi.getConfig(), "DefaultReason", "none") : reason).replace("'", "");
    }

    public String getHexId() {
        return Integer.toHexString(id).toUpperCase();
    }

    public String getDate(long date) {
        SimpleDateFormat format = new SimpleDateFormat(mi.getString(mi.getConfig(), "DateFormat", "dd.MM.yyyy-HH:mm"));
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

        final int currentWarningCount = getType().getBasic() == PunishmentType.WARNING ? (PunishmentManager.get().getCurrentWarns(getUuid()) + 1) : 0;

        DatabaseManager.get().executeStatement(SQLQuery.INSERT_PUNISHMENT_HISTORY, getName(), getUuid(), getReason(), getOperator(), getType().name(), getStart(), getEnd(), getCalculation());

        if (getType() != PunishmentType.KICK) {
            try {
                DatabaseManager.get().executeStatement(SQLQuery.INSERT_PUNISHMENT, getName(), getUuid(), getReason(), getOperator(), getType().name(), getStart(), getEnd(), getCalculation());
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
        }

        if (!silent) {
            announce(currentWarningCount);
        }

        mi.isOnline(getName(), isPlayerOnline -> executePunishment(currentWarningCount, isPlayerOnline));
    }

    private void executePunishment(int currentWarningCount, boolean isPlayerOnline) {
        if (isPlayerOnline) {
            if (getType().getBasic() == PunishmentType.BAN || getType() == PunishmentType.KICK) {
                mi.runSync(() -> mi.kickPlayer(getName(), getLayoutBSN()));
            } else {
                if (getType().getBasic() != PunishmentType.NOTE
                    && mi.isOnlineOnThisServer(getName())
                ) {
                    mi.sendMessage(mi.getPlayer(getName()), getLayout());
                }
                PunishmentManager.get().getLoadedPunishments(false).add(this);
            }
        }

        PunishmentManager.get().getLoadedHistory().add(this);

        mi.callPunishmentEvent(this);

        if (getType().getBasic() == PunishmentType.WARNING) {
            String cmd = null;
            for (int i = 1; i <= currentWarningCount; i++) {
                if (mi.contains(mi.getConfig(), "WarnActions." + i)) {
                    cmd = mi.getString(mi.getConfig(), "WarnActions." + i);
                }
            }
            if (cmd != null) {
                final String finalCmd = cmd.replace("%PLAYER%", getName())
                    .replaceAll("%COUNT%", currentWarningCount + "")
                    .replaceAll("%REASON%", getReason());
                mi.runSync(() -> {
                    mi.executeCommand(finalCmd);
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
        String notification = MessageManager.getLayout(mi.getMessages(),
            getType().getName() + ".Notification",
            "OPERATOR", getOperator(),
            "PREFIX", mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? "" : MessageManager.getMessage("General.Prefix"),
            "DURATION", getDuration(true),
            "REASON", getReason(),
            "NAME", getName(),
            "ID", String.valueOf(id),
            "HEXID", getHexId(),
            "DATE", getDate(start),
            "COUNT", currentWarningCount + "");

        mi.notify("ab.notify." + getType().getName(), notification);
    }

    public void delete() {
        this.delete(null, false, true);
    }

    public void delete(String who, boolean massClear, boolean removeCache) {
        if (getType() == PunishmentType.KICK) {
            Universal.get().log("!! Failed deleting! You are not able to delete Kicks!");
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
            String message = MessageManager.getMessage("Un" + getType().getBasic().getConfSection("Notification"),
                true, "OPERATOR", who, "NAME", getName());
            mi.notify("ab.undoNotify." + getType().getBasic().getName(), message);

            Universal.get().debug(who + " is deleting a punishment");
        }

        Universal.get().debug("Deleted punishment " + getId() + " from " + getName() + " punishment reason: " + getReason());
        mi.callRevokePunishmentEvent(this, massClear);
    }

    public String getLayout() {
        boolean isLayout = getReason().startsWith("@") || getReason().startsWith("~");

        return MessageManager.getLayout(
            isLayout ? mi.getLayouts() : mi.getMessages(),
            isLayout ? "Message." + getReason().split(" ")[0].substring(1) : getType().getName() + ".Layout",
            "OPERATOR", getOperator(),
            "PREFIX", mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? "" : MessageManager.getMessage("General.Prefix"),
            "DURATION", getDuration(false),
            "REASON", isLayout ? (getReason().split(" ").length < 2 ? "" : getReason().substring(getReason().split(" ")[0].length() + 1)) : getReason(),
            "HEXID", getHexId(),
            "ID", String.valueOf(id),
            "DATE", getDate(start),
            "COUNT", getType().getBasic() == PunishmentType.WARNING ? (PunishmentManager.get().getCurrentWarns(getUuid()) + 1) + "" : "0");
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
