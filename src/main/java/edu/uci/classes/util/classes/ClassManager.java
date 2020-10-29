package edu.uci.classes.util.classes;

import edu.uci.classes.util.Snowflake;
import net.dv8tion.jda.api.entities.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author Qhucy
 * @author Discord: Qhucy#2279
 */
public class ClassManager
{

    private static Connection connection = null;

    private final static ArrayList<Class> classes = new ArrayList<>();

    public static boolean connectToDatabase()
    {
        // If we already have a connection, we don't need to make a new one.
        if(connection != null)
            return true;

        try
        {
            // Sometimes java doesn't recognize SQLite and this helps it.
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:data/database.db");

            try(Statement statement = connection.createStatement())
            {
                // Create a new database table for classes.
                statement.execute("CREATE TABLE IF NOT EXISTS classes (\n"
                        + "  id TEXT NOT NULL UNIQUE,\n"
                        + "  voice_chat INTEGER NOT NULL DEFAULT 0\n"
                        + ");");

                // Iterate through the database and add all classes to memory.
                try(ResultSet resultSet = statement.executeQuery("SELECT * FROM classes"))
                {
                    classes.clear();

                    while(resultSet.next())
                    {
                        classes.add(new Class(resultSet.getString("id"), resultSet.getBoolean("voice_chat"), DatabaseSyncOption.NONE));
                    }
                } catch(Exception exception)
                {
                    exception.printStackTrace();
                    return false;
                }
            } catch(Exception exception)
            {
                exception.printStackTrace();
                return false;
            }

            System.out.println("Connected to the SQLite Database.");
            return true;
        } catch(ClassNotFoundException e)
        {
            System.out.println("Java SQLite libraries not found, unable to connect to database.");
            e.printStackTrace();
        } catch(SQLException e)
        {
            System.out.println("Unable to find and connect to SQLite Database.");
            e.printStackTrace();
        }

        return false;
    }

