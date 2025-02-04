package me.leoko.advancedban.manager;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.Command;

/**
 * The Command Manager is used to handle commands based on the sender, command-name and arguments.
 */
public class CommandManager {

    private static CommandManager instance = null;

    /**
     * Get the instance of the command manager
     *
     * @return the command manager instance
     */
    public static synchronized CommandManager get() {
        if (instance == null) instance = new CommandManager();

        return instance;
    }

    /**
     * Handle/Perform a command.
     *
     * @param sender the sender which executes the command
     * @param cmd    the command name
     * @param args   the arguments for this command
     */
    public void onCommand(final Object sender, final String cmd, final String[] args) {
        Universal.get().getMethods().runAsync(() -> {
            Command command = Command.getByName(cmd);
            if (command == null) {
                return;
            }

            String permission = command.getPermission();
            if (permission != null && !Universal.get().hasPerms(sender, permission)) {
                MessageManager.sendMessage(sender, "General.NoPerms", true);
                return;
            }

            if (!command.validateArguments(args)) {
                MessageManager.sendMessage(sender, command.getUsagePath(), true);
                return;
            }

            command.execute(sender, args);
        });
    }
}
