import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Minesweeper {
    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/game.json")));
            JSONObject jsonObject = new JSONObject(content);
            int rows = jsonObject.getInt("rows");
            int cols = jsonObject.getInt("cols");
            JSONArray minesArray = jsonObject.getJSONArray("mines");

            List<Boolean> mines = new ArrayList<>();
            for (int i = 0; i < minesArray.length(); i++) {
                mines.add(minesArray.getBoolean(i));
            }

            Board board = new Board(rows, cols, mines);
            GameView view = new GameView(rows, cols);
            GameController controller = new GameController(board, view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




