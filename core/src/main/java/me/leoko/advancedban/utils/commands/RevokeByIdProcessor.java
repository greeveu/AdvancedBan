package me.leoko.advancedban.utils.commands;

import lombok.AllArgsConstructor;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class RevokeByIdProcessor implements Consumer<Command.CommandInput> {
    private final String path;
    private final Function<Integer, Punishment> resolver;

    @Override
    public void accept(Command.CommandInput input) {
        int id = Integer.parseInt(input.getPrimary());

        if (!input.hasNext()) {
            MessageManager.sendMessage(input.getSender(), path + ".Usage", true);
            return;
        }

        input.next();

        Punishment punishment = resolver.apply(id);
        if (punishment == null) {
            MessageManager.sendMessage(input.getSender(), path + ".NotFound", true, "ID", id + "");
            return;
        }

        final String operator = Universal.get().getMethods().getName(input.getSender());
        punishment.delete(operator, false, true);
        Punishment.create(punishment.getName(), punishment.getUuid(), String.join(" ", input.getArgs()), operator, PunishmentType.REVOKE_NOTE, -1L, "", true);
        MessageManager.sendMessage(input.getSender(), path + ".Done", true, "ID", id + "");
    }
}
