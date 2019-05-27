package net.runelite.client.plugins.worldhealth;

import com.google.inject.Provides;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

@PluginDescriptor(
        name = "World Health",
        description = "Measures the tick rate of worlds to determine server health",
        tags = {"skilling", "tick", "timers"},
        enabledByDefault = false
)
public class WorldHealthPlugin extends Plugin
{
    private static final Duration STARTUP_DELAY = Duration.ofMillis(1200);

    private boolean ready = true;
    private Instant startupTime;

    private int tickCount;
    private int tickTotalMs;
    private double tickAvgMs;

    private Instant tickLast;
    private int tickLastMs;

    private final List<Integer> tickCache = new LinkedList<>();
    private double tickStdDev;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private WorldHealthOverlay overlay;

    @Inject
    private WorldHealthConfig config;

    @Provides
    WorldHealthConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(WorldHealthConfig.class);
    }

    private void resetState() {
        tickCount = 0;
        tickTotalMs = 0;
        tickLast = null;
        tickCache.clear();
    }

    private void start() {
        ready = false;
        startupTime = Instant.now().plus(STARTUP_DELAY);
        resetState();
    }

    @Override
    protected void startUp() throws Exception {
        if (ready) start();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        ready = true;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        switch (event.getGameState())
        {
            case LOGGING_IN:
            case HOPPING:
            case CONNECTION_LOST:
                ready = true;
                break;
            case LOGGED_IN:
                if (ready) start();
                break;
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        final Instant now = Instant.now();
        if (startupTime.isAfter(now)) return;

        tickCount++;

        if (tickLast != null)
        {
            tickLastMs = (int) ChronoUnit.MILLIS.between(tickLast, now);
            updateAvg();
            updateStdDev();
        }

        tickLast = now;
    }

    private void updateAvg()
    {
        tickTotalMs += tickLastMs;
        tickAvgMs = tickTotalMs / (double) (tickCount - 1);
    }

    private void updateStdDev()
    {
        tickCache.add(0, tickLastMs);
        while (tickCache.size() > config.tickStdDevCache())
        {
            tickCache.remove(tickCache.size() - 1);
        }

        int tickSum = 0;
        for (Integer tick : tickCache)
        {
            tickSum += tick;
        }

        double tickAvg = tickSum / (double) tickCache.size();

        double errorSqSum = 0;
        for (Integer tick : tickCache)
        {
            errorSqSum += Math.pow(tick - tickAvg, 2.0);
        }

        double variance = errorSqSum / (double) tickCache.size();
        tickStdDev = Math.sqrt(variance);
    }

    protected int getTickCount()
    {
        return tickCount;
    }

    protected double getTickAvgMillis()
    {
        return tickAvgMs;
    }

    protected int getTickLastMillis()
    {
        return tickLastMs;
    }

    protected int getStdDevCacheSize()
    {
        return tickCache.size();
    }

    protected double getTickStdDevMillis()
    {
        return tickStdDev;
    }
}
