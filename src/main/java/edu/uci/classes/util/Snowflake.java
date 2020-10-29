package edu.uci.classes.util;

import edu.irvine.classes.Classes;
import net.dv8tion.jda.api.entities.Guild;

public class Snowflake
{

    public final static long GUILD_ID = 760605648445505556L;

    public final static long CATEGORY_CLASS_CHATS = 760605649133633615L;

    public final static long TEXT_CHANNEL_CLASS_REQUESTS = 760605652426293251L;

    public final static long MESSAGE_CLASS_SIGNUP = 764272180343078962L;
    public final static long MESSAGE_CLASS_REQUEST = 764743521286094868L;
    public final static long MESSAGE_NICKNAME = 764807106750054430L;

    public static Guild getGuild()
    {
        return Classes.getJdaBot().getGuildById(GUILD_ID);
    }

}