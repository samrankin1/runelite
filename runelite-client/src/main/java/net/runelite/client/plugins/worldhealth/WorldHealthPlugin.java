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
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@PluginDescriptor(
        name = "World Health",
        description = "Measures the tick rate of worlds to determine server health",
        tags = {"skilling", "tick", "timers"},
        enabledByDefault = false
)
public class WorldHealthPlugin extends Plugin
{
    private static final long LOGIN_DELAY_MS = 1200;

    private boolean ready = true;
    private Instant loginTime;

    private int tickCount;
    private int tickTotalMs;

    private Instant lastTick;
    private int lastTickMs;

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
        lastTick = null;
    }

    private void start() {
        ready = false;
        loginTime = Instant.now();
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
        if (getLoginMillis() < 0) return;

        if (lastTick != null)
        {
            lastTickMs = (int) ChronoUnit.MILLIS.between(lastTick, Instant.now());
            tickTotalMs += lastTickMs;
        }

        lastTick = Instant.now();
        tickCount++;
    }

    private long getLoginMillis()
    {
        if (loginTime == null)
        {
            return -1;
        }
        return ChronoUnit.MILLIS.between(loginTime, Instant.now()) - LOGIN_DELAY_MS;
    }

    protected int getTickCount()
    {
        return tickCount;
    }

    protected double getAvgTickMillis()
    {
        return tickTotalMs / (double) (tickCount - 1);
    }

    protected int getLastTickMillis()
    {
        return lastTickMs;
    }
}
