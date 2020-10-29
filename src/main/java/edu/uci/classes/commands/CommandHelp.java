package edu.uci.classes.commands;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelp extends Command
{

    public CommandHelp()
    {
        super(CommandType.UNIVERSAL, new String[] {"**/nickname**: Setup your server nickname.",
                                                   "**/class**: Add/remove classes for yourself."}, "help", "about");
    }

    public void onCommand(MessageReceivedEvent e, User user, Member member, Guild guild, String label, String[] args)
    {
        super.sendUsage(user.openPrivateChannel().complete(), label);
    }

}