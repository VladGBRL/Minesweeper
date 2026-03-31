import java.awt.*;

public class Theme {
    public static final int CELL_SIZE = 32;
    public static final int CELL_GAP  = 2;

    public static final Color BG_DEEP       = new Color(0x13, 0x17, 0x1b);
    public static final Color BG_ELEVATED   = new Color(0x1e, 0x24, 0x2b);
    public static final Color CELL_HIDDEN   = new Color(0x28, 0x2f, 0x38);
    public static final Color CELL_HOVER    = new Color(0x32, 0x3b, 0x47);
    public static final Color CELL_REVEAL   = new Color(0x1a, 0x1f, 0x26);
    public static final Color CELL_BORDER   = new Color(0x20, 0x26, 0x2d);
    public static final Color ACCENT        = new Color(0x4a, 0xa8, 0xff);
    public static final Color DANGER        = new Color(0xf8, 0x51, 0x49);
    public static final Color FLAG          = new Color(0xff, 0xa0, 0x44);
    public static final Color TEXT_PRIMARY  = new Color(0xe6, 0xed, 0xf3);
    public static final Color TEXT_MUTED    = new Color(0x6e, 0x78, 0x87);

    public static final Font FONT_CELL  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font FONT_LCD   = new Font("Courier New", Font.BOLD, 20);

    public static final Color[] NUMBER_COLORS = {
        null,
        new Color(0x4a, 0xa8, 0xff), // 1 - blue
        new Color(0x3f, 0xc7, 0x6e), // 2 - green
        new Color(0xf8, 0x51, 0x49), // 3 - red
        new Color(0x9b, 0x59, 0xb6), // 4 - purple
        new Color(0xe7, 0x4c, 0x3c), // 5 - dark red
        new Color(0x1a, 0xbc, 0x9c), // 6 - teal
        new Color(0xe6, 0xed, 0xf3), // 7 - white
        new Color(0x95, 0xa5, 0xa6), // 8 - grey
    };
}
