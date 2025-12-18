package it.ipedmanager.ui.config;

import java.awt.*;
import java.awt.geom.*;

/**
 * Utility class to draw modern vector icons using Java 2D.
 * Updated with professional 24x24 grid icons (Feather/Lucide style).
 */
public class VectorIcons {

    public static void paintIcon(Graphics2D g2, String iconName, int width, int height, Color color, double rotation) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Scale from 24x24 coordinate system to requested size
        double scaleX = width / 24.0;
        double scaleY = height / 24.0;

        // Center if aspect ratio differs (though usually square)
        AffineTransform oldTr = g2.getTransform();
        g2.scale(scaleX, scaleY);

        if (rotation != 0) {
            g2.rotate(Math.toRadians(rotation), 12, 12); // Rotate around center of 24x24 grid
        }

        switch (iconName) {
            case "home":
                // 3,9 -> 12,2 -> 21,9
                drawPoly(g2, 3, 9, 12, 2, 21, 9);
                // 21,9 -> 21,20 -> 19,22 -> 5,22 -> 3,20 -> 3,9
                // Simplified home body
                Path2D home = new Path2D.Double();
                home.moveTo(21, 20);
                home.curveTo(21, 21.1, 20.1, 22, 19, 22);
                home.lineTo(5, 22);
                home.curveTo(3.9, 22, 3, 21.1, 3, 20);
                home.lineTo(3, 9);
                home.lineTo(21, 9);
                home.closePath(); // Not closed, just lines usually, but close finishes the loop
                g2.draw(home);
                // Door: 9,22 -> 9,12 -> 15,12 -> 15,22
                drawPoly(g2, 9, 22, 9, 12, 15, 12, 15, 22);
                break;

            case "gears":
            case "settings": // Alias
                // Cog (Mechanical Gear)
                // Center hole
                g2.draw(new Ellipse2D.Double(10, 10, 4, 4));
                // Outer Ring
                g2.draw(new Ellipse2D.Double(6, 6, 12, 12));
                // Teeth
                AffineTransform saved = g2.getTransform();
                for (int i = 0; i < 8; i++) {
                    g2.rotate(Math.toRadians(i * 45), 12, 12);
                    g2.fill(new Rectangle2D.Double(10.5, 2, 3, 3));
                }
                g2.setTransform(saved);
                break;

            case "report":
                // Document shape
                Path2D doc = new Path2D.Double();
                doc.moveTo(14, 2);
                doc.lineTo(18, 6);
                doc.lineTo(18, 20);
                doc.curveTo(18, 21.1, 17.1, 22, 16, 22);
                doc.lineTo(8, 22);
                doc.curveTo(6.9, 22, 6, 21.1, 6, 20);
                doc.lineTo(6, 4);
                doc.curveTo(6, 2.9, 6.9, 2, 8, 2);
                doc.lineTo(14, 2);
                g2.draw(doc);
                // Lines
                g2.draw(new Line2D.Double(10, 8, 14, 8)); // Short
                g2.draw(new Line2D.Double(10, 13, 14, 13));
                g2.draw(new Line2D.Double(10, 17, 14, 17));
                break;

            case "hash":
                g2.draw(new Line2D.Double(4, 9, 20, 9));
                g2.draw(new Line2D.Double(4, 15, 20, 15));
                g2.draw(new Line2D.Double(10, 3, 8, 21));
                g2.draw(new Line2D.Double(16, 3, 14, 21));
                break;

            case "ocr":
                drawPoly(g2, 4, 7, 4, 4, 20, 4, 20, 7);
                g2.draw(new Line2D.Double(9, 20, 15, 20)); // Base
                g2.draw(new Line2D.Double(12, 4, 12, 20)); // Vert
                break;

            case "audio":
                // Mic
                Path2D mic = new Path2D.Double();
                mic.moveTo(12, 1);
                mic.curveTo(13.6, 1, 15, 2.3, 15, 4);
                mic.lineTo(15, 10);
                mic.curveTo(15, 11.6, 13.6, 13, 12, 13);
                mic.curveTo(10.3, 13, 9, 11.6, 9, 10);
                mic.lineTo(9, 4);
                mic.curveTo(9, 2.3, 10.3, 1, 12, 1);
                g2.draw(mic);
                // Stand
                Path2D stand = new Path2D.Double();
                stand.moveTo(19, 10);
                stand.lineTo(19, 10); // handle
                stand.moveTo(19, 10);
                stand.curveTo(19, 13.8, 15.8, 17, 12, 17);
                stand.curveTo(8.1, 17, 5, 13.8, 5, 10);
                g2.draw(stand);
                g2.draw(new Line2D.Double(12, 17, 12, 23));
                g2.draw(new Line2D.Double(8, 23, 16, 23));
                break;

            case "media":
                // Video/Media Camera
                Path2D med = new Path2D.Double();
                med.moveTo(23, 7);
                med.lineTo(16, 12);
                med.lineTo(23, 17);
                med.lineTo(23, 7);
                g2.draw(med);
                g2.draw(new RoundRectangle2D.Double(1, 5, 15, 14, 4, 4));
                break;

            case "video":
                // Rect + triangle
                Path2D vid = new Path2D.Double();
                vid.moveTo(23, 7);
                vid.lineTo(16, 12);
                vid.lineTo(23, 17);
                vid.lineTo(23, 7);
                g2.draw(vid);
                g2.draw(new RoundRectangle2D.Double(1, 5, 15, 14, 4, 4));
                break;

            case "image":
                g2.draw(new RoundRectangle2D.Double(3, 3, 18, 18, 4, 4));
                g2.draw(new Ellipse2D.Double(8.5 - 1.5, 8.5 - 1.5, 3, 3));
                drawPoly(g2, 21, 15, 16, 10, 5, 21);
                break;

            case "face":
                g2.draw(new RoundRectangle2D.Double(3, 3, 18, 18, 4, 4)); // Frame
                g2.draw(new Ellipse2D.Double(9, 9, 1, 1)); // Eye L
                g2.draw(new Ellipse2D.Double(14, 9, 1, 1)); // Eye R
                // Mouth
                Path2D mouth = new Path2D.Double();
                mouth.moveTo(9, 15);
                mouth.quadTo(12, 17, 15, 15);
                g2.draw(mouth);
                break;

            case "photodna":
                // Grid 3x3
                for (int r = 0; r < 2; r++) {
                    for (int c = 0; c < 2; c++) {
                        g2.draw(new Rectangle2D.Double(4 + c * 9, 4 + r * 9, 7, 7));
                    }
                }
                break;

            case "elastic":
                // Search
                g2.draw(new Ellipse2D.Double(11 - 8, 11 - 8, 16, 16));
                g2.draw(new Line2D.Double(21, 21, 16.65, 16.65));
                break;

            case "minio":
                // Cloud
                Path2D cloud = new Path2D.Double();
                cloud.moveTo(18, 10);
                cloud.lineTo(16.74, 10); // gap
                // Simplified cloud
                cloud.moveTo(4, 18);
                cloud.curveTo(4, 21, 8, 21, 9, 20); // bottom left
                cloud.lineTo(18, 20);
                cloud.curveTo(22, 20, 22, 14, 18, 14); // right
                cloud.curveTo(18, 11, 15, 10, 14, 11); // top
                cloud.curveTo(12, 7, 7, 9, 7, 13); // top left
                cloud.curveTo(3, 13, 3, 18, 7, 18); // left
                g2.draw(cloud); // drawing rough cloud
                break;

            case "globe":
            case "language":
                // Lucide 'languages' icon (from lucide.dev) - translation symbol
                // Left side: letter with underline and cross
                g2.draw(new Line2D.Double(2, 5, 14, 5)); // Top horizontal line
                g2.draw(new Line2D.Double(7, 2, 8, 2)); // Small tick
                drawPoly(g2, 5, 8, 11, 14); // Diagonal line 1
                drawPoly(g2, 4, 14, 10, 8); // Diagonal line 2
                drawPoly(g2, 4, 14, 12, 5); // Line from bottom
                // Right side: letter A shape
                drawPoly(g2, 22, 22, 17, 12, 12, 22); // A shape
                g2.draw(new Line2D.Double(14, 18, 20, 18)); // A crossbar
                break;

            case "user":
                g2.draw(new Ellipse2D.Double(12 - 4, 7 - 4, 8, 8)); // head
                Path2D u = new Path2D.Double();
                u.moveTo(20, 21);
                u.lineTo(20, 19);
                u.curveTo(20, 17, 16, 17, 16, 17); // simple
                u.lineTo(8, 17);
                u.curveTo(8, 17, 4, 17, 4, 19);
                u.lineTo(4, 21);
                g2.draw(u);
                break;

            case "terminal":
                drawPoly(g2, 4, 17, 10, 11, 4, 5);
                g2.draw(new Line2D.Double(12, 19, 20, 19));
                break;

            case "archive":
                // Box
                Path2D box = new Path2D.Double();
                box.moveTo(21, 8);
                box.lineTo(21, 21);
                box.lineTo(3, 21);
                box.lineTo(3, 8);
                g2.draw(box);
                // Lid
                g2.draw(new Rectangle2D.Double(1, 3, 22, 5));
                g2.draw(new Line2D.Double(10, 12, 14, 12));
                break;

            case "chip":
                g2.draw(new RoundRectangle2D.Double(4, 4, 16, 16, 4, 4));
                g2.draw(new Rectangle2D.Double(9, 9, 6, 6));
                // pins
                g2.draw(new Line2D.Double(9, 1, 9, 4));
                g2.draw(new Line2D.Double(15, 1, 15, 4));
                g2.draw(new Line2D.Double(9, 20, 9, 23));
                g2.draw(new Line2D.Double(15, 20, 15, 23));
                g2.draw(new Line2D.Double(1, 9, 4, 9));
                g2.draw(new Line2D.Double(1, 15, 4, 15));
                g2.draw(new Line2D.Double(20, 9, 23, 9));
                g2.draw(new Line2D.Double(20, 15, 23, 15));
                break;

            case "sliders":
                g2.draw(new Line2D.Double(4, 21, 4, 14));
                g2.draw(new Line2D.Double(4, 10, 4, 3));
                g2.draw(new Line2D.Double(12, 21, 12, 12));
                g2.draw(new Line2D.Double(12, 8, 12, 3));
                g2.draw(new Line2D.Double(20, 21, 20, 16));
                g2.draw(new Line2D.Double(20, 12, 20, 3));
                g2.draw(new Line2D.Double(1, 14, 7, 14));
                g2.draw(new Line2D.Double(9, 8, 15, 8));
                g2.draw(new Line2D.Double(17, 16, 23, 16));
                break;

            case "export":
                // Box export
                Path2D exp = new Path2D.Double();
                exp.moveTo(9, 21);
                exp.lineTo(5, 21);
                exp.curveTo(4, 21, 3, 20, 3, 19);
                exp.lineTo(3, 5);
                exp.curveTo(3, 4, 4, 3, 5, 3);
                exp.lineTo(9, 3);
                g2.draw(exp);
                drawPoly(g2, 16, 17, 21, 12, 16, 7);
                g2.draw(new Line2D.Double(21, 12, 9, 12));
                break;

            case "folder":
                // 22,19 -> -2-2 H4 a-2-2 V5 a-2-2 h5 l2 3 h9 a2 2 z
                Path2D f = new Path2D.Double();
                f.moveTo(22, 19);
                f.curveTo(22, 20.1, 21.1, 21, 20, 21);
                f.lineTo(4, 21);
                f.curveTo(2.9, 21, 2, 20.1, 2, 19);
                f.lineTo(2, 5);
                f.curveTo(2, 3.9, 2.9, 3, 4, 3);
                f.lineTo(9, 3);
                f.lineTo(11, 6);
                f.lineTo(20, 6);
                f.curveTo(21.1, 6, 22, 6.9, 22, 8);
                f.lineTo(22, 19);
                f.closePath();
                g2.draw(f);
                break;

            case "folder-open":
                // Just folder for now or similar to export
                paintIcon(g2, "folder", 24, 24, color, rotation);
                break;

            case "file":
                Path2D file = new Path2D.Double();
                file.moveTo(13, 2);
                file.lineTo(6, 2);
                file.curveTo(4.9, 2, 4, 2.9, 4, 4);
                file.lineTo(4, 20);
                file.curveTo(4, 21.1, 4.9, 22, 6, 22);
                file.lineTo(18, 22);
                file.curveTo(19.1, 22, 20, 21.1, 20, 20);
                file.lineTo(20, 9);
                file.lineTo(13, 2);
                g2.draw(file);
                drawPoly(g2, 13, 2, 13, 9, 20, 9);
                break;

            case "trash":
                drawPoly(g2, 3, 6, 5, 6, 21, 6);
                Path2D tr = new Path2D.Double();
                tr.moveTo(19, 6);
                tr.lineTo(19, 20);
                tr.curveTo(19, 21.1, 18.1, 22, 17, 22);
                tr.lineTo(7, 22);
                tr.curveTo(5.9, 22, 5, 21.1, 5, 20);
                tr.lineTo(5, 6);
                g2.draw(tr);
                g2.draw(new Line2D.Double(10, 11, 10, 17));
                g2.draw(new Line2D.Double(14, 11, 14, 17));
                break;

            case "github":
                // GitHub Octocat logo (simplified 16x16 scaled to 24x24)
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D gh = new Path2D.Double();
                // Outer circle/body
                gh.moveTo(12, 2);
                gh.curveTo(6.48, 2, 2, 6.48, 2, 12);
                gh.curveTo(2, 16.42, 4.87, 20.17, 8.84, 21.5);
                gh.curveTo(9.34, 21.58, 9.5, 21.27, 9.5, 21);
                gh.curveTo(9.5, 20.77, 9.5, 20, 9.5, 19);
                gh.curveTo(6.73, 19.7, 6.14, 17.7, 6.14, 17.7);
                gh.curveTo(5.68, 16.6, 5.03, 16.3, 5.03, 16.3);
                gh.curveTo(4.12, 15.7, 5.1, 15.7, 5.1, 15.7);
                gh.curveTo(6.1, 15.77, 6.63, 16.72, 6.63, 16.72);
                gh.curveTo(7.5, 18.22, 8.97, 17.76, 9.54, 17.5);
                gh.curveTo(9.63, 16.87, 9.89, 16.45, 10.17, 16.21);
                gh.curveTo(8, 15.96, 5.73, 15.09, 5.73, 11.37);
                gh.curveTo(5.73, 10.36, 6.1, 9.53, 6.65, 8.88);
                gh.curveTo(6.55, 8.63, 6.2, 7.68, 6.75, 6.41);
                gh.curveTo(6.75, 6.41, 7.59, 6.14, 9.5, 7.31);
                gh.curveTo(10.29, 7.09, 11.15, 6.98, 12, 6.98);
                gh.curveTo(12.85, 6.98, 13.71, 7.09, 14.5, 7.31);
                gh.curveTo(16.41, 6.14, 17.25, 6.41, 17.25, 6.41);
                gh.curveTo(17.8, 7.68, 17.45, 8.63, 17.35, 8.88);
                gh.curveTo(17.9, 9.53, 18.27, 10.36, 18.27, 11.37);
                gh.curveTo(18.27, 15.1, 15.99, 15.95, 13.81, 16.19);
                gh.curveTo(14.17, 16.5, 14.5, 17.09, 14.5, 18);
                gh.curveTo(14.5, 19.36, 14.5, 20.64, 14.5, 21);
                gh.curveTo(14.5, 21.27, 14.66, 21.59, 15.17, 21.5);
                gh.curveTo(19.14, 20.16, 22, 16.42, 22, 12);
                gh.curveTo(22, 6.48, 17.52, 2, 12, 2);
                gh.closePath();
                g2.fill(gh);
                break;

            case "info":
                g2.draw(new Ellipse2D.Double(2, 2, 20, 20)); // Circle
                g2.draw(new Line2D.Double(12, 16, 12, 12));
                g2.draw(new Line2D.Double(12, 8, 12, 8.01));
                break;

            case "plus":
                g2.draw(new Line2D.Double(12, 5, 12, 19));
                g2.draw(new Line2D.Double(5, 12, 19, 12));
                break;

            case "file-plus":
                // Draw File
                paintIcon(g2, "file", 24, 24, color, 0);
                // Draw Plus Badge (Bottom Right)
                // Clear background for badge? No, just draw bold plus
                // Center at 18,18
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Vertical
                g2.draw(new Line2D.Double(18, 14, 18, 22));
                // Horizontal
                g2.draw(new Line2D.Double(14, 18, 22, 18));
                break;

            case "folder-plus":
                // Draw Folder
                paintIcon(g2, "folder", 24, 24, color, 0);
                // Draw Plus Badge (Bottom Right)
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Double(18, 14, 18, 22));
                g2.draw(new Line2D.Double(14, 18, 22, 18));
                break;

