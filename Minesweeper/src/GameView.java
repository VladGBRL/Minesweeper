import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class GameView extends JFrame {
    private GameController controller;

    public GameView(int rows, int cols) {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(rows, cols));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton button = new JButton();
                final int row = i;
                final int col = j;
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        controller.cellClicked(row, col);
                    }
                });
                add(button);
            }
        }

        setSize(500, 500);
        setVisible(true);
    }

    public void setController(GameController controller) {

        this.controller = controller;
    }

    public void updateCell(int row, int col) {
        Cell cell = controller.getBoard().getCell(row, col);
        JButton button = (JButton) getContentPane().getComponent(row * controller.getBoard().getCols() + col);

        if (cell.isRevealed()) {
            if (cell.isMine()) {
                button.setBackground(Color.RED);
            } else {
                button.setBackground(Color.WHITE);
            }
            button.setText("");
        } else {
            button.setBackground(Color.LIGHT_GRAY);
        }
    }

    public void resetView() {
        for (Component component : getContentPane().getComponents()) {
            JButton button = (JButton) component;
            button.setBackground(null);
            button.setText("");
            button.setEnabled(true);
        }
    }
}