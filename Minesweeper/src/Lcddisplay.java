import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LcdDisplay extends JPanel {
    private int value = 0;
    private final Color color;

    public LcdDisplay(Color color) {
        this.color = color;
        setPreferredSize(new Dimension(72, 32));
        setOpaque(false);
    }

    public void setValue(int v) { this.value = v; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        RoundRectangle2D rr = new RoundRectangle2D.Float(0, 0, w, h, 6, 6);
        g2.setColor(new Color(0x0a, 0x0d, 0x10));
        g2.fill(rr);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g2.draw(rr);
        g2.setFont(Theme.FONT_LCD);
        g2.setColor(color);
        String s = String.format("%03d", Math.min(999, Math.max(0, value)));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(s)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(s, tx, ty);
        g2.dispose();
    }
}