            case "minus":
                g2.draw(new Line2D.Double(5, 12, 19, 12));
                break;

            case "refresh":
            case "reset":
                // Clean circular arrows
                Path2D arr1 = new Path2D.Double();
                arr1.moveTo(21, 12);
                arr1.curveTo(21, 7.03, 16.97, 3, 12, 3);
                arr1.curveTo(9.39, 3, 7.06, 4.07, 5.38, 5.81);
                g2.draw(arr1);
                // Arrow head top
                drawPoly(g2, 1, 4, 5, 4, 5, 8);
                Path2D arr2 = new Path2D.Double();
                arr2.moveTo(3, 12);
                arr2.curveTo(3, 16.97, 7.03, 21, 12, 21);
                arr2.curveTo(14.61, 21, 16.94, 19.93, 18.62, 18.19);
                g2.draw(arr2);
                // Arrow head bottom
                drawPoly(g2, 23, 20, 19, 20, 19, 16);
                break;

            case "play":
                Path2D play = new Path2D.Double();
                play.moveTo(5, 3);
                play.lineTo(19, 12);
                play.lineTo(5, 21);
                play.closePath();
                g2.fill(play); // Filled play
                break;

            case "menu":
            case "list":
                // Hamburger Menu
                g2.draw(new Line2D.Double(4, 6, 20, 6));
                g2.draw(new Line2D.Double(4, 12, 20, 12));
                g2.draw(new Line2D.Double(4, 18, 20, 18));
                break;

