import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ImageProcessor {

    public static BufferedImage blur(BufferedImage src, int times) {
        float[] matrix = {
            1/16f, 2/16f, 1/16f,
            2/16f, 4/16f, 2/16f,
            1/16f, 2/16f, 1/16f
        };
        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage result = src;
        for (int i = 0; i < times; i++) {
            result = op.filter(result, null);
        }
        return result;
    }

    public static BufferedImage scale(BufferedImage src, int newW, int newH) {
        BufferedImage result = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();
        return result;
    }

    public static BufferedImage rotate(BufferedImage src, double angleDegrees) {
        double rad = Math.toRadians(angleDegrees);
        int w = src.getWidth(), h = src.getHeight();
        double cos = Math.abs(Math.cos(rad)), sin = Math.abs(Math.sin(rad));
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage result = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.translate((newW - w) / 2.0, (newH - h) / 2.0);
        g.rotate(rad, w / 2.0, h / 2.0);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return result;
    }

    public static BufferedImage crop(BufferedImage src, int x, int y, int w, int h) {
        x = Math.max(0, Math.min(x, src.getWidth() - 1));
        y = Math.max(0, Math.min(y, src.getHeight() - 1));
        w = Math.min(w, src.getWidth() - x);
        h = Math.min(h, src.getHeight() - y);
        return src.getSubimage(x, y, w, h);
    }

    public static BufferedImage grayscale(BufferedImage src) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = result.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return result;
    }

    /** Adjust brightness: factor > 1 brightens, < 1 darkens */
    public static BufferedImage brightness(BufferedImage src, float factor) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >> 24) & 0xff;
                int r = Math.min(255, (int)(((argb >> 16) & 0xff) * factor));
                int g = Math.min(255, (int)(((argb >>  8) & 0xff) * factor));
                int b = Math.min(255, (int)(((argb      ) & 0xff) * factor));
                result.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    public static BufferedImage flipHorizontal(BufferedImage src) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-src.getWidth(), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(src, null);
    }

    public static BufferedImage createMineSprite(int size, Color bodyColor, Color spikeColor) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = size / 2, cy = size / 2, r = size / 3;

        g.setColor(spikeColor);
        g.setStroke(new BasicStroke(size / 12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * i / 4;
            int x1 = (int)(cx + r * Math.cos(angle));
            int y1 = (int)(cy + r * Math.sin(angle));
            int x2 = (int)(cx + (r + size / 6) * Math.cos(angle));
            int y2 = (int)(cy + (r + size / 6) * Math.sin(angle));
            g.drawLine(x1, y1, x2, y2);
        }

        g.setColor(bodyColor);
        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);

        g.setColor(spikeColor.darker());
        g.setStroke(new BasicStroke(size / 20f));
        g.drawOval(cx - r, cy - r, 2 * r, 2 * r);

        g.setColor(new Color(255, 255, 255, 120));
        int refR = r / 3;
        g.fillOval(cx - r / 2, cy - r / 2, refR * 2, refR * 2);

        g.dispose();
        return img;
    }

    // ---------------------------------------------------------------
    // Mine-field scene (dark terrain with mine silhouettes)
    // ---------------------------------------------------------------
    public static BufferedImage createSceneImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark ground gradient
        GradientPaint bg = new GradientPaint(0, 0, new Color(0x10, 0x16, 0x1c),
                                              0, h, new Color(0x1e, 0x28, 0x30));
        g.setPaint(bg);
        g.fillRect(0, 0, w, h);

        // Faint grid lines
        g.setColor(new Color(255, 255, 255, 12));
        g.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < w; x += 20) g.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 20) g.drawLine(0, y, w, y);

        // Mine silhouettes scattered
        java.util.Random rng = new java.util.Random(42);
        for (int i = 0; i < 7; i++) {
            int mx = 15 + rng.nextInt(w - 30);
            int my = 15 + rng.nextInt(h - 30);
            int mr = 8 + rng.nextInt(8);
            drawSimpleMine(g, mx, my, mr, new Color(0x3a, 0x45, 0x52));
        }

        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.setColor(new Color(0xff, 0xa0, 0x44, 160));
        g.drawString("⚠  DANGER", 18, 22);

        // Subtle scan line texture
        g.setColor(new Color(0, 0, 0, 20));
        for (int y = 0; y < h; y += 3) g.fillRect(0, y, w, 1);

        g.dispose();
        return img;
    }

    private static void drawSimpleMine(Graphics2D g, int cx, int cy, int r, Color col) {
        g.setColor(col);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * i / 4;
            g.drawLine((int)(cx + r * Math.cos(a)), (int)(cy + r * Math.sin(a)),
                       (int)(cx + (r + 4) * Math.cos(a)), (int)(cy + (r + 4) * Math.sin(a)));
        }
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
    }

    public static BufferedImage createRadarImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Very dark green background
        g.setColor(new Color(0x04, 0x0e, 0x07));
        g.fillRect(0, 0, w, h);

        int cx = w / 2, cy = h / 2;
        int maxR = Math.min(cx, cy) - 4;

        // Concentric rings
        g.setStroke(new BasicStroke(1f));
        for (int r = maxR / 4; r <= maxR; r += maxR / 4) {
            int alpha = 40 + (r * 60 / maxR);
            g.setColor(new Color(0x00, 0x88, 0x33, alpha));
            g.drawOval(cx - r, cy - r, r * 2, r * 2);
        }

        // Cross-hairs
        g.setColor(new Color(0x00, 0x55, 0x22, 100));
        g.drawLine(cx - maxR, cy, cx + maxR, cy);
        g.drawLine(cx, cy - maxR, cx, cy + maxR);

        // Static sweep ghost at ~45deg
        GradientPaint sweep = new GradientPaint(cx, cy,
            new Color(0x00, 0xff, 0x44, 100), cx + maxR, cy - maxR, new Color(0, 0, 0, 0));
        g.setPaint(sweep);
        g.fillArc(cx - maxR, cy - maxR, maxR * 2, maxR * 2, 0, 60);

        // Fixed blips
        int[][] blipPos = {{cx + 30, cy - 20}, {cx - 40, cy + 25}, {cx + 15, cy + 45}};
        for (int[] bp : blipPos) {
            g.setColor(new Color(0xff, 0x44, 0x22, 180));
            g.fillOval(bp[0] - 4, bp[1] - 4, 8, 8);
            g.setColor(new Color(0xff, 0x44, 0x22, 50));
            g.fillOval(bp[0] - 10, bp[1] - 10, 20, 20);
        }

        // Scanline texture
        g.setColor(new Color(0, 0, 0, 25));
        for (int y = 0; y < h; y += 2) g.fillRect(0, y, w, 1);

        g.dispose();
        return img;
    }

}
