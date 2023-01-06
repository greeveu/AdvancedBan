package me.leoko.advancedban.utils.commands;

import lombok.AllArgsConstructor;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.Permissionable;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static me.leoko.advancedban.utils.CommandUtils.*;

@AllArgsConstructor
public class PunishmentProcessor implements Consumer<Command.CommandInput> {
    private final PunishmentType type;

    @Override
    public void accept(Command.CommandInput input) {
        boolean silent = processTag(input, "-s");
        String name = input.getPrimary();

        // extract target
        String target = type.isIpOrientated() ? processIP(input) : processName(input);
        if (target == null) {
            return;
        }

        // is exempted
        processExempt(name, target, input.getSender(), type, exempted -> {
            if (exempted) return;

            // calculate duration if necessary
            Long end = -1L;
            String timeTemplate = "";
            if (type.isTemp()) {
                TimeCalculation calculation = processTime(input, target, type);
                if (calculation == null) {
                    return;
                }

                end = calculation.time;

                if (calculation.template != null) {
                    timeTemplate = calculation.template;
                }
            }

            // build reason
            String reason = processReason(input);
            if (reason == null) {
                return;
            } else if (reason.isEmpty()) {
                reason = null;
            }

            // check if punishment of this type is already active
            if (alreadyPunished(target, type)) {
                MessageManager.sendMessage(input.getSender(), type.getBasic().getName() + ".AlreadyDone", true, "NAME", name);
                return;
            }

            MethodInterface mi = Universal.get().getMethods();
            String operator = mi.getName(input.getSender());
            Punishment.create(name, target, reason, operator, type, end, timeTemplate, silent);

            MessageManager.sendMessage(input.getSender(), type.getBasic().getName() + ".Done", true, "NAME", name);
        });
    }

    // Removes time argument and returns timestamp (null if failed)
    private static TimeCalculation processTime(Command.CommandInput input, String uuid, PunishmentType type) {
        String time = input.getPrimary();
        input.next();
        MethodInterface mi = Universal.get().getMethods();
        if (time.matches("#.+")) {
            String layout = time.substring(1);
            if (!mi.contains(mi.getLayouts(), "Time." + layout)) {
                MessageManager.sendMessage(input.getSender(), "General.LayoutNotFound", true, "NAME", layout);
                return null;
            }
            int i = PunishmentManager.get().getCalculationLevel(uuid, layout);
            List<String> timeLayout = mi.getStringList(mi.getLayouts(), "Time." + layout);
            String timeName = timeLayout.get(Math.min(i, timeLayout.size() - 1));
            if (timeName.equalsIgnoreCase("perma")) {
                return new TimeCalculation(layout, -1L);
            }
            Long actualTime = TimeManager.getTime() + TimeManager.toMilliSec(timeName);
            return new TimeCalculation(layout, actualTime);
        }

        long toAdd = TimeManager.toMilliSec(time);
        if (!Universal.get().hasPerms(input.getSender(), "ab." + type.getName() + ".dur.max")) {
            long max = -1;
            for (int i = 10; i >= 1; i--) {
                if (Universal.get().hasPerms(input.getSender(), "ab." + type.getName() + ".dur." + i) &&
                    mi.contains(mi.getConfig(), "TempPerms." + i)) {
                    max = mi.getLong(mi.getConfig(), "TempPerms." + i) * 1000;
                    break;
                }
            }
            if (max != -1 && toAdd > max) {
                MessageManager.sendMessage(input.getSender(), type.getName() + ".MaxDuration", true, "MAX", max / 1000 + "");
                return null;
            }
        }

        return new TimeCalculation(null, TimeManager.getTime() + toAdd);
    }

    // Checks whether target is exempted from punishment
    private static void processExempt(String name, String target, Object sender, PunishmentType type, Consumer<Boolean> callback) {
        MethodInterface mi = Universal.get().getMethods();
        String dataName = name.toLowerCase();

        boolean exempt;
        if (mi.isOnlineOnThisServer(dataName)) {
            Object onlineTarget = mi.getPlayer(dataName);
            exempt = canNotPunish(
                perms -> mi.hasPerms(sender, perms),
                perms -> mi.hasPerms(onlineTarget, perms),
                type.getName()
            );
        } else {
            final Permissionable offlinePermissionPlayer = mi.getOfflinePermissionPlayer(name);
            exempt = Universal.get().isExemptPlayer(dataName)
                || canNotPunish(perms -> mi.hasPerms(sender, perms), offlinePermissionPlayer::hasPermission, type.getName());
        }

        if (exempt) {
            MessageManager.sendMessage(sender, type.getBasic().getName() + ".Exempt", true, "NAME", name);
            callback.accept(true);
            return;
        }
        callback.accept(false);
    }

    // Check based on exempt level if some is able to ban a player
    public static boolean canNotPunish(Function<String, Boolean> operatorHasPerms, Function<String, Boolean> targetHasPerms, String path) {
        final String perms = "ab." + path + ".exempt";
        if (targetHasPerms.apply(perms)) {
            return true;
        }

        int targetLevel = permissionLevel(targetHasPerms, perms);
        int operatorLevel = permissionLevel(operatorHasPerms, perms);

        return targetLevel != 0 && operatorLevel <= targetLevel;
    }

    private static int permissionLevel(Function<String, Boolean> hasPerms, String permission) {
        for (int i = 10; i >= 1; i--) {
            if (hasPerms.apply(permission + "." + i)) {
                return i;
            }
        }

        return 0;
    }

    // Checks whether input contains tag and removes it
    private static boolean processTag(Command.CommandInput input, String tag) {
        // Check the first few arguments for the tag
        String[] args = input.getArgs();
        for (int i = 0; i < args.length && i < 4; i++) {
            if (tag.equalsIgnoreCase(args[i])) {
                input.removeArgument(i);
                return true;
            }
        }
        return false;
    }

    private static boolean alreadyPunished(String target, PunishmentType type) {
        return (type.getBasic() == PunishmentType.MUTE && PunishmentManager.get().isMuted(target))
            || (type.getBasic() == PunishmentType.BAN && PunishmentManager.get().isBanned(target));
    }

    @AllArgsConstructor
    private static class TimeCalculation {
        private final String template;
        private final Long time;
    }
}
