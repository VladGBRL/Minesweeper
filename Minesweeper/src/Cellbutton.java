import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class CellButton extends JPanel {

    private final int row;
    private final int col;

    private boolean hovered  = false;
    private boolean pressed  = false;
    private boolean revealed = false;
    private boolean mine     = false;
    private boolean mineHit  = false;
    private boolean flagged  = false;
    private int     adjacent = 0;

    private float revealAlpha = 0f;
    private Timer revealTimer;
    private float flagPulse = 0f;
    private Timer flagTimer;

    private java.util.List<Particle> particles = new java.util.ArrayList<>();
    private Timer particleTimer;

    private CellClickListener listener;

    public interface CellClickListener {
        void onLeftClick(int row, int col);
        void onRightClick(int row, int col);
    }

    public CellButton(int row, int col) {
        this.row = row;
        this.col = col;
        setPreferredSize(new Dimension(Theme.CELL_SIZE, Theme.CELL_SIZE));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)   { hovered = false; pressed = false; repaint(); }
            @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (listener == null) return;
                if (SwingUtilities.isRightMouseButton(e))     listener.onRightClick(row, col);
                else if (SwingUtilities.isLeftMouseButton(e)) listener.onLeftClick(row, col);
            }
        });
    }

    public void setListener(CellClickListener l) { this.listener = l; }

    public void applyState(Cell cell) {
        boolean wasRevealed = this.revealed;
        boolean wasFlagged  = this.flagged;
        this.revealed = cell.isRevealed();
        this.mine     = cell.isMine();
        this.flagged  = cell.isFlagged();
        this.adjacent = cell.getAdjacentMines();
        this.mineHit  = false;

        boolean triggered = false;
        if (revealed && !wasRevealed) {
            animateReveal();
            spawnRevealParticles();
            triggered = true;
        }
        if (flagged && !wasFlagged) {
            startFlagPulse();
            triggered = true;
        }
        if (!triggered) repaint();
    }

    public void markMineHit() { this.mineHit = true; repaint(); }

    public void reset() {
        revealed = false; mine = false; flagged = false;
        adjacent = 0; mineHit = false; revealAlpha = 0f;
        if (revealTimer != null) revealTimer.stop();
        if (flagTimer != null) flagTimer.stop();
        if (particleTimer != null) particleTimer.stop();
        particles.clear();
        flagPulse = 0f;
        hovered = false; pressed = false;
        repaint();
    }

    private void animateReveal() {
        revealAlpha = 0f;
        if (revealTimer != null) revealTimer.stop();
        revealTimer = new Timer(16, null);
        revealTimer.addActionListener(e -> {
            revealAlpha = Math.min(1f, revealAlpha + 0.12f);
            repaint();
            if (revealAlpha >= 1f) revealTimer.stop();
        });
        revealTimer.start();
    }

    private void startFlagPulse() {
        flagPulse = 1f;
        if (flagTimer != null) flagTimer.stop();
        flagTimer = new Timer(16, e -> {
            flagPulse = Math.max(0f, flagPulse - 0.08f);
            repaint();
            if (flagPulse <= 0f) flagTimer.stop();
        });
        flagTimer.start();
    }

    private void spawnRevealParticles() {
        int count = 10;
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = w / 2f;
            p.y = h / 2f;
            double angle = Math.random() * Math.PI * 2;
            float speed = 1.2f + (float)Math.random() * 2.5f;
            p.vx = (float)Math.cos(angle) * speed;
            p.vy = (float)Math.sin(angle) * speed;
            p.life = 1f;
            p.size = 2f + (float)Math.random() * 2.5f;
            p.color = Theme.ACCENT;
            particles.add(p);
        }

        if (particleTimer == null) {
            particleTimer = new Timer(16, e -> {
                for (int i = particles.size() - 1; i >= 0; i--) {
                    Particle p = particles.get(i);
                    p.x += p.vx;
                    p.y += p.vy;
                    p.vy += 0.05f;
                    p.life -= 0.05f;
                    if (p.life <= 0f) particles.remove(i);
                }
                repaint();
                if (particles.isEmpty()) particleTimer.stop();
            });
        }
        if (!particleTimer.isRunning()) particleTimer.start();
    }

    private static class Particle {
        float x, y, vx, vy, life, size;
        Color color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth(), h = getHeight();
        int arc = 6;
        RoundRectangle2D rr = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, arc, arc);

        if (revealed) paintRevealed(g2, w, h, rr);
        else          paintHidden(g2, w, h, rr);

        g2.dispose();
    }

    private void paintHidden(Graphics2D g2, int w, int h, RoundRectangle2D rr) {
        Color fill = pressed ? Theme.CELL_BORDER
                   : hovered ? Theme.CELL_HOVER
                   : Theme.CELL_HIDDEN;
        g2.setColor(fill);
        g2.fill(rr);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(pressed ? Theme.BG_DEEP : new Color(0x3d, 0x44, 0x4d));
        g2.draw(rr);
        if (!pressed) {
            g2.setColor(new Color(255, 255, 255, 18));
            g2.drawLine(3, 1, w - 4, 1);
        }
        if (flagged) drawFlag(g2, w, h);
    }

    private void paintRevealed(Graphics2D g2, int w, int h, RoundRectangle2D rr) {
        Color bg = mineHit ? Theme.DANGER
                 : mine    ? new Color(0x3a, 0x1c, 0x1c)
                 : Theme.CELL_REVEAL;

        if (revealAlpha < 1f) {
            g2.setColor(blendColor(Theme.CELL_HIDDEN, bg, revealAlpha));
        } else {
            g2.setColor(bg);
        }
        g2.fill(rr);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Theme.CELL_BORDER);
        g2.draw(rr);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, revealAlpha + 0.3f)));
        if (mine)              drawMine(g2, w, h, mineHit);
        else if (adjacent > 0) drawNumber(g2, w, h);
        g2.setComposite(AlphaComposite.SrcOver);
    }

    private void drawNumber(Graphics2D g2, int w, int h) {
        g2.setFont(Theme.FONT_CELL);
        g2.setColor(Theme.NUMBER_COLORS[adjacent]);
        String s = String.valueOf(adjacent);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(s)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(s, tx, ty);
    }

    private void drawMine(Graphics2D g2, int w, int h, boolean hit) {
        int cx = w / 2, cy = h / 2, r = w / 5;
        if (hit) {
            g2.setColor(new Color(0xf8, 0x51, 0x49, 60));
            g2.fillOval(cx - r * 2, cy - r * 2, r * 4, r * 4);
        }
        g2.setColor(new Color(0xe6, 0xed, 0xf3, 200));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * i / 4;
            int x1 = (int)(cx + r * Math.cos(angle));
            int y1 = (int)(cy + r * Math.sin(angle));
            int x2 = (int)(cx + (r + 4) * Math.cos(angle));
            int y2 = (int)(cy + (r + 4) * Math.sin(angle));
            g2.drawLine(x1, y1, x2, y2);
        }
        g2.setColor(hit ? Theme.DANGER : new Color(0xe6, 0xed, 0xf3));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillOval(cx - r / 2, cy - r / 2, r / 2, r / 2);
    }

    private void drawFlag(Graphics2D g2, int w, int h) {
        int cx = w / 2, cy = h / 2;
        if (flagPulse > 0f) {
            int glow = (int)(12 * flagPulse);
            g2.setColor(new Color(Theme.FLAG.getRed(), Theme.FLAG.getGreen(), Theme.FLAG.getBlue(),
                    (int)(80 * flagPulse)));
            g2.fillOval(cx - glow / 2, cy - glow / 2, glow, glow);
        }
        g2.setColor(Theme.TEXT_MUTED);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cx, cy + 7, cx, cy - 7);
        int bump = (int)(3 * flagPulse);
        int[] px = { cx, cx + 8 + bump, cx };
        int[] py = { cy - 7 - bump, cy - 3, cy + 1 + bump };
        g2.setColor(Theme.FLAG);
        g2.fillPolygon(px, py, 3);
        g2.setColor(Theme.TEXT_MUTED);
        g2.drawLine(cx - 4, cy + 7, cx + 4, cy + 7);
    }

    private static Color blendColor(Color a, Color b, float t) {
        int r  = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int g  = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
        return new Color(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, bl))
        );
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (!particles.isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (Particle p : particles) {
                float alpha = Math.max(0f, Math.min(1f, p.life));
                g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(),
                        (int)(alpha * 140)));
                int s = Math.max(1, (int)(p.size * (0.6f + alpha)));
                g2.fillOval((int)p.x - s / 2, (int)p.y - s / 2, s, s);
            }
            g2.dispose();
        }
    }
}
