package it.ipedmanager.ui.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Modern Toggle Switch component (iOS/Material style).
 * 
 * @author William Tritapepe
 */
public class ToggleSwitch extends JComponent {

    private boolean selected = false;
    private boolean hover = false;

    // Animation
    private float animationProgress = 0f;
    private Timer timer;

    // Config
    private final int width = 40;
    private final int height = 22;
    private final int margin = 3;

    // Colors
    private Color switchOnColor = new Color(30, 58, 138); // Navy Blue // Primary Blue
    private Color switchOffColor = new Color(203, 213, 225); // Slate 300
    private Color knobColor = Color.WHITE;

    private final List<ActionListener> listeners = new ArrayList<>();

    public interface ActionListener {
        void onToggle(boolean selected);
    }

    public ToggleSwitch() {
        setPreferredSize(new Dimension(width, height));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isEnabled()) {
                    setSelected(!selected);
                    notifyListeners();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            startAnimation();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ActionListener l : listeners) {
            l.onToggle(selected);
        }
    }

    private void startAnimation() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        timer = new Timer(10, e -> {
            boolean done = false;
            if (selected) {
                animationProgress += 0.15f;
                if (animationProgress >= 1f) {
                    animationProgress = 1f;
                    done = true;
                }
            } else {
                animationProgress -= 0.15f;
                if (animationProgress <= 0f) {
                    animationProgress = 0f;
                    done = true;
                }
            }
            repaint();
            if (done)
                ((Timer) e.getSource()).stop();
        });
        timer.start();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCursor(enabled ? new Cursor(Cursor.HAND_CURSOR) : null);
        repaint();
    }

    public void setColors(Color switchOn, Color switchOff, Color knob) {
        this.switchOnColor = switchOn;
        this.switchOffColor = switchOff;
        this.knobColor = knob;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        int r = height;
        g2.setColor(calculateColor(switchOffColor, switchOnColor, animationProgress));
        g2.fillRoundRect(0, 0, width, height, r, r);

        // Knob
        int knobSize = height - (margin * 2);
        int startX = margin;
        int endX = width - margin - knobSize;
        int x = startX + (int) ((endX - startX) * animationProgress);

        g2.setColor(knobColor);
        g2.fillOval(x, margin, knobSize, knobSize);

        g2.dispose();
    }

    private Color calculateColor(Color c1, Color c2, float progress) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        return new Color(r, g, b);
    }
}
