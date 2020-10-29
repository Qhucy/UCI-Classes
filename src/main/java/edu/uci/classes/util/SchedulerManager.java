package edu.uci.classes.util;

import edu.irvine.classes.Classes;
import edu.irvine.classes.commands.CommandClass;
import edu.irvine.classes.classes.ClassManager;
import edu.irvine.classes.events.EventNickname;
import net.dv8tion.jda.api.entities.Activity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerManager
{

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startScheduler()
    {
        scheduler.scheduleWithFixedDelay(syncData, 10, 1800, TimeUnit.SECONDS);
    }

    public static void stopScheduler()
    {
        scheduler.shutdown();
    }

    private static final Runnable syncData = () ->
    {
        // The status message of the Bot can disappear after some time, so we reset it every once in a while.
        Classes.getJdaBot().getPresence().setPresence(Activity.playing("Class of 2024"), true);

        // Clear any memory that needs to be cleared.
        EventNickname.clearMemory();
        CommandClass.clearMemory();

        // Sync the database and Discord server on an asynchronous thread.
        new Thread(() ->
        {
            ClassManager.syncToDatabase();
            ClassManager.syncToDiscord();
        }).start();
    };

}