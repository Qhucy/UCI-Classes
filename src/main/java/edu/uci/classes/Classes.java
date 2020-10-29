package edu.uci.classes;

import edu.uci.classes.commands.*;
import edu.uci.classes.events.EventHandler;
import edu.uci.classes.util.SchedulerManager;
import edu.uci.classes.util.classes.ClassManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.Scanner;

/**
 * Main class for the project: handles initial setup and connection of the bot.
 *
 * @author Qhucy
 * @author Discord: Qhucy#2279
 */
public class Classes
{

    private static JDA jdaBot;

    /**
     * Main method of the project: handles initial setup and connection of the bot.
     *
     * @param arguments The startup arguments for the program. Can't be null.
     */
    public static void main( @Nonnull final String[] arguments )
    {
        // Retrieve the Discord Bot Token from data/token.txt.
        File dataFolder = new File( "data" );
        File tokenFile = new File( "data/token.txt" );

        if ( (!dataFolder.exists() && !dataFolder.mkdir() )
                || (!tokenFile.exists()) )
        {
            System.out.println( "Unable to find data/token.txt." );

            System.exit( 0 );
            return;
        }

        if ( !ClassManager.connectToDatabase() )
        {
            System.exit( 0 );
            return;
        }

        try
        {
            BufferedReader bufferedReader = new BufferedReader( new FileReader( tokenFile ) );

            // The Discord Bot Token is on the first line of the file.
            jdaBot = JDABuilder.create( bufferedReader.readLine(),
                    GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.GUILD_INVITES,
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_VOICE_STATES,
                    GatewayIntent.GUILD_EMOJIS ).build();
        } catch ( IOException ioException )
        {
            System.out.println( "Unable to retrieve the Discord Bot Token from data/token.txt.");
            ioException.printStackTrace();

            System.exit( 0 );
            return;
        } catch ( LoginException loginException )
        {
            System.out.println( "Unable to connect to Discord servers." );
            loginException.printStackTrace();

            System.exit( 0 );
            return;
        }

        jdaBot.getPresence().setPresence( Activity.playing( "Class of 2024" ), true );

        jdaBot.addEventListener( new EventHandler() );

        Command.addCommand( new CommandHelp() );
        Command.addCommand( new CommandNickname() );
        Command.addCommand( new CommandClass() );
        Command.addCommand( new CommandAClass() );

        SchedulerManager.startScheduler();

        // Allows for console to interpret commands.
        Scanner scanner = new Scanner( System.in );
        String input;

        do
        {
            System.out.println( "Type 'stop' to stop the Discord Bot safely." );

            input = scanner.nextLine();
        } while ( !input.equalsIgnoreCase( "stop" )
                        && !input.equalsIgnoreCase( "exit" ) );

        System.out.println( "Saving data and exiting." );

        SchedulerManager.stopScheduler();
        jdaBot.shutdown();

        ClassManager.syncToDatabase();

        System.exit( 0 );
    }

    /**
     * @return The Discord JDA Object for the running bot.
     */
    public static JDA getJdaBot()
    {
        return jdaBot;
    }

}
