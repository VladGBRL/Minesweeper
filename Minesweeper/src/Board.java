import java.util.Random;

public class Board {
    private Cell[][] cells;
    private final int rows;
    private final int cols;
    private final int totalMines;

    public Board(int rows, int cols, int totalMines) {
        this.rows       = rows;
        this.cols       = cols;
        this.totalMines = totalMines;
        this.cells      = new Cell[rows][cols];
        initEmpty();
    }

    private void initEmpty() {
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                cells[i][j] = CellFactory.createCell(false);
    }

    public void placeMines(int safeRow, int safeCol) {
        Random rand   = new Random();
        int    placed = 0;
        while (placed < totalMines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (!cells[r][c].isMine() && !isNeighbor(r, c, safeRow, safeCol)) {
                cells[r][c].setMine(true);
                placed++;
            }
        }
        computeAdjacentCounts();
    }

    private boolean isNeighbor(int r, int c, int sr, int sc) {
        return Math.abs(r - sr) <= 1 && Math.abs(c - sc) <= 1;
    }

    private void computeAdjacentCounts() {
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isMine()) continue;
                int count = 0;
                for (int[] d : DIRS) {
                    int ni = i + d[0], nj = j + d[1];
                    if (inBounds(ni, nj) && cells[ni][nj].isMine()) count++;
                }
                cells[i][j].setAdjacentMines(count);
            }
    }

    public void floodReveal(int row, int col) {
        if (!inBounds(row, col)) return;
        Cell cell = cells[row][col];
        if (cell.isRevealed() || cell.isFlagged() || cell.isMine()) return;
        cell.reveal();
        if (cell.getAdjacentMines() == 0)
            for (int[] d : DIRS) floodReveal(row + d[0], col + d[1]);
    }

    public void revealAll() {
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                Cell c = cells[i][j];
                c.forceReveal();
            }
    }

    public boolean isWon() {
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (!cells[i][j].isMine() && !cells[i][j].isRevealed()) return false;
        return true;
    }

    public int countFlags() {
        int n = 0;
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (cells[i][j].isFlagged()) n++;
        return n;
    }

    public void reset() { initEmpty(); }

    public Cell    getCell(int r, int c)  { return cells[r][c]; }
    public int     getRows()              { return rows; }
    public int     getCols()              { return cols; }
    public int     getTotalMines()        { return totalMines; }
    public boolean inBounds(int r, int c) { return r >= 0 && r < rows && c >= 0 && c < cols; }

    private static final int[][] DIRS = {
        {-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}
    };
}
