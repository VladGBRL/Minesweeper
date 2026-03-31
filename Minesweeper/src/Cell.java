public class Cell {
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;

    public Cell(boolean isMine) {
        this.isMine = isMine;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }

    public boolean isMine()          { return isMine; }
    public boolean isRevealed()      { return isRevealed; }
    public boolean isFlagged()       { return isFlagged; }
    public int getAdjacentMines()    { return adjacentMines; }

    public void setMine(boolean mine)        { this.isMine = mine; }
    public void setAdjacentMines(int count)  { this.adjacentMines = count; }
    public void reveal()                     { if (!isFlagged) isRevealed = true; }
    public void toggleFlag()                 { if (!isRevealed) isFlagged = !isFlagged; }

    public void reset(boolean mine) {
        this.isMine      = mine;
        this.isRevealed  = false;
        this.isFlagged   = false;
        this.adjacentMines = 0;
    }
    public void forceReveal() { isRevealed = true; }
}
