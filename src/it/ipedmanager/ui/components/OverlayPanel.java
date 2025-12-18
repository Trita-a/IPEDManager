package it.ipedmanager.ui.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * Wrapper panel that can be "disabled" by painting a semi-transparent overlay
 * and blocking mouse interaction.
 * 
 * @author William Tritapepe
 */
public class OverlayPanel extends JLayeredPane {

    private final JPanel content;
    private final JPanel overlay;
    private boolean contentEnabled = true;

    public OverlayPanel(JComponent actualContent) {
        setLayout(new OverlayLayout(this));

        // Content Layer (Default)
        this.content = new JPanel(new BorderLayout());
        this.content.setOpaque(false);
        this.content.add(actualContent, BorderLayout.CENTER);

        // Overlay Layer (Top)
        this.overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (!contentEnabled) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(245, 247, 250, 180)); // Light gray, semi-transparent
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Optional: Lock Icon or Text
                    // g2.setColor(new Color(100, 116, 139));
                    // g2.drawString("Disabilitato", getWidth() / 2 - 30, getHeight() / 2);

                    g2.dispose();
                }
            }
        };
        this.overlay.setOpaque(false);

        // Block mouse events when disabled
        this.overlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!contentEnabled)
                    e.consume(); // Block
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!contentEnabled)
                    e.consume();
            }
            // Add other events if strictly needed, usually click/press is enough to block
            // buttons
        });

        // Add layers (Overlay on top of Content)
        add(overlay, JLayeredPane.PALETTE_LAYER); // Higher z-order
        add(content, JLayeredPane.DEFAULT_LAYER);
    }

    public void setContentEnabled(boolean enabled) {
        this.contentEnabled = enabled;
        // Recursively enable/disable Swing components?
        // Not strictly necessary if we block input, but looks better if we do neither
        // or both.
        // The overlay handles the visual "gray out".
        overlay.setVisible(!enabled); // Make overlay visible only when disabled
        overlay.repaint();
    }
}
