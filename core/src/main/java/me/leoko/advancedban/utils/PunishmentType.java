package me.leoko.advancedban.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
@AllArgsConstructor
public enum PunishmentType {
    BAN("Ban", null, false, "ab.ban.perma"),
    TEMP_BAN("Tempban", BAN, true, "ab.ban.temp"),
    IP_BAN("Ipban", BAN, false, "ab.ipban.perma"),
    TEMP_IP_BAN("Tempipban", BAN, true, "ab.ipban.temp"),
    MUTE("Mute", null, false, "ab.mute.perma"),
    TEMP_MUTE("Tempmute", MUTE, true, "ab.mute.temp"),
    WARNING("Warn", null, false, "ab.warn.perma"),
    TEMP_WARNING("Tempwarn", WARNING, true, "ab.warn.temp"),
    KICK("Kick", null, false, "ab.kick.use"),
    NOTE("Note", null, false, "ab.note.use"),
    REVOKE_NOTE("Revokenote", null, false, "ab.note.use"); // This is a special case, as it's not a punishment type, but a command and it *should* be a basic note, but the way the plugin is made that's not possible

    @Getter
    private final String name;
    private final PunishmentType basic;
    @Getter
    private final boolean temp;
    @Getter
    private final String perms;

    public static PunishmentType fromCommandName(String cmd) {
        switch (cmd) {
            case "ban":
                return BAN;
            case "tempban":
                return TEMP_BAN;
            case "ban-ip":
            case "banip":
            case "ipban":
                return IP_BAN;
            case "tempipban":
            case "tipban":
                return TEMP_IP_BAN;
            case "mute":
                return MUTE;
            case "tempmute":
                return TEMP_MUTE;
            case "warn":
                return WARNING;
            case "note":
                return NOTE;
            case "revokenote":
                return REVOKE_NOTE;
            case "tempwarn":
                return TEMP_WARNING;
            case "kick":
                return KICK;
            default:
                return null;
        }
    }

    public String getConfSection(String path) {
        return name + "." + path;
    }

    public PunishmentType getBasic() {
        return basic == null ? this : basic;
    }

    public PunishmentType getPermanent() {
        if (this == IP_BAN || this == TEMP_IP_BAN) {
            return IP_BAN;
        }

        if (this == REVOKE_NOTE) {
            return REVOKE_NOTE;
        }

        return getBasic();
    }

    public boolean isIpOrientated() {
        return this == IP_BAN || this == TEMP_IP_BAN;
    }
}
