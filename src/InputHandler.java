import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InputHandler extends MouseAdapter {
    private Board board;
    private GamePanel panel;

    private int selectedRow = -1;
    private int selectedCol = -1;

    public InputHandler(Board board, GamePanel panel) {
        this.board = board;
        this.panel = panel;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // NẾU GAME ĐÃ KẾT THÚC -> Click chuột vào đâu cũng sẽ Reset game chơi lại
        if (board.isGameOver) {
            board.resetGame();
            panel.repaint();
            return;
        }

        // NẾU GAME ĐANG KHÓA CHUỘT (do đang rớt kẹo/trượt kẹo) -> Không cho click
        if (board.inputLocked)
            return;

        // Tính bù trừ khoảng cách Offset màn hình
        int col = (e.getX() - panel.getOffsetX()) / board.TILE_SIZE;
        // ... (GIỮ NGUYÊN TOÀN BỘ PHẦN CODE BÊN DƯỚI KHÔNG ĐỔI)
        int row = (e.getY() - panel.getOffsetY()) / board.TILE_SIZE;

        if (col < 0 || col >= board.COLS || row < 0 || row >= board.ROWS)
            return;

        if (selectedRow == -1 && selectedCol == -1) {
            selectedRow = row;
            selectedCol = col;
            panel.repaint();
        } else {
            boolean isAdjacent = (Math.abs(selectedRow - row) == 1 && selectedCol == col) ||
                    (Math.abs(selectedCol - col) == 1 && selectedRow == row);

            if (isAdjacent) {
                board.inputLocked = true;
                Candy c1 = board.getCandy(selectedRow, selectedCol);
                Candy c2 = board.getCandy(row, col);
                int sr1 = selectedRow, sc1 = selectedCol, sr2 = row, sc2 = col;

                boolean isC1 = c1 != null && c1.specialType == SpecialType.CHOCO;
                boolean isC2 = c2 != null && c2.specialType == SpecialType.CHOCO;
                boolean isS1 = c1 != null
                        && (c1.specialType == SpecialType.STRIPE_H || c1.specialType == SpecialType.STRIPE_V);
                boolean isS2 = c2 != null
                        && (c2.specialType == SpecialType.STRIPE_H || c2.specialType == SpecialType.STRIPE_V);
                boolean isW1 = c1 != null && c1.specialType == SpecialType.WRAPPED;
                boolean isW2 = c2 != null && c2.specialType == SpecialType.WRAPPED;

                boolean anyChoco = isC1 || isC2;
                boolean stripedStriped = isS1 && isS2;
                boolean wrappedWrapped = isW1 && isW2;
                boolean stripedWrapped = (isS1 && isW2) || (isW1 && isS2);

                if (anyChoco || stripedStriped || wrappedWrapped || stripedWrapped) {
                    board.swapCandies(sr1, sc1, sr2, sc2);
                    board.movesLeft--; // TRỪ LƯỢT ĐI KHI COMBO
                    board.updateUI();

                    panel.playSwapAnimation(sr1, sc1, sr2, sc2, () -> {
                        if (anyChoco)
                            board.activateChocoCombo(sr1, sc1, sr2, sc2);
                        else if (stripedStriped)
                            board.activateStripedStriped(sr2, sc2);
                        else if (wrappedWrapped)
                            board.activateWrappedWrapped(sr2, sc2);
                        else if (stripedWrapped)
                            board.activateStripedWrapped(sr2, sc2);

                        board.needsGravity = true;
                        panel.startCascade();
                    });
                } else {
                    board.swapCandies(sr1, sc1, sr2, sc2);
                    panel.playSwapAnimation(sr1, sc1, sr2, sc2, () -> {
                        if (board.checkMatches()) {
                            board.movesLeft--; // TRỪ LƯỢT ĐI KHI ĂN ĐƯỢC KẸO
                            board.updateUI();
                            board.needsGravity = true;
                            panel.startCascade();
                        } else {
                            board.swapCandies(sr1, sc1, sr2, sc2);
                            panel.playSwapAnimation(sr1, sc1, sr2, sc2, () -> {
                                board.inputLocked = false;
                            });
                        }
                    });
                }
            }
            selectedRow = -1;
            selectedCol = -1;
            panel.repaint();
        }
    }
}
