import java.util.List;

class Board {
    private Cell[][] cells;
    private final List<Boolean> mines;

    public Board(int rows, int cols, List<Boolean> mines) {
        this.cells = new Cell[rows][cols];
        this.mines = mines;
        initializeCells();
    }

    private void initializeCells() {
        int index = 0;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                cells[i][j] = CellFactory.createCell(mines.get(index++));
            }
        }
    }

    public Cell getCell(int row, int col) {

        return cells[row][col];
    }

    public int getRows() {

        return cells.length;
    }

    public int getCols() {

        return cells[0].length;
    }

    public void resetBoard() {
        initializeCells();
    }
}
