package edu.uci.classes.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Command
{

    private static final ArrayList<Command> commands = new ArrayList<>();

    public static void runCommands(MessageReceivedEvent e, Guild guild, User user, String label, String[] args)
    {
        Member member = guild.retrieveMember(user).complete();

        // If this is null, they aren't a member of the server.
        if(member == null)
            return;

        for(Command command : commands)
        {
            // Check if this command in iteration is the correct command and in the correct command type circumstance.
            if( !command.containsLabel(label)
                    || ( command.getCommandType() != CommandType.UNIVERSAL && command.getCommandType() == CommandType.GUILD_ONLY && !e.isFromGuild() ) )
                continue;

            command.onCommand(e, user, member, guild, label, args);
            return;
        }
    }

    public static void addCommand(Command command)
    {
        commands.add(command);
    }

    private final CommandType commandType;
    private final String[] usage;
    private final List<String> labels;

    public Command(CommandType commandType, String[] usage, String... labels)
    {
        this.commandType = commandType;
        this.usage = usage;
        this.labels = Arrays.asList(labels);
    }

    public CommandType getCommandType()
    {
        return commandType;
    }

    public boolean containsLabel(String label)
    {
        return labels.contains(label);
    }

    // Send the usage help for this command.
    public void sendUsage(MessageChannel messageChannel, String label)
    {
        // If there isn't a usage defined, we can't send them anything.
        if(usage == null
                || usage.length < 1)
        {
            messageChannel.sendMessage("There is no defined usage for this command.").queue();
            return;
        }

        // If the usage is only one line, we can send them it simplified.
        if(usage.length == 1)
        {
            messageChannel.sendMessage("Invalid syntax: </" + usage[0] + ">").queue();
        }
        else
        {
            StringBuilder usageString = new StringBuilder();

            for(String usageArgument : usage)
            {
                usageArgument = usageArgument.replace("{label}", label);

                if(usageString.length() == 0)
                    usageString.append(usageArgument);
                else
                    usageString.append("\n  ").append(usageArgument);
            }

            messageChannel.sendMessage("**Showing help for /" + label + "**:\n" +
                    "  " + usageString.toString()).queue();
        }
    }

    public abstract void onCommand(MessageReceivedEvent e, User user, Member member, Guild guild, String label, String[] args);

}