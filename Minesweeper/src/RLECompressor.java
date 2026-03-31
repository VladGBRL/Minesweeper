import java.util.ArrayList;
import java.util.List;

public class RLECompressor {

    public static String encode(List<Integer> data) {
        if (data == null || data.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int current = data.get(0);
        int count   = 1;

        for (int i = 1; i < data.size(); i++) {
            if (data.get(i) == current) {
                count++;
            } else {
                sb.append(current).append(':').append(count).append('|');
                current = data.get(i);
                count   = 1;
            }
        }
        sb.append(current).append(':').append(count);
        return sb.toString();
    }


    public static List<Integer> decode(String encoded) {
        List<Integer> result = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) return result;
        String[] parts = encoded.split("\\|");
        for (String part : parts) {
            String[] pair  = part.split(":");
            int value = Integer.parseInt(pair[0]);
            int count = Integer.parseInt(pair[1]);
            for (int i = 0; i < count; i++) result.add(value);
        }
        return result;
    }

    public static String encodeBoard(Board board) {
        List<Integer> data = new ArrayList<>();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                int val = (cell.isMine()     ? 4 : 0)
                        | (cell.isRevealed() ? 2 : 0)
                        | (cell.isFlagged()  ? 1 : 0);
                data.add(val);
            }
        }
        return encode(data);
    }

    public static double compressionRatio(List<Integer> original, String encoded) {
        int originalSize = original.size() * 4;
        int encodedSize  = encoded.getBytes().length;
        return (double) originalSize / encodedSize;
    }

    public static void printBoardStats(Board board, String event) {
        // Build raw mine list (0/1)
        List<Integer> mineList = new ArrayList<>();
        for (int r = 0; r < board.getRows(); r++)
            for (int c = 0; c < board.getCols(); c++)
                mineList.add(board.getCell(r, c).isMine() ? 1 : 0);

        // Full board state (mine + revealed + flagged)
        List<Integer> stateList = new ArrayList<>();
        for (int r = 0; r < board.getRows(); r++)
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                int val = (cell.isMine()     ? 4 : 0)
                        | (cell.isRevealed() ? 2 : 0)
                        | (cell.isFlagged()  ? 1 : 0);
                stateList.add(val);
            }

        String encodedMines = encode(mineList);
        String encodedState = encodeBoard(board);
        List<Integer> decoded = decode(encodedMines);
        boolean valid = decoded.equals(mineList);

        double mineRatio  = compressionRatio(mineList,  encodedMines);
        double stateRatio = compressionRatio(stateList, encodedState);

        int revealedCount = 0, flagCount = 0;
        for (int r = 0; r < board.getRows(); r++)
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isRevealed()) revealedCount++;
                if (board.getCell(r, c).isFlagged())  flagCount++;
            }

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.printf( "║  RLE COMPRESSOR  ─  %s%n", padRight(event, 32));
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf( "║  Board size  : %dx%d  (%d cells)%n",
                board.getRows(), board.getCols(), board.getRows() * board.getCols());
        System.out.printf( "║  Mines       : %d  │  Revealed: %d  │  Flagged: %d%n",
                board.getTotalMines(), revealedCount, flagCount);
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  [MINE MAP — 0=safe, 1=mine]");
        System.out.printf( "║  Original  : %d bytes (int array)%n", mineList.size() * 4);
        System.out.printf( "║  Encoded   : %d bytes  →  ratio %.2fx%n",
                encodedMines.getBytes().length, mineRatio);
        System.out.printf( "║  Encoded   : %s%n",
                encodedMines.length() > 48
                    ? encodedMines.substring(0, 48) + "…"
                    : encodedMines);
        System.out.printf( "║  Decode OK : %s%n", valid ? "✓ YES" : "✗ ERROR");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  [FULL STATE — bits: mine|revealed|flagged]");
        System.out.printf( "║  Original  : %d bytes%n", stateList.size() * 4);
        System.out.printf( "║  Encoded   : %d bytes  →  ratio %.2fx%n",
                encodedState.getBytes().length, stateRatio);
        System.out.printf( "║  Encoded   : %s%n",
                encodedState.length() > 48
                    ? encodedState.substring(0, 48) + "…"
                    : encodedState);
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  Board visual (M=mine, .=safe, R=revealed, F=flag):");
        printBoardVisual(board);
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.flush();
    }

    private static void printBoardVisual(Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            System.out.print("║  ");
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isFlagged())       System.out.print("F");
                else if (cell.isRevealed()) System.out.print(cell.isMine() ? "X" : "R");
                else if (cell.isMine())     System.out.print("M");
                else                        System.out.print(".");
            }
            System.out.println();
        }
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static String getBoardStats(Board board) {
        String encoded = encodeBoard(board);
        List<Integer> raw = new ArrayList<>();
        for (int r = 0; r < board.getRows(); r++)
            for (int c = 0; c < board.getCols(); c++)
                raw.add(board.getCell(r, c).isMine() ? 1 : 0);
        double ratio = compressionRatio(raw, encoded);
        return String.format(
            "RLE %dx%d  |  Original: %d B  |  Compressed: %d B  |  Ratio: %.2fx",
            board.getRows(), board.getCols(),
            raw.size() * 4, encoded.getBytes().length, ratio);
    }
}
