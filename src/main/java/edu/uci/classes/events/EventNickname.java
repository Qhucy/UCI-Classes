package edu.uci.classes.events;

import edu.irvine.classes.util.EmbedManager;
import edu.irvine.classes.util.Snowflake;
import edu.irvine.classes.util.memory.MemoryManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.awt.*;
import java.util.HashMap;

public class EventNickname
{

    private static class NicknameSetup
    {

        private String name = null;
        private String major = null;

        private long messageId;
        private final long destructionTime = System.currentTimeMillis() + 1800000;

        private NicknameSetup()
        {

        }

        private String getName()
        {
            return name;
        }

        private void setName(String name)
        {
            this.name = name;
        }

        private String getMajor()
        {
            return major;
        }

        private void setMajor(String major)
        {
            this.major = major;
        }

        private long getMessageId()
        {
            return messageId;
        }

        private void setMessageId(long messageId)
        {
            this.messageId = messageId;
        }

        private long getDestructionTime()
        {
            return destructionTime;
        }

    }

    // Map to keep track of who is currently setting up their nickname.
    // Key is the user's Discord Id.
    // Value is where all their nickname setup progress is stored inside of.
    private static final HashMap<Long, NicknameSetup> nicknameSetupMap = new HashMap<>();

    public static void setupNickname(User user)
    {
        PrivateChannel privateChannel = user.openPrivateChannel().complete();

        privateChannel.sendMessage("**Welcome to the UCI Freshmxn Classes Discord!**" +
                "\n\nTo help to get to know everyone, we require everyone to display their name and major.\nPlease enter your first and last name in one message below and we will set it up for you!").queue();

        nicknameSetupMap.put(user.getIdLong(), new NicknameSetup());
    }

    public static boolean onSetupMemberResponse(User user, MessageReceivedEvent e)
    {
        // If they aren't in this map, then they aren't setting up a nickname.
        if(!nicknameSetupMap.containsKey(user.getIdLong()))
            return false;

        PrivateChannel privateChannel = e.getPrivateChannel();
        String content = e.getMessage().getContentRaw();

        // If their name or major contains '|' it will mess up our formatting.
        if(content.contains("|"))
        {
            privateChannel.sendMessage("You can't use the '|' character.").queue();
            return true;
        }

        NicknameSetup nicknameSetup = nicknameSetupMap.get(user.getIdLong());

        // If their nickname setup doesn't have a name yet, then they are responding with their name.
        // Otherwise they are responding with their major.
        if(nicknameSetup.getName() == null)
        {
            nicknameSetup.setName(firstUpperCaseString(content));

            privateChannel.sendMessage("**Your name has been set!**\n\n" +
                    "Now, please enter your declared major at UCI. If you are undeclared, you can just write 'Undeclared'.\n" +
                    "NOTE: Try to use abbreviations when possible (e.g. CGS instead of Computer Game Science).").queue();
        }
        else
        {
            nicknameSetup.setMajor(firstUpperCaseString(content));

            Message message = privateChannel.sendMessage(EmbedManager.createEmbedBuilder(Color.GREEN,
                    true,
                    "UCI Freshmxn Information",
                    "**React with ✅ to confirm, 🔄 to restart, or ❌ to cancel.**\n\n" +
                            "**Name:** " + nicknameSetup.getName() + "\n" +
                            "**Major:** " + nicknameSetup.getMajor(),
                    null).build()).complete();

            message.addReaction("✅").queue();
            message.addReaction("🔄").queue();
            message.addReaction("❌").queue();

            nicknameSetup.setMessageId(message.getIdLong());
        }

        return true;
    }

