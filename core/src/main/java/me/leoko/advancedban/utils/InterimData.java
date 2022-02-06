package me.leoko.advancedban.utils;

import me.leoko.advancedban.manager.PunishmentManager;

import java.util.Comparator;
import java.util.Set;

/**
 * Created by Leo on 04.08.2017.
 */
public class InterimData {

    private final String uuid, name, ip;
    private final Set<Punishment> punishments, history;

    public InterimData(String uuid, String name, String ip, Set<Punishment> punishments, Set<Punishment> history) {
        this.uuid = uuid;
        this.name = name;
        this.ip = ip;
        this.punishments = punishments;
        this.history = history;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public Set<Punishment> getPunishments() {
        return punishments;
    }

    public Set<Punishment> getHistory() {
        return history;
    }

    public Punishment getBan() {
        return punishments.stream()
                .filter(pt -> pt.getType().getBasic() == PunishmentType.BAN && !pt.isExpired())
                .filter(punishment -> punishment.getEnd() == -1)
                .min(Comparator.comparing(Punishment::getId))
                .orElseGet(() -> punishments.stream()
                        .filter(pt -> pt.getType().getBasic() == PunishmentType.BAN && !pt.isExpired())
                        .max(Comparator.comparing(Punishment::getEnd))
                        .orElse(null)
                );
    }

    public void accept() {
        PunishmentManager.get().getLoadedPunishments(false).addAll(punishments);
        PunishmentManager.get().getLoadedHistory().addAll(history);
        PunishmentManager.get().setCached(this);
    }
}