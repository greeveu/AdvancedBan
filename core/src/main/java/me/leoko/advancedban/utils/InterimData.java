package me.leoko.advancedban.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.leoko.advancedban.manager.PunishmentManager;

import java.util.Comparator;
import java.util.Set;

/**
 * Created by Leo on 04.08.2017.
 */
@AllArgsConstructor
@Getter
public class InterimData {
    private final String uuid;
    private final String name;
    private final String ip;
    private final Set<Punishment> punishments;

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
        PunishmentManager.get().setCached(this);
    }
}