            case "pencil":
            case "edit":
            case "rename":
                // Pencil icon
                Path2D pen = new Path2D.Double();
                pen.moveTo(17, 3);
                pen.lineTo(21, 7);
                pen.lineTo(8, 20);
                pen.lineTo(3, 21);
                pen.lineTo(4, 16);
                pen.closePath();
                g2.draw(pen);
                // Line inside
                g2.draw(new Line2D.Double(15, 5, 19, 9));
                break;

            case "help":
                // Standard Clean Question Mark (Manually tuned for visibility)
                Path2D q = new Path2D.Double();
                // Start left-middle of the head
                q.moveTo(8, 9);
                // Arch up to the center-top (12, 5)
                q.curveTo(8, 5, 10, 5, 12, 5);
                // Arch down to the right (16, 9)
                q.curveTo(15, 5, 16, 7, 16, 9);
                // Curve in towards the center stem (12, 13)
                q.curveTo(16, 12, 12, 11, 12, 15);
                // Draw the stem down
                q.lineTo(12, 16);
                g2.draw(q);

                // Dot (Bolder)
                g2.fill(new Ellipse2D.Double(12 - 1.5, 19, 3, 3));
                break;

            default:
                g2.draw(new Rectangle2D.Double(2, 2, 20, 20));
        }

        g2.setTransform(oldTr);
    }

    private static void drawPoly(Graphics2D g2, double... coords) {
        if (coords.length < 2)
            return;
        Path2D p = new Path2D.Double();
        p.moveTo(coords[0], coords[1]);
        for (int i = 2; i < coords.length; i += 2) {
            p.lineTo(coords[i], coords[i + 1]);
        }
        g2.draw(p);
    }
}
