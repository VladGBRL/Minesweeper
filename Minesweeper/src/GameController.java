import javax.swing.*;

class GameController {
    private Board board;
    private GameView view;
    private boolean gameOver;

    public GameController(Board board, GameView view) {
        this.board = board;
        this.view = view;
        this.gameOver = false;
        view.setController(this);
    }

    public void cellClicked(int row, int col) {
        if (gameOver) {
            return;
        }

        Cell cell = board.getCell(row, col);
        if (cell.isMine()) {
            gameOver = true;
            revealAllMines();
            view.updateCell(row, col);
            JOptionPane.showMessageDialog(view, "Game Over! You hit a mine.");
            resetGame();
        } else {
            cell.reveal();
            view.updateCell(row, col);
            if (isGameWon()) {
                gameOver = true;
                revealAllMines();
                JOptionPane.showMessageDialog(view, "Victory!");
            }
        }
    }

    private void resetGame() {
        gameOver = false;
        board.resetBoard();
        view.resetView();
        view.setController(this);
    }

    private void revealAllMines() {
        Board board = getBoard();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell cell = board.getCell(i, j);
                if (cell.isMine() && !cell.isRevealed()) {
                    cell.reveal();
                    view.updateCell(i, j);
                }
            }
        }
    }

    private boolean isGameWon() {
        Board board = getBoard();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell cell = board.getCell(i, j);
                if (!cell.isMine() && !cell.isRevealed()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Board getBoard() {

        return board;
    }

    public boolean isGameOver() {

        return gameOver;
    }
}