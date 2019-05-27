package net.runelite.client.plugins.worldhealth;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("worldhealth")
public interface WorldHealthConfig extends Config
{
    @ConfigItem(
            position = 1,
            keyName = "showWorldNumber",
            name = "Show world number",
            description = "Toggle display of the current world number"
    )
    default boolean showWorldNumber()
    {
        return true;
    }

    @ConfigItem(
            position = 2,
            keyName = "showTickCount",
            name = "Show tick count",
            description = "Toggle display of the tick counter"
    )
    default boolean showTickCount()
    {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "showTickLast",
            name = "Show last tick",
            description = "Toggle display of the last tick time"
    )
    default boolean showTickLast()
    {
        return false;
    }

    @ConfigItem(
            position = 4,
            keyName = "showTickStdDev",
            name = "Show standard deviation",
            description = "Toggle display of tick time standard deviation"
    )
    default boolean showTickStdDev()
    {
        return false;
    }

    @ConfigItem(
            position = 5,
            keyName = "tickStdDevCache",
            name = "Std dev cache",
            description = "Number of recent ticks used to calculate standard deviation"
    )
    default int tickStdDevCache()
    {
        return 30;
    }
}
