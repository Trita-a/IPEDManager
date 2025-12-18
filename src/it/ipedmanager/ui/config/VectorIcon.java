package it.ipedmanager.ui.config;

import java.awt.*;
import javax.swing.Icon;

public class VectorIcon implements Icon {
    private final String name;
    private final int size;
    private Color color;
    private double rotation = 0;
    private double scale = 1.0;

    public VectorIcon(String name, int size, Color color) {
        this.name = name;
        this.size = size;
        this.color = color;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);

        // Apply scaling if needed (centered)
        if (scale != 1.0) {
            double cx = size / 2.0;
            double cy = size / 2.0;
            g2.translate(cx, cy);
            g2.scale(scale, scale);
            g2.translate(-cx, -cy);
        }

        VectorIcons.paintIcon(g2, name, size, size, color, rotation);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
