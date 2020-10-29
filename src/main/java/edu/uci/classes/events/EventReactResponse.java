package edu.uci.classes.events;

import edu.irvine.classes.commands.CommandClass;
import edu.irvine.classes.util.Snowflake;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class EventReactResponse
{

    public static void onSetupMemberReact(User user, MessageReactionAddEvent e)
    {
        long messageId = e.getMessageIdLong();

        // If they aren't reacting on these two key messages, we don't care and return.
        if(messageId != Snowflake.MESSAGE_CLASS_SIGNUP
                && messageId != Snowflake.MESSAGE_CLASS_REQUEST
                && messageId != Snowflake.MESSAGE_NICKNAME)
            return;

        PrivateChannel privateChannel = user.openPrivateChannel().complete();

        if(messageId == Snowflake.MESSAGE_CLASS_SIGNUP)
        {
            privateChannel.sendMessage("**Hello! Please consult the below commands to add classes**:\n" +
                    "  **/class list**: List all classes that we support.\n" +
                    "  **/class add [id]**: Add yourself to a new class.\n" +
                    "  **/class remove [id]**: Remove yourself from a class.\n" +
                    "  **/class clear**: Remove yourself from all your classes.\n" +
                    "  **/class request**: Submit a request for a new class to be added.").queue();
        }
        else if(messageId == Snowflake.MESSAGE_CLASS_REQUEST)
        {
            CommandClass.startClassRequest(privateChannel, user);
        }
        else
        {
            EventNickname.setupNickname(user);
        }
    }

}