import javax.swing.*;
import java.awt.*;

/**
 * GameView — main window.
 * Layout:
 *   [LEFT SIDE PANEL]  [TOOLBAR + GRID]  [RIGHT SIDE PANEL]
 *
 * Left side:  decorative mine-field scene image (cropped, rotated)
 * Right side: radar image (cropped, rotated) + live video (radar animation)
 */
public class GameView extends JFrame {

    private GameController   controller;
    private Toolbar          toolbar;
    private GridPanel        gridPanel;
    private SideImagePanel   leftPanel;
    private SideImagePanel   rightPanel;
    private VideoPanel       videoPanel;

    private JButton soundBtn;
    private SoundManager sound;

    private int rows;
    private int cols;

    private JPanel root;
    private JPanel gameArea;
    private JPanel leftColumn;
    private JPanel rightColumn;
    private float bgPhase = 0f;
    private Timer bgTimer;

    public GameView(int rows, int cols, int totalMines, SoundManager sound) {
        this.rows  = rows;
        this.cols  = cols;
        this.sound = sound;

        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_DEEP);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 7));
                int step = 20;
                for (int x = 0; x < getWidth(); x += step)
                    for (int y = 0; y < getHeight(); y += step)
                        g2.fillOval(x - 1, y - 1, 2, 2);
                paintRadarSweep(g2);
                g2.dispose();
            }
        };
        root.setOpaque(true);
        setContentPane(root);

        // LEFT COLUMN
        leftPanel  = new SideImagePanel(SideImagePanel.Side.LEFT);
        leftColumn = new JPanel(new BorderLayout());
        leftColumn.setOpaque(false);
        leftColumn.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));
        leftColumn.add(leftPanel, BorderLayout.CENTER);

        // CENTER: toolbar + grid
        gameArea = new JPanel(new BorderLayout(0, 0));
        gameArea.setOpaque(false);
        gameArea.setBorder(BorderFactory.createEmptyBorder(0, 4, 12, 4));

        toolbar = new Toolbar(
            () -> { if (controller != null) controller.resetGame(); },
            level -> { if (controller != null) controller.setDifficulty(level); }
        );
        toolbar.setMines(totalMines);

        gridPanel = buildEmptyGrid(rows, cols);
        gameArea.add(toolbar,   BorderLayout.NORTH);
        gameArea.add(gridPanel, BorderLayout.CENTER);

        // RIGHT COLUMN: image + video + sound toggle
        rightPanel = new SideImagePanel(SideImagePanel.Side.RIGHT);
        videoPanel = new VideoPanel();
        soundBtn   = makeSoundButton();

        rightColumn = new JPanel(new BorderLayout(0, 6));
        rightColumn.setOpaque(false);
        rightColumn.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));

        JPanel videoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        videoWrap.setOpaque(false);
        videoWrap.add(videoPanel);

        JPanel soundWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        soundWrap.setOpaque(false);
        soundWrap.add(soundBtn);

        JPanel rBottom = new JPanel(new BorderLayout(0, 4));
        rBottom.setOpaque(false);
        rBottom.add(videoWrap,  BorderLayout.NORTH);
        rBottom.add(soundWrap,  BorderLayout.SOUTH);

        rightColumn.add(rightPanel, BorderLayout.CENTER);
        rightColumn.add(rBottom,    BorderLayout.SOUTH);

        root.add(leftColumn,  BorderLayout.WEST);
        root.add(gameArea,    BorderLayout.CENTER);
        root.add(rightColumn, BorderLayout.EAST);

        hookParallax();
        startBackgroundAnim();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton makeSoundButton() {
        JButton btn = new JButton("ON");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        btn.setForeground(Theme.TEXT_MUTED);
        btn.setBackground(new Color(0x1e, 0x24, 0x2b));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x30, 0x36, 0x3d), 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            boolean nowEnabled = !sound.isEnabled();
            sound.setEnabled(nowEnabled);
            btn.setText(nowEnabled ? "ON" : "♪ OFF");
        });
        return btn;
    }

    private GridPanel buildEmptyGrid(int r, int c) {
        CellButton.CellClickListener noOp = new CellButton.CellClickListener() {
            public void onLeftClick(int row, int col)  {}
            public void onRightClick(int row, int col) {}
        };
        return new GridPanel(r, c, noOp);
    }

    public void rebuildGrid(int rows, int cols, int totalMines,
                            CellButton.CellClickListener listener) {
        this.rows = rows; this.cols = cols;
        gameArea.remove(gridPanel);
        gridPanel = new GridPanel(rows, cols, listener);
        gameArea.add(gridPanel, BorderLayout.CENTER);
        toolbar.setMines(totalMines);
        toolbar.setTime(0);
        toolbar.setResetState(ResetButton.State.PLAYING);
        revalidate();
        pack();
        setLocationRelativeTo(null);
        repaint();
    }

    public void setController(GameController c) { this.controller = c; }

    public void updateCell(int r, int c, Cell cell) {
        gridPanel.getCell(r, c).applyState(cell);
    }

    public void markMineHit(int r, int c) {
        gridPanel.getCell(r, c).markMineHit();
    }

    public void setMines(int n)  { toolbar.setMines(n); }
    public void setTime(int n)   { toolbar.setTime(n); }
    public void setResetState(ResetButton.State s) { toolbar.setResetState(s); }

    public void resetAllCells(int totalMines) {
        gridPanel.resetAll();
        toolbar.setMines(totalMines);
        toolbar.setTime(0);
        toolbar.setResetState(ResetButton.State.PLAYING);
    }

    public GridPanel  getGridPanel()  { return gridPanel; }
    public VideoPanel getVideoPanel() { return videoPanel; }

    public void setHeartbeatIntensity(float intensity) {
        toolbar.setHeartbeatIntensity(intensity);
    }

    private void hookParallax() {
        root.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int w = Math.max(1, root.getWidth());
                int h = Math.max(1, root.getHeight());
                float nx = (e.getX() / (float) w) * 2f - 1f;
                float ny = (e.getY() / (float) h) * 2f - 1f;
                leftPanel.setParallax(nx, ny);
                rightPanel.setParallax(-nx, -ny);
            }
        });
    }

    private void startBackgroundAnim() {
        bgTimer = new Timer(40, e -> {
            bgPhase += 0.03f;
            if (bgPhase > (float)(Math.PI * 2)) bgPhase -= (float)(Math.PI * 2);
            root.repaint();
        });
        bgTimer.start();
    }

    private void paintRadarSweep(Graphics2D g2) {
        int w = root.getWidth();
        int h = root.getHeight();
        if (w <= 0 || h <= 0) return;

        float sweep = (float) ((Math.sin(bgPhase) + 1f) / 2f);
        int offset = (int) (sweep * (w + h)) - h;

        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(new Color(0x4a, 0xa8, 0xff, 35));
        g2.drawLine(offset, 0, offset + h, h);

        g2.setColor(new Color(0x4a, 0xa8, 0xff, 15));
        g2.drawLine(offset - 8, 0, offset + h - 8, h);
        g2.drawLine(offset + 8, 0, offset + h + 8, h);
    }
}
