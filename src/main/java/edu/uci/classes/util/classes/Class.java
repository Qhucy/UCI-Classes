package edu.uci.classes.util.classes;

import net.dv8tion.jda.api.entities.*;

/**
 * Object that holds all basic information for a class.
 *
 * @author Qhucy
 * @author Discord: Qhucy#2279
 */
public class Class
{

    private String classId;
    private boolean hasVoiceChat;

    private DatabaseSyncOption databaseSyncOption;

    public Class(String id, boolean voiceChat, DatabaseSyncOption updateAction)
    {
        this.classId = id;
        this.hasVoiceChat = voiceChat;

        this.updateAction = updateAction;
    }

    public String getId()
    {
        return classId;
    }

    public void setId(String id)
    {
        Guild guild = Snowflake.getGuild();

        // If we can't access the guild, we can't edit anything on Discord.
        if(guild == null)
            return;

        Category classChats = guild.getCategoryById(Snowflake.CATEGORY_CLASS_CHATS);

        // If we can't access the class chats category, then we can't edit any channels.
        if(classChats == null)
            return;

        // Change the Role name to the new id.
        for(Role role : guild.getRoles())
        {
            if(!role.getName().equalsIgnoreCase(this.classId))
                continue;

            role.getManager().setName(id).queue();
        }

        // Change the Text Channel name to the new id.
        for(TextChannel textChannel : classChats.getTextChannels())
        {
            if(!textChannel.getName().equalsIgnoreCase(this.classId))
                continue;

            textChannel.getManager().setName(id).queue();
        }

        // Change the Voice Channel name to the new id.
        for(VoiceChannel voiceChannel : classChats.getVoiceChannels())
        {
            if(!voiceChannel.getName().equalsIgnoreCase(this.classId))
                continue;

            voiceChannel.getManager().setName(id).queue();
        }

        // If the UpdateAction is to create it already, then there is nothing to update in the current database.
        // Otherwise, we do need to edit the id to the new one.
        if(updateAction != DatabaseSyncOption.CREATE)
        {
            ClassManager.executeStatement("UPDATE classes SET id='" + id + "' WHERE id='" + this.classId + "'");
        }

        this.classId = id;
    }

    public boolean isVoiceChat()
    {
        return hasVoiceChat;
    }

    public void setVoiceChat(boolean voiceChat)
    {
        changeUpdateAction();

        this.hasVoiceChat = voiceChat;
    }

    public DatabaseSyncOption getUpdateAction()
    {
        return updateAction;
    }

    public void setUpdateAction(DatabaseSyncOption updateAction)
    {
        this.updateAction = updateAction;
    }

    private void changeUpdateAction()
    {
        if(updateAction != DatabaseSyncOption.NONE)
            return;

        updateAction = DatabaseSyncOption.UPDATE;
    }

}