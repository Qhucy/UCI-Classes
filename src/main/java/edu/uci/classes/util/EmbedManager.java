package edu.uci.classes.util;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class EmbedManager
{

    public static EmbedBuilder createEmbedBuilder(Color color, boolean titled, String title, String description, String footer)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if(color != null)
        {
            embedBuilder.setColor(color);
        }

        if(title != null)
        {
            embedBuilder.setTitle((titled ? "——————————————————————\n" : "") +
                    title +
                    (titled ? "\n——————————————————————" : ""));
        }

        if(description != null)
        {
            embedBuilder.setDescription(description);
        }

        if(footer != null)
        {
            embedBuilder.setFooter(footer);
        }

        return embedBuilder;
    }

}