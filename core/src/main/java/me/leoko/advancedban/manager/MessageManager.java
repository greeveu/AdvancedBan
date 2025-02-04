package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;

/**
 * The Message Manager is used for a convenient way to retrieve messages from configuration files.<br>
 * The manager is designed for (but not limited to) messages from the <code>message.yml</code> file.
 */
public class MessageManager {

    private static MethodInterface mi() {
        return Universal.get().getMethods();
    }

    /**
     * Get the message from the given path.<br>
     * The parameters work as described in {@link #sendMessage(Object, String, boolean, String...)}.
     *
     * @param path       the path
     * @param parameters the parameters
     * @return the message
     */
    public static String getMessage(String path, String... parameters) {
        MethodInterface mi = mi();
        String str = mi.getString(mi.getMessages(), path);
        if (str == null) {
            str = "Failed! See console for details!";
            System.out.println("!! Message-Error!\n"
                + "In order to solve the problem please:"
                + "\n  - Check the Message.yml-File for any missing or double \" or '"
                + "\n  - Visit yamllint.com to  validate your Message.yml"
                + "\n  - Delete the message file and restart the server");
        } else {
            str = replace(str, parameters).replace('&', '§');
        }
        return str;
    }


    /**
     * Get the message from the given path.<br>
     * The parameters work as described in {@link #sendMessage(Object, String, boolean, String...)}.
     *
     * @param path       the path
     * @param prefix     whether to prepend a prefix (can be overridden by the DisablePrefix option)
     * @param parameters the parameters
     * @return the message
     */
    public static String getMessage(String path, boolean prefix, String... parameters) {
        MethodInterface mi = mi();
        String prefixStr = "";
        if (prefix && !mi.getBoolean(mi.getConfig(), "Disable Prefix", false)) {
            prefixStr = getMessage("General.Prefix") + " ";
        }

        return prefixStr + getMessage(path, parameters);
    }

    /**
     * Get the layout (basically just a string list) from the given path in the given file.<br>
     * The parameters work as described in {@link #sendMessage(Object, String, boolean, String...)}.
     *
     * @param file       the file (see {@link MethodInterface#getConfig()}, {@link MethodInterface#getMessages()},
     *                   {@link MethodInterface#getLayouts()})
     * @param path       the path
     * @param parameters the parameters
     * @return the layout
     */
    public static String getLayout(Object file, String path, String... parameters) {
        MethodInterface mi = mi();
        StringBuilder stringBuilder = new StringBuilder();
        if (mi.contains(file, path)) {
            for (String str : mi.getStringList(file, path)) {
                stringBuilder.append(replace(str, parameters).replace('&', '§')).append("\n");
            }
            return stringBuilder.toString().trim();
        }
        String fileName = mi.getFileName(file);
        System.out.println("!! Message-Error in " + fileName + "!\n"
            + "In order to solve the problem please:"
            + "\n  - Check the " + fileName + "-File for any missing or double \" or '"
            + "\n  - Visit yamllint.com to  validate your " + fileName
            + "\n  - Delete the message file and restart the server");
        return "Failed! See console for details!";
    }

    /**
     * Send message from the given path directly to the given receiver.<br><br>
     * <b>How the <code>parameters</code> work:</b>
     * The amount of parameters given has to be an even number as the parameters are interpreted in pairs.<br>
     * The first parameter is the String to search for and the second one is the one it will be replaced with.<br>
     * Same goes for the third and fourth and for the fifth and sixth and so on.<br><br>
     * <b>e.g.:</b> <code>getMessage("some.path", "NAME", "Leoko", "ID", "#342")</code> will get the message located at
     * "some.path" and replace each <i>%NAME%</i> with <i>Leoko</i> and each <i>%ID%</i> with <i>#342</i>.
     *
     * @param receiver   the receiver (Bukkit or Bungeecord player object)
     * @param path       the path
     * @param prefix     whether to use the global prefix
     * @param parameters the parameters
     */
    public static void sendMessage(Object receiver, String path, boolean prefix, String... parameters) {
        MethodInterface mi = mi();
        final String message = getMessage(path, parameters);
        if (!message.isEmpty()) {
            final String prefixString = prefix && !mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? getMessage("General.Prefix") + " " : "";
            mi.sendMessage(receiver, prefixString + message);
        }
    }

    private static String replace(String str, String... parameters) {
        for (int i = 0; i < parameters.length - 1; i = i + 2) {
            str = str.replaceAll("%" + parameters[i] + "%", parameters[i + 1]);
        }
        return str;
    }
}
