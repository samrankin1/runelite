package net.runelite.client.plugins.worldhealth;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class WorldHealthOverlay extends Overlay
{
    private final Client client;
    private final WorldHealthPlugin plugin;
    private final WorldHealthConfig config;
    private final PanelComponent panel = new PanelComponent();

    @Inject
    private WorldHealthOverlay(Client client, WorldHealthPlugin plugin, WorldHealthConfig config)
    {
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    private String renderTickCount()
    {
        return Integer.toString(plugin.getTickCount());
    }

    private String renderTickLast()
    {
        if (plugin.getTickCount() < 2)
        {
            return "N/A";
        }
        return plugin.getTickLastMillis() + " ms";
    }

    private Color colorTickLast()
    {
        if (plugin.getTickCount() < 2)
        {
            return Color.WHITE;
        }
        return colorTickTime(plugin.getTickLastMillis());
    }

    private String renderTickRate()
    {
        if (plugin.getTickCount() < 2)
        {
            return "N/A";
        }
        return String.format("%.1f ms", plugin.getTickAvgMillis());
    }

    private Color colorTickRate()
    {
        if (plugin.getTickCount() < 2)
        {
            return Color.WHITE;
        }
        return colorTickTime((int) plugin.getTickAvgMillis());
    }

    private String renderTickStdDev()
    {
        if (plugin.getTickCount() < 2)
        {
            return "N/A";
        }
        return String.format("%.2f ms", plugin.getTickStdDevMillis());
    }

    private static Color colorTickTime(int tickMs) {
        if (tickMs <= 605)
        {
            return Color.GREEN;
        }
        else if (tickMs <= 650)
        {
            return Color.YELLOW;
        }
        else if (tickMs <= 700)
        {
            return Color.ORANGE;
        }
        else
        {
            return Color.RED;
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panel.getChildren().clear();

        String overlayTitle = "World Health";
        if (config.showWorldNumber())
        {
            overlayTitle += " (" + client.getWorld() + ")";
        }

        panel.getChildren().add(TitleComponent.builder()
            .text(overlayTitle)
            .color(new Color(200, 200 , 200))
            .build()
        );

        panel.setPreferredSize(new Dimension(
            graphics.getFontMetrics().stringWidth(overlayTitle + 50),
            0
        ));

        if (config.showTickCount())
        {
            panel.getChildren().add(LineComponent.builder()
                .left("Ticks:")
                .right(renderTickCount())
                .build()
            );
        }

        if (config.showTickLast())
        {
            panel.getChildren().add(LineComponent.builder()
                .left("Last:")
                .right(renderTickLast())
                .rightColor(colorTickLast())
                .build()
            );
        }

        panel.getChildren().add(LineComponent.builder()
            .left("Rate:")
            .right(renderTickRate())
            .rightColor(colorTickRate())
            .build()
        );

        if (config.showTickStdDev())
        {
            panel.getChildren().add(LineComponent.builder()
                .left("σ(n=" + plugin.getStdDevCacheSize() + "):")
                .right(renderTickStdDev())
                .build()
            );
        }

        return panel.render(graphics);
    }
}
