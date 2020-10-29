package edu.uci.classes.events;

import edu.irvine.classes.commands.CommandClass;
import edu.irvine.classes.commands.CommandHandler;
import edu.irvine.classes.util.Snowflake;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventHandler extends ListenerAdapter
{

    public void onGuildMemberJoin(GuildMemberJoinEvent e)
    {
        User user = e.getUser();

        // If we aren't in the right guild or the user is a bot, we don't need to continue.
        if(e.getGuild().getIdLong() != Snowflake.GUILD_ID
                || user.isBot())
            return;

        // When a new member joins, we DM them and help them setup their nickname.
        EventNickname.setupNickname(user);
    }

    public void onMessageReceived(MessageReceivedEvent e)
    {
        User user = e.getAuthor();
        Guild guild = Snowflake.getGuild();
        Member member = guild.retrieveMember(user).complete();

        // If a bot is sending the message we don't want to continue.
        // If the member is null it means the user left the server and we don't want to continue.
        if(user.isBot() || member == null)
            return;

        // If the message isn't from a guild then it's from a DM, so we execute our DM only methods.
        if(!e.isFromGuild())
        {
            // If it isn't from the correct guild, return.
            if(guild.getIdLong() != Snowflake.GUILD_ID)
                return;

            // If their message is a response to something we ask them, one of these methods will return true
            // and we will want to return early to prevent other methods from running when not necessary.
            if(EventNickname.onSetupMemberResponse(user, e)
                    || CommandClass.classRequestResponse(guild, user, e))
                return;

            // If a member is sending us a DM and they don't have a nickname, they need to set it up before
            // continuing in doing anything else.
            if(member.getNickname() == null)
            {
                EventNickname.setupNickname(user);
                return;
            }
        }

        CommandHandler.onCommand(e, guild, user);
    }

    public void onMessageReactionAdd(MessageReactionAddEvent e)
    {
        User user = e.getUser();

        // If the user is null or is a bot, then we exit.
        if(user == null
                || user.isBot())
            return;

        if(e.isFromGuild())
        {
            // Return if it's not in the correct guild.
            if(e.getGuild().getIdLong() != Snowflake.GUILD_ID)
                return;

            EventReactResponse.onSetupMemberReact(user, e);
            CommandClass.classRequestReact(user, e);
        }
        else
        {
            EventNickname.onSetupMemberReact(user, e);
        }
    }

}