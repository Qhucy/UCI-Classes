package edu.uci.classes.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class CommandHandler
{

    public static void onCommand(MessageReceivedEvent e, Guild guild, User user)
    {
        String message = e.getMessage().getContentRaw();

        // Check to see if the message is a command.
        // Commands must start with '/' or '!' and have a size greater than 1.
        if( message.length() < 2
                || ( !message.startsWith("/") && !message.startsWith("!") ) )
            return;

        String[] splitMessage = message.substring(1).split(" ");

        // The command label is the first argument (the main command).
        // The command arguments are the arguments after this.
        String label = splitMessage[0];
        String[] args = Arrays.copyOfRange(splitMessage, 1, splitMessage.length);

        Command.runCommands(e, guild, user, label, args);
    }

}