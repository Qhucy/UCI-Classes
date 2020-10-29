package edu.uci.classes.commands;

import edu.irvine.classes.classes.Class;
import edu.irvine.classes.classes.ClassManager;
import edu.irvine.classes.classes.UpdateAction;
import edu.irvine.classes.util.EmbedManager;
import edu.irvine.classes.util.Snowflake;
import edu.irvine.classes.util.memory.MemoryManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CommandClass extends Command
{

    // The map that keeps track of who is confirming to clear all their classes.
    // The key is the user's Discord Id.
    // The value is the destruction time of their row in the map.
    private static final HashMap<Long, Long> clearConfirmation = new HashMap<>();

    // The map that keeps track of who is requesting a class.
    // The key is the user's Discord Id.
    // The value is the destruction time of their row in the map.
    private static final HashMap<Long, Long> classRequestMap = new HashMap<>();

    public CommandClass()
    {
        super(CommandType.UNIVERSAL, new String[] {"**/{label} list**: List all classes that we support.",
                                                   "**/{label} add [id]**: Add yourself to a new class.",
                                                   "**/{label} remove [id]**: Remove yourself from a class.",
                                                   "**/{label} clear**: Remove yourself from all your classes.",
                                                   "**/{label} request**: Submit a request for a new class to be added."}, "class", "course");
    }

    public void onCommand(MessageReceivedEvent e, User user, Member member, Guild guild, String label, String[] args)
    {
        PrivateChannel privateChannel = user.openPrivateChannel().complete();

        if(args.length < 1)
        {
            sendUsage(privateChannel, label);
            return;
        }

        if(args[0].equalsIgnoreCase("clear"))
        {
            if(!clearConfirmation.containsKey(user.getIdLong()))
            {
                // Check to see if the user even has classes in their roles.
                boolean hasClassRole = false;

                for(Role role : member.getRoles())
                {
                    // If this method returns null, then the role we're on isn't a class.
                    if(ClassManager.getClass(role.getName()) == null)
                        continue;

                    // If it is a class then we break and set the boolean to true.
                    hasClassRole = true;
                    break;
                }

                // If they don't have classes in their roles, then we tell them.
                if(!hasClassRole)
                {
                    privateChannel.sendMessage("You aren't in any classes and have nothing to clear!").queue();
                    return;
                }

                privateChannel.sendMessage("Are you sure you want to remove yourself from all your current classes? Re-type this command to confirm.").queue();

                // Set this confirmation to expire in 60 seconds.
                clearConfirmation.put(user.getIdLong(), System.currentTimeMillis() + 60000);
            }
            else
            {
                clearConfirmation.remove(user.getIdLong());

                // We need to make a new list of roles for the user that no longer contains their classes.
                ArrayList<Role> newRoles = new ArrayList<>();

                for(Role role : member.getRoles())
                {
                    if(ClassManager.isClassRole(role))
                        continue;

                    newRoles.add(role);
                }

                guild.modifyMemberRoles(member, newRoles).queue();
                privateChannel.sendMessage("You have removed yourself from all your classes.").queue();
            }
            return;
        }
        else if(args[0].equalsIgnoreCase("list"))
        {
            List<String> classNames = ClassManager.getClassNames();

            if(classNames.isEmpty())
            {
                privateChannel.sendMessage("We currently don't support any classes!").queue();
                return;
            }

            String initialText = "**We support the following classes**:\n";

            // Since Discord has a limit of 2000 characters per message, we potentially need to send multiple messages.
            // We add new classes to the String that will display the classes, and if it exceeds 2000, we send it and
            // start another message.
            int index = 0;

            while(index < classNames.size())
            {
                StringBuilder stringBuilder = new StringBuilder();

                if(index == 0)
                {
                    stringBuilder.append(initialText);
                }

                for(; index < classNames.size(); index++)
                {
                    // The text that is appended to the StringBuilder that will display all the classes.
                    // If this is the first iteration, then we don't need to add a comma and a space.
                    String append = ((stringBuilder.length() == initialText.length()) ? "" : ", ") + classNames.get(index);

                    // If the String goes above the 2000 character limit, then we don't append it and we just break and send.
                    // It will keep the same index since we are breaking out of it.
                    if((stringBuilder.length() + append.length()) >= 2000)
                        break;
                    else
                        stringBuilder.append(append);
                }

                privateChannel.sendMessage(stringBuilder.toString()).queue();
            }

            privateChannel.sendMessage("**NOTE: If you don't see a class on this list and want it to be added, type `/class request`**").queue();
            return;
        }
        else if(args[0].equalsIgnoreCase("request"))
        {
            startClassRequest(privateChannel, user);
            return;
        }

        if(args.length < 2)
        {
            sendUsage(privateChannel, label);
            return;
        }

        // The class id could span over multiple arguments so we join the Strings from the array from all
        // arguments past the first two.
        // We also manipulate the String to follow the correct format of class ids.
        String classId = String.join("_", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
        Class clazz = ClassManager.getClass(classId);

        if(args[0].equalsIgnoreCase("add"))
        {
            // If clazz is null, that means that we couldn't find a class with their inputted id.
            if(clazz == null)
            {
                privateChannel.sendMessage("This class doesn't exist! Use `/class list` to display all available classes.").queue();
                return;
            }

            // Now we need to find the correct role with the class id to give to them.
            for(Role role : guild.getRoles())
            {
                if(!role.getName().equalsIgnoreCase(clazz.getId()))
                    continue;

                guild.addRoleToMember(member, role).queue();
                privateChannel.sendMessage("You have added yourself to **" + clazz.getId() + "**!").queue();
                return;
            }

            // If we can't find the role, but the class id exists, then we haven't synced changes to Discord yet
            // and they need to wait.
            privateChannel.sendMessage("This class is still being integrated into the system, please try again in 10 minutes.").queue();
        }
        else if(args[0].equalsIgnoreCase("delete")
                    || args[0].equalsIgnoreCase("remove"))
        {
            // If clazz is null, that means that we couldn't find a class with their inputted id.
            if(clazz == null)
            {
                privateChannel.sendMessage("This class doesn't exist! Please check your roles to see what classes you're in.").queue();
                return;
            }

            // Find the class role they're trying to remove, and then remove it.
            for(Role role : member.getRoles())
            {
                if(!clazz.getId().equalsIgnoreCase(role.getName()))
                    continue;

                guild.removeRoleFromMember(member, role).queue();
                privateChannel.sendMessage("You have removed yourself from **" + clazz.getId() + "**!").queue();
                return;
            }

            // If we get to this point, we didn't find the class role, and they don't have it already.
            privateChannel.sendMessage("You are not in **" + clazz.getId() + "**.").queue();
        }
        else
        {
            sendUsage(privateChannel, label);
        }
    }

    public static void startClassRequest(PrivateChannel privateChannel, User user)
    {
        privateChannel.sendMessage("**Thank you for wanting to request a new class!**\n\n" +
                "Please enter the id of the class you wanted added! Examples: ics_31, sociol_2, econ_20b, math_2a.\n" +
                "Type 'cancel' to cancel.").queue();

        // We add the user to a map that will now check for their next response to be the class they are requesting.
        // This will expire in 5 minutes.
        classRequestMap.put(user.getIdLong(), System.currentTimeMillis() + 300000);
    }

    public static boolean classRequestResponse(Guild guild, User user, MessageReceivedEvent e)
    {
        // If they aren't in this map, then they aren't submitting a class request.
        if(!classRequestMap.containsKey(user.getIdLong()))
            return false;

        classRequestMap.remove(user.getIdLong());

        PrivateChannel privateChannel = e.getPrivateChannel();
        String id = e.getMessage().getContentRaw().replace(" ", "_").toLowerCase();

        // They can cancel the request by typing 'cancel'.
        if(id.equalsIgnoreCase("cancel"))
        {
            privateChannel.sendMessage("Canceled requesting a new class.").queue();
            return true;
        }

        // If the class already exists, cancel their request.
        if(ClassManager.getClass(id) != null)
        {
            privateChannel.sendMessage("This class already exists! Try adding it with `/class add " + id + "`!").queue();
            return true;
        }

        // The text channel where server administrators accept/deny requests.
        TextChannel textChannel = guild.getTextChannelById(Snowflake.TEXT_CHANNEL_CLASS_REQUESTS);

        // If we can't access this text channel, then we can't submit a request.
        if(textChannel == null)
        {
            privateChannel.sendMessage("There was a problem submitting your request, please contact an administrator.").queue();
            return true;
        }

        // If the class has already been requested, cancel their request.
        for(Message message : textChannel.getIterableHistory())
        {
            // If the message isn't an embed, we don't need to scan it.
            if(message == null
                    || message.getEmbeds().isEmpty())
                continue;

            MessageEmbed messageEmbed = message.getEmbeds().get(0);

            // The class request is in the footer, and if we can't get to it, it's not a valid request.
            if(messageEmbed == null
                    || messageEmbed.getFooter() == null
                    || messageEmbed.getFooter().getText() == null
                    || !id.equalsIgnoreCase(messageEmbed.getFooter().getText()))
                continue;

            privateChannel.sendMessage("This class has already been requested!").queue();

            return true;
        }

        EmbedBuilder embedBuilder = EmbedManager.createEmbedBuilder(Color.GREEN,
                true,
                "Class Request",
                "**Class Id**: " + id + "\n\n" +
                        "Submitted by " + user.getAsMention() + "\n\n" +
                        "**React with ✅ to accept WITHOUT a Voice Chat Channel.**\n" +
                        "**React with ☑ to accept WITH a Voice Chat Channel.**\n" +
                        "**React with ❌ to deny the request.**\n",
                null);

        embedBuilder.setAuthor(user.getId());
        embedBuilder.setFooter(id);

        Message message = textChannel.sendMessage(embedBuilder.build()).complete();

        message.addReaction("✅").queue();
        message.addReaction("☑").queue();
        message.addReaction("❌").queue();

        privateChannel.sendMessage("You have submitted your class request **" + id + "**. You will be notified if it is accepted. Thank you!").queue();

        return true;
    }

    public static void classRequestReact(User user, MessageReactionAddEvent e)
    {
        // If this reaction isn't in the class requests channel, return.
        if(e.getChannel().getIdLong() != Snowflake.TEXT_CHANNEL_CLASS_REQUESTS)
            return;

        Message message = e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete();

        // If this message doesn't exist or it doesn't contain embeds, then it's not a class request message.
        if(message == null
                || message.getEmbeds().size() < 1)
            return;

        MessageEmbed messageEmbed = message.getEmbeds().get(0);
        MessageEmbed.AuthorInfo authorInfo = messageEmbed.getAuthor();

        // The author info contains the person who submitted the request.
        // The footer contains the class they are requesting.
        // If we can't access either of them, we can't continue.
        if(authorInfo == null
                || authorInfo.getName() == null
                || messageEmbed.getFooter() == null
                || messageEmbed.getFooter().getText() == null)
            return;

        Member member = e.getGuild().retrieveMemberById(authorInfo.getName()).complete();
        String classId = messageEmbed.getFooter().getText();

        if(e.getReactionEmote().getEmoji().contains("✅")
                || e.getReactionEmote().getEmoji().contains("☑"))
        {
            // Add the new class to the database.
            ClassManager.addClass(new Class(classId, e.getReactionEmote().getEmoji().contains("☑"), UpdateAction.CREATE));

            // Update the database and discord now, but asynchronously.
            new Thread(() ->
            {
                ClassManager.syncToDatabase();
                ClassManager.syncToDiscord();
            }).start();

            // Let the person who accepted the class, know that it accepted successfully.
            user.openPrivateChannel().complete().sendMessage("You accepted the class **" + classId + "**.").queue();

            // Let the person who requested the class know that it has been accepted.
            if(member != null)
            {
                member.getUser().openPrivateChannel().complete().sendMessage("Your class request **" + classId + "** has been accepted!").queue();
            }
        }
        else if(member != null)
        {
            // Let the person who requested the class know that it has been denied.
            member.getUser().openPrivateChannel().complete().sendMessage("Your class request **" + classId + "** has been denied.").queue();
        }

        // Delete the class request message at the end since it's been dealt with.
        message.delete().queue();
    }

    public synchronized static void clearMemory()
    {
        // Remove all expired values from the clearConfirmation HashMap.
        for(long id : MemoryManager.getMemoryToRemove(clearConfirmation.entrySet(), object -> (long) object))
        {
            clearConfirmation.remove(id);
        }

        // Remove all expired values from the classRequestMap HashMap.
        for(long id : MemoryManager.getMemoryToRemove(classRequestMap.entrySet(), object -> (long) object))
        {
            classRequestMap.remove(id);
        }
    }

}