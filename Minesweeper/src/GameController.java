import javax.swing.*;

public class GameController implements CellButton.CellClickListener {

    private static final int[][] DIFFICULTIES = {
        {  9,  9, 10 },
        { 16, 16, 40 },
        { 16, 30, 99 },
    };

    private Board        board;
    private GameView     view;
    private SoundManager sound;
    private boolean      gameOver;
    private boolean      firstClick;
    private Timer        swingTimer;
    private int          elapsedSeconds;
    private int          currentDiff = 0;

    // Heartbeat fires when flaggedMines / totalMines >= this threshold
    private static final float HEARTBEAT_THRESHOLD = 0.50f;
    private int lastHeartbeatFlagCount = -1;

    public GameController(Board board, GameView view, SoundManager sound) {
        this.board = board;
        this.view  = view;
        this.sound = sound;
        view.setController(this);
        view.rebuildGrid(board.getRows(), board.getCols(), board.getTotalMines(), this);
        resetState();
        sound.startAmbient();
        RLECompressor.printBoardStats(board, "GAME START (no mines placed yet)");
    }

    @Override
    public void onLeftClick(int row, int col) {
        if (gameOver) return;
        Cell cell = board.getCell(row, col);
        if (cell.isRevealed() || cell.isFlagged()) return;

        if (firstClick) {
            firstClick = false;
            board.placeMines(row, col);
            startTimer();
            RLECompressor.printBoardStats(board, "MINES PLACED (first click)");
        }

        if (cell.isMine()) {
            cell.reveal();
            view.updateCell(row, col, cell);
            view.markMineHit(row, col);
            sound.playExplosion();
            RLECompressor.printBoardStats(board, "MINE HIT at [" + row + "," + col + "]");
            ExplosionAnimation.playExplosion(view, () ->
                SwingUtilities.invokeLater(() -> endGame(false))
            );
        } else {
            board.floodReveal(row, col);
            refreshAll();
            sound.playReveal();
            RLECompressor.printBoardStats(board, "REVEAL at [" + row + "," + col + "]");
            if (board.isWon()) endGame(true);
        }
    }

    @Override
    public void onRightClick(int row, int col) {
        if (gameOver) return;
        Cell cell = board.getCell(row, col);
        if (cell.isRevealed()) return;
        cell.toggleFlag();
        view.updateCell(row, col, cell);
        int flagCount = board.countFlags();
        view.setMines(board.getTotalMines() - flagCount);
        sound.playFlag();
        RLECompressor.printBoardStats(board,
            (cell.isFlagged() ? "FLAG PLACED" : "FLAG REMOVED") + " at [" + row + "," + col + "]");

        // Heartbeat tension: play when >= 50% of mines are flagged
        float ratio = (float) flagCount / board.getTotalMines();
        if (ratio >= HEARTBEAT_THRESHOLD && flagCount != lastHeartbeatFlagCount) {
            lastHeartbeatFlagCount = flagCount;
            sound.playHeartbeat();
        }
        float intensity = (ratio - HEARTBEAT_THRESHOLD) / (1f - HEARTBEAT_THRESHOLD);
        view.setHeartbeatIntensity(Math.max(0f, Math.min(1f, intensity)));
    }

    public void setDifficulty(int level) {
        if (level < 0 || level >= DIFFICULTIES.length) return;
        currentDiff = level;
        int[] d = DIFFICULTIES[level];
        stopTimer();
        board = new Board(d[0], d[1], d[2]);
        view.rebuildGrid(d[0], d[1], d[2], this);
        resetState();
        String[] names = {"BEGINNER", "MEDIUM", "EXPERT"};
        RLECompressor.printBoardStats(board, "DIFFICULTY: " + names[level]);
    }

    public void resetGame() {
        stopTimer();
        board.reset();
        view.resetAllCells(board.getTotalMines());
        resetState();
        RLECompressor.printBoardStats(board, "GAME RESET");
    }

    private void resetState() {
        gameOver               = false;
        firstClick             = true;
        elapsedSeconds         = 0;
        lastHeartbeatFlagCount = -1;
        view.setTime(0);
        view.setResetState(ResetButton.State.PLAYING);
        view.setHeartbeatIntensity(0f);
    }

    private void endGame(boolean won) {
        gameOver = true;
        stopTimer();

        if (!won) {
            board.revealAll();
            refreshAll();
            view.setResetState(ResetButton.State.DEAD);
            RLECompressor.printBoardStats(board, "GAME OVER — all mines revealed");
            JOptionPane.showMessageDialog(view,
                "Mine hit!  Click ◉ to try again.",
                "Game Over", JOptionPane.ERROR_MESSAGE);
        } else {
            view.setResetState(ResetButton.State.WIN);
            RLECompressor.printBoardStats(board, "VICTORY in " + elapsedSeconds + "s");
            // Play victory sound then animate
            sound.playVictory();
            VictoryAnimation.playVictory(view, () ->
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(view,
                        "🎉  Board cleared in " + elapsedSeconds + "s!",
                        "Victory", JOptionPane.INFORMATION_MESSAGE)
                )
            );
        }
    }

    private void startTimer() {
        swingTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            view.setTime(elapsedSeconds);
            // Countdown beep in the last 10 seconds if many cells revealed
            int remaining = board.getRows() * board.getCols()
                          - countRevealed() - board.getTotalMines();
            if (remaining <= 5 && remaining > 0) {
                sound.playCountdown();
            }
        });
        swingTimer.start();
    }

    private int countRevealed() {
        int n = 0;
        for (int r = 0; r < board.getRows(); r++)
            for (int c = 0; c < board.getCols(); c++)
                if (board.getCell(r, c).isRevealed()) n++;
        return n;
    }

    private void stopTimer() {
        if (swingTimer != null) { swingTimer.stop(); swingTimer = null; }
    }

    private void refreshAll() {
        for (int r = 0; r < board.getRows(); r++)
            for (int c = 0; c < board.getCols(); c++)
                view.updateCell(r, c, board.getCell(r, c));
    }

    public Board getBoard() { return board; }
}
