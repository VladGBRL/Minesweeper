class CellFactory {
    public static Cell createCell(boolean isMine) {
        return new Cell(isMine);
    }
}