    // Execute a statement into the SQL Database.
    public static void executeStatement(String sqlStatement)
    {
        try(Statement statement = connection.createStatement())
        {
            statement.execute(sqlStatement);
        } catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    // Sync potential changes from classe data to the database.
    public static void syncToDatabase()
    {
        try(Statement statement = connection.createStatement())
        {
            // We create a new array with the values of the current version of classes
            // so if we have to remove a value from the original array, we can keep iterating.
            ArrayList<Class> tempClasses = new ArrayList<>(classes);

            for(Class clazz : tempClasses)
            {
                // If we don't have to update the this class, go to the next one.
                if(clazz.getUpdateAction() == DatabaseSyncOption.NONE)
                    continue;

                if(clazz.getUpdateAction() == DatabaseSyncOption.UPDATE)
                {
                    statement.executeUpdate("UPDATE classes SET "
                            + " voice_chat=" + (clazz.isVoiceChat() ? 1 : 0) + ""
                            + " WHERE id='" + clazz.getId() + "'");
                }
                else if(clazz.getUpdateAction() == DatabaseSyncOption.CREATE)
                {
                    statement.executeUpdate("INSERT INTO classes"
                            + "(id,voice_chat) "
                            + "VALUES('" + clazz.getId() + "'," + (clazz.isVoiceChat() ? 1 : 0) + ")");
                }
                else
                {
                    statement.executeUpdate("DELETE FROM classes WHERE id='" + clazz.getId() + "'");
                    classes.remove(clazz);
                }

                clazz.setUpdateAction(DatabaseSyncOption.NONE);
            }
        } catch(SQLException exception)
        {
            System.out.println("Failed to sync to SQLite Database.");
            exception.printStackTrace();
        }
    }

    // Sync classes in database to classes in the Discord server.
    public static void syncToDiscord()
    {
        Guild guild = Snowflake.getGuild();
        Category classChats = guild.getCategoryById(Snowflake.CATEGORY_CLASS_CHATS);

        for(Class clazz : classes)
        {
            // Scan to see if the class Role exists in the guild, if it does, then break.
            Role currentClassRole = null;
            boolean containsValue = false;

            for(Role role : guild.getRoles())
            {
                if(!role.getName().equalsIgnoreCase(clazz.getId()))
                    continue;

                containsValue = true;
                currentClassRole = role;
                break;
            }

            // If the class Role doesn't exist in the guild, then create it.
            if(!containsValue)
            {
                currentClassRole = guild.createRole().setName(clazz.getId()).setPermissions(0L).complete();
            }

            // If we can't access the class chats category, we can't create new class channels.
            if(classChats == null)
                continue;

            // Scan to see if the Text Channel exists, if it does, then break.
            containsValue = false;

            for(TextChannel textChannel : classChats.getTextChannels())
            {
                if(!textChannel.getName().equalsIgnoreCase(clazz.getId()))
                    continue;

                containsValue = true;
                break;
            }

            // If the Text Channel doesn't exist, then create it.
            if(!containsValue)
            {
                classChats.createTextChannel(clazz.getId())
                        .setName(clazz.getId())
                        .setTopic("Class chat for " + clazz.getId() + ".")
                        .addPermissionOverride(guild.getPublicRole(), 0L, 523328L)
                        .addPermissionOverride(currentClassRole, 379968L, 0L).queue();
            }

            if(!clazz.isVoiceChat())
            {
                // If the Voice Channel exists, then delete it since it's disabled.
                for(VoiceChannel voiceChannel : classChats.getVoiceChannels())
                {
                    if(!voiceChannel.getName().equalsIgnoreCase(clazz.getId()))
                        continue;

                    voiceChannel.delete().queue();
                    break;
                }

                continue;
            }

            // Scan to see if the Voice Channel exists, if it does, then break.
            containsValue = false;

            for(VoiceChannel voiceChannel : classChats.getVoiceChannels())
            {
                if(!voiceChannel.getName().equalsIgnoreCase(clazz.getId()))
                    continue;

                containsValue = true;
                break;
            }

            // If the Voice Channel doesn't exist, then create it.
            if(!containsValue)
            {
                classChats.createVoiceChannel(clazz.getId())
                        .setName(clazz.getId())
                        .addPermissionOverride(guild.getPublicRole(), 0L, 66061568L)
                        .addPermissionOverride(currentClassRole, 36701184L, 0L).queue();
            }
        }
    }

    public synchronized static void addClass(Class clazz)
    {
        classes.add(clazz);
    }

    public synchronized static void removeClass(String classId)
    {
        // Set the UpdateAction of the current class to delete so it will be deleted by the database next sync.
        for(Class clazz : classes)
        {
            if(!clazz.getId().equalsIgnoreCase(classId))
                continue;

            clazz.setUpdateAction(DatabaseSyncOption.DELETE);
            break;
        }

        Guild guild = Snowflake.getGuild();
        Category classChats = guild.getCategoryById(Snowflake.CATEGORY_CLASS_CHATS);

        if(classChats != null)
        {
            // Delete the class' Text Channel if it exists.
            for(TextChannel textChannel : classChats.getTextChannels())
            {
                if(!textChannel.getName().equalsIgnoreCase(classId))
                    continue;

                textChannel.delete().queue();
                break;
            }

            // Delete the class' Voice Channel if it exists.
            for(VoiceChannel voiceChannel : classChats.getVoiceChannels())
            {
                if(!voiceChannel.getName().equalsIgnoreCase(classId))
                    continue;

                voiceChannel.delete().queue();
                break;
            }
        }

        // Delete the class' Role if it exists.
        for(Role role : guild.getRoles())
        {
            if(!role.getName().equalsIgnoreCase(classId))
                continue;

            role.delete().queue();
            return;
        }
    }

    // Return the class given its id.
    public synchronized static Class getClass(String classId)
    {
        // Set the classId to the correct format.
        classId = classId.toLowerCase().replace(" ", "_");

        for(Class clazz : classes)
        {
            if(!clazz.getId().equalsIgnoreCase(classId))
                continue;

            return clazz;
        }

        return null;
    }

    // Check whether a given role is a class role.
    public synchronized static boolean isClassRole(Role role)
    {
        for(Class clazz : classes)
        {
            if(clazz.getId().equalsIgnoreCase(role.getName()))
            {
                return true;
            }
        }
        return false;
    }

    // Get the list of class ids.
    public synchronized static List<String> getClassNames()
    {
        ArrayList<String> classNames = new ArrayList<>();

        for(Class clazz : classes)
        {
            classNames.add(clazz.getId());
        }

        return classNames;
    }

}