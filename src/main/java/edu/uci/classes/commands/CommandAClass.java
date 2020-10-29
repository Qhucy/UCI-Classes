package edu.uci.classes.commands;

import edu.irvine.classes.classes.Class;
import edu.irvine.classes.classes.ClassManager;
import edu.irvine.classes.classes.UpdateAction;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class CommandAClass extends Command
{

    public CommandAClass()
    {
        super(CommandType.DM_ONLY, new String[] {"**/{label} add [id] [vc]**: Manually add a new class to the database. Put true for vc to have a voice chat.",
                                                 "**/{label} remove [id]**: Manually remove a class from the database.",
                                                 "**/{label} edit [id] [newId]**: Change the id of a class to a new one.",
                                                 "**/{label} vc**: Change whether or not a class will have a Voice Chat Channel."}, "aclass", "adminclass", "acourse", "admincourse");
    }

    public void onCommand(MessageReceivedEvent e, User user, Member member, Guild guild, String label, String[] args)
    {
        // Only Discord Administrators can use this command.
        if(!member.hasPermission(Permission.ADMINISTRATOR))
            return;

        PrivateChannel privateChannel = user.openPrivateChannel().complete();

        if(args.length < 2)
        {
            super.sendUsage(privateChannel, label);
            return;
        }

        // Since the class id can span multiple arguments, we join them past the first two to get the inputted
        // class id.
        // We also manipulate the id to be in the correct format.
        String classId = args[1].toLowerCase();
        Class clazz = ClassManager.getClass(classId);

        if(args[0].equalsIgnoreCase("add")
                || args[0].equalsIgnoreCase("create"))
        {
            ClassManager.addClass(new Class(classId, args.length >= 3 && args[2].equalsIgnoreCase("true"), UpdateAction.CREATE));

            privateChannel.sendMessage("Added the class **" + classId + "**.").queue();

            // We instantly sync the changes to the database and Discord on an asynchronous thread.
            syncChanges();
        }
        else if(args[0].equalsIgnoreCase("remove")
                    || args[0].equalsIgnoreCase("delete"))
        {
            // If clazz is null, then it already doesn't exist.
            if(clazz == null)
            {
                privateChannel.sendMessage("This class doesn't exist.").queue();
                return;
            }

            ClassManager.removeClass(classId);

            privateChannel.sendMessage("Removed the class **" + classId + "**.").queue();

            // We instantly sync the changes to the database and Discord on an asynchronous thread.
            syncChanges();
        }
        else if(args[0].equalsIgnoreCase("vc")
                    || args[0].equalsIgnoreCase("voicechat"))
        {
            // If clazz is null, then it doesn't exist.
            if(clazz == null)
            {
                privateChannel.sendMessage("This class doesn't exist.").queue();
                return;
            }

            clazz.setVoiceChat(!clazz.isVoiceChat());

            if(clazz.isVoiceChat())
            {
                privateChannel.sendMessage("**" + classId + "** now has a Voice Channel.").queue();
            }
            else
            {
                privateChannel.sendMessage("**" + classId + "** no longer has a Voice Channel.").queue();
            }

            // We instantly sync the changes to the database and Discord on an asynchronous thread.
            syncChanges();
        }

        if(args.length < 3)
        {
            super.sendUsage(privateChannel, label);
            return;
        }

        if(args[0].equalsIgnoreCase("edit"))
        {
            String oldId = args[1].toLowerCase();
            String newId = args[2].toLowerCase();

            clazz = ClassManager.getClass(oldId);

            // If this is null, then the class doesn't exist.
            if(clazz == null)
            {
                privateChannel.sendMessage("This class id doesn't exist!").queue();
                return;
            }

            // If a class exists with the new id, then we can't change the old one to it.
            if(ClassManager.getClass(newId) != null)
            {
                privateChannel.sendMessage("You can't change this class to id **" + newId + "** because it already exists.").queue();
                return;
            }

            clazz.setId(newId);

            privateChannel.sendMessage("You changed the id of **" + oldId + "** to **" + newId + "**.").queue();
            return;
        }

        super.sendUsage(privateChannel, label);
    }

    private void syncChanges()
    {
        new Thread(() ->
        {
            ClassManager.syncToDatabase();
            ClassManager.syncToDiscord();
        }).start();
    }

}