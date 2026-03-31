import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Toolbar extends JPanel {

    private final LcdDisplay  mineDisplay;
    private final LcdDisplay  timerDisplay;
    private final ResetButton resetBtn;
    private final HeartbeatPanel heartbeat;

    private int selectedDiff = 0;
    private static final String[] DIFF_LABELS = { "Beginner", "Medium", "Expert" };
    private final JPanel diffRow;

    private DifficultyListener diffListener;

    public interface DifficultyListener {
        void onDifficultySelected(int level);
    }

    public Toolbar(Runnable onReset, DifficultyListener diffListener) {
        this.diffListener = diffListener;
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));

        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);
        topRow.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

        mineDisplay  = new LcdDisplay(Theme.DANGER);
        timerDisplay = new LcdDisplay(Theme.ACCENT);
        resetBtn     = new ResetButton(onReset);
        heartbeat    = new HeartbeatPanel();

        JPanel leftGroup  = buildLabeledDisplay("MINES", mineDisplay);
        JPanel rightGroup = buildLabeledDisplay("TIME",  timerDisplay);

        topRow.add(leftGroup,  BorderLayout.WEST);
        topRow.add(rightGroup, BorderLayout.EAST);

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrap.setOpaque(false);
        centerWrap.add(resetBtn);
        topRow.add(centerWrap, BorderLayout.CENTER);

        diffRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        diffRow.setOpaque(false);
        diffRow.setBorder(BorderFactory.createEmptyBorder(0, 16, 10, 16));

        for (int i = 0; i < DIFF_LABELS.length; i++) {
            final int idx = i;
            DiffTab tab = new DiffTab(DIFF_LABELS[i], i == 0);
            tab.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectDiff(idx);
                }
            });
            diffRow.add(tab);
        }

        add(topRow, BorderLayout.NORTH);
        add(heartbeat, BorderLayout.CENTER);
        add(diffRow, BorderLayout.SOUTH);
    }

    private JPanel buildLabeledDisplay(String label, LcdDisplay display) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label, JLabel.CENTER);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.TEXT_MUTED);
        p.add(lbl,     BorderLayout.NORTH);
        p.add(display, BorderLayout.SOUTH);
        return p;
    }

    private void selectDiff(int idx) {
        selectedDiff = idx;
        for (int i = 0; i < diffRow.getComponentCount(); i++) {
            Component c = diffRow.getComponent(i);
            if (c instanceof DiffTab dt) dt.setSelected(i == idx);
        }
        if (diffListener != null) diffListener.onDifficultySelected(idx);
    }

    public void setMines(int n)  { mineDisplay.setValue(n); }
    public void setTime(int n)   { timerDisplay.setValue(n); }
    public void setResetState(ResetButton.State s) { resetBtn.setState(s); }
    public void setHeartbeatIntensity(float intensity) { heartbeat.setIntensity(intensity); }

    private static class DiffTab extends JPanel {
        private final String label;
        private boolean selected;
        private boolean hovered = false;

        DiffTab(String label, boolean selected) {
            this.label    = label;
            this.selected = selected;
            setPreferredSize(new Dimension(80, 24));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(java.awt.event.MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        void setSelected(boolean s) { this.selected = s; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            RoundRectangle2D rr = new RoundRectangle2D.Float(0, 0, w, h, h, h);
            if (selected) {
                g2.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 30));
                g2.fill(rr);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(Theme.ACCENT);
                g2.draw(rr);
            } else if (hovered) {
                g2.setColor(Theme.BG_ELEVATED);
                g2.fill(rr);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(new Color(0x30, 0x36, 0x3d));
                g2.draw(rr);
            }
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(selected ? Theme.ACCENT : hovered ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(label)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(label, tx, ty);
            g2.dispose();
        }
    }

    private static class HeartbeatPanel extends JPanel {
        private float phase = 0f;
        private float intensity = 0f;
        private final Timer timer;

        HeartbeatPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(1, 18));
            timer = new Timer(40, e -> {
                phase += 0.12f;
                if (phase > Math.PI * 2) phase -= Math.PI * 2;
                repaint();
            });
            timer.start();
        }

        void setIntensity(float v) {
            intensity = Math.max(0f, Math.min(1f, v));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int mid = h / 2;

            g2.setColor(new Color(0x1b, 0x22, 0x2a));
            g2.drawLine(0, mid, w, mid);

            if (intensity > 0.01f) {
                g2.setStroke(new BasicStroke(1.6f));
                g2.setColor(new Color(Theme.FLAG.getRed(), Theme.FLAG.getGreen(), Theme.FLAG.getBlue(),
                        (int)(80 + 120 * intensity)));
                int step = Math.max(12, w / 20);
                for (int x = 0; x < w; x += step) {
                    float local = (float) Math.sin(phase + x * 0.08f);
                    int y = mid - (int) (local * intensity * 6f);
                    g2.drawLine(x, mid, x + step / 2, y);
                    g2.drawLine(x + step / 2, y, x + step, mid);
                }
            }
            g2.dispose();
        }
    }
}