    public static void onSetupMemberReact(User user, MessageReactionAddEvent e)
    {
        // If they aren't in this map, they aren't setting up a nickname.
        // Also check to make sure they are reacting to the correct message.
        if(!nicknameSetupMap.containsKey(user.getIdLong())
                || e.getMessageIdLong() != nicknameSetupMap.get(user.getIdLong()).getMessageId())
            return;

        PrivateChannel privateChannel = e.getPrivateChannel();
        NicknameSetup nicknameSetup = nicknameSetupMap.get(user.getIdLong());

        if(e.getReactionEmote().getEmoji().contains("✅"))
        {
            Guild guild = Snowflake.getGuild();

            // If we can't access the guild, we can't change their nickname.
            if(guild == null)
            {
                privateChannel.sendMessage("There was an error, please try again or contact an administrator.").queue();
                return;
            }

            Member member = guild.retrieveMember(user).complete();

            // If this is null, then they aren't a member of the server.
            if(member == null)
            {
                privateChannel.sendMessage("You have left the server: unable to continue!").queue();
                return;
            }

            String major = nicknameSetup.getMajor();
            // The format of their nickname is 'Name | Major'.
            String name = nicknameSetup.getName() + " | " + major;

            // Discord's maximum nickname length is 32 characters, so if their combined result is greater,
            // then we need to attempt and shorten it.
            if(name.length() > 32)
            {
                if(major.contains("Science"))
                    major = major.replace("Science", "Sci");
                if(major.contains("Computer"))
                    major = major.replace("Computer", "Comp");
                if(major.contains("Biological"))
                    major = major.replace("Biological", "Bio");
                if(major.contains("Biology"))
                    major = major.replace("Biology", "Bio");
                if(major.contains("Economics"))
                    major = major.replace("Economics", "Econ");
                if(major.contains("Engineering"))
                    major = major.replace("Engineering", "Engin");
                if(major.contains("English"))
                    major = major.replace("English", "Eng");
                if(major.contains("Electrical"))
                    major = major.replace("Electrical", "Elec");
                if(major.contains("Environmental"))
                    major = major.replace("Environmental", "Envir");
                if(major.contains("Psychology"))
                    major = major.replace("Psychology", "Psych");
                if(major.contains("Psychological"))
                    major = major.replace("Psychological", "Psych");
                if(major.contains("Chemistry"))
                    major = major.replace("Chemistry", "Chem");
                if(major.contains("Pharmaceutical"))
                    major = major.replace("Pharmaceutical", "Pharm");
                if(major.contains("Biomedical"))
                    major = major.replace("Biomedical", "Biomed");
                if(major.contains("Mathematics"))
                    major = major.replace("Mathematics", "Math");
                if(major.contains("Mathematical"))
                    major = major.replace("Mathematical", "Math");
                if(major.contains("Mechanical"))
                    major = major.replace("Mechanical", "Mech");
                if(major.contains("Business"))
                    major = major.replace("Business", "Bus");
                if(major.contains("Political"))
                    major = major.replace("Political", "Poli");

                name = nicknameSetup.getName() + " | " + major;

                // If their combined name is still over 32 characters, then we just cut off the last characters going over.
                if(name.length() > 32)
                {
                    name = name.substring(0, 31);
                }
            }

            guild.modifyNickname(member, name).queue();
            privateChannel.sendMessage("Your nickname has been setup! Thank you!\n\n" +
                    "**To learn about adding classes, please enter `/class`!**").queue();
            nicknameSetupMap.remove(user.getIdLong());
        }
        else if(e.getReactionEmote().getEmoji().contains("🔄"))
        {
            nicknameSetup.setName(null);
            nicknameSetup.setMajor(null);
            nicknameSetup.setMessageId(0);

            privateChannel.sendMessage("**Welcome to the UCI Freshmxn Classes Discord!**" +
                    "\n\nTo help to get to know everyone, we require everyone to display their name and major in their Discord.\nPlease enter your first and last name in one message below and we will set it up for you!").queue();
        }
        else
        {
            nicknameSetupMap.remove(user.getIdLong());

            privateChannel.sendMessage("You have canceled setting up your nickname.").queue();
        }
    }

    // Converts a string to have the first letter of each word uppercase and the rest lowercase.
    private static String firstUpperCaseString(String text)
    {
        String[] splitString = text.split(" ");
        StringBuilder stringBuilder = new StringBuilder();

        for(String str : splitString)
        {
            if(str.isEmpty())
                continue;

            if(str.length() < 2)
            {
                stringBuilder.append(str.toUpperCase());
                continue;
            }

            String firstUppercase = String.valueOf(str.charAt(0)).toUpperCase();

            if(stringBuilder.length() == 0)
                stringBuilder.append(firstUppercase).append(str.substring(1).toLowerCase());
            else
                stringBuilder.append(" ").append(firstUppercase).append(str.substring(1).toLowerCase());
        }

        return stringBuilder.toString();
    }

    public synchronized static void clearMemory()
    {
        for(long id : MemoryManager.getMemoryToRemove(nicknameSetupMap.entrySet(), object -> ((NicknameSetup) object).getDestructionTime()))
        {
            nicknameSetupMap.remove(id);
        }
    }

}