import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GridPanel extends JPanel {

    private CellButton[][] cells;
    private final int rows;
    private final int cols;

    public GridPanel(int rows, int cols, CellButton.CellClickListener listener) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new CellButton[rows][cols];

        int gap  = Theme.CELL_GAP;
        setLayout(new GridLayout(rows, cols, gap, gap));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton btn = new CellButton(r, c);
                btn.setListener(listener);
                cells[r][c] = btn;
                add(btn);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        RoundRectangle2D rr = new RoundRectangle2D.Float(0, 0, w, h, 12, 12);
        g2.setColor(new Color(0x30, 0x36, 0x3d, 120));
        g2.fill(rr);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(0x30, 0x36, 0x3d));
        g2.draw(rr);
        g2.dispose();
        super.paintComponent(g);
    }

    public CellButton getCell(int r, int c) { return cells[r][c]; }

    public void resetAll() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c].reset();
    }
}
