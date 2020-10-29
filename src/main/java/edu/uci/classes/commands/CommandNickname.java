package edu.uci.classes.commands;

import edu.irvine.classes.events.EventNickname;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandNickname extends Command
{

    public CommandNickname()
    {
        super(CommandType.UNIVERSAL, null, "nickname", "nick", "name", "nn");
    }

    public void onCommand(MessageReceivedEvent e, User user, Member member, Guild guild, String label, String[] args)
    {
        EventNickname.setupNickname(user);
    }

}