import javax.swing.JLabel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {
    public final int ROWS = 8;
    public final int COLS = 8;
    public final int TILE_SIZE = 85;
    private final int TOTAL_CANDY_IMAGES = 26;

    private Candy[][] grid;
    private BufferedImage[] candyImages;
    private BufferedImage bgImage;
    private Random rand = new Random();
    // position of click
    private int swapR1 = -1, swapC1 = -1, swapR2 = -1, swapC2 = -1;

    // score and moveleft
    public int score = 0;
    public int movesLeft = 20;
    public boolean isGameOver = false;
    public boolean isVictory = false;
    public static int bestScore = 0;
    private JLabel scoreLabel;

    public boolean inputLocked = false;
    public boolean needsGravity = false;

    public Board(JLabel scoreLabel) {
        this.scoreLabel = scoreLabel;
        grid = new Candy[ROWS][COLS];
        loadImages();
        initializeBoard();
    }

    private void loadImages() {
        candyImages = new BufferedImage[TOTAL_CANDY_IMAGES];
        String[] myImageFiles = {
                "Blue.png", "Blue-Striped-Horizontal.png", "Blue-Striped-Vertical.png", "Blue-Wrapped.png",
                "Green.png", "Green-Striped-Horizontal.png", "Green-Striped-Vertical.png", "Green-Wrapped.png",
                "Yellow.png", "Yellow-Striped-Horizontal.png", "Yellow-Striped-Vertical.png", "Yellow-Wrapped.png",
                "Orange.png", "Orange-Striped-Horizontal.png", "Orange-Striped-Vertical.png", "Orange-Wrapped.png",
                "Purple.png", "Purple-Striped-Horizontal.png", "Purple-Striped-Vertical.png", "Purple-Wrapped.png",
                "Red.png", "Red-Striped-Horizontal.png", "Red-Striped-Vertical.png", "Red-Wrapped.png",
                "Choco.png", "Blank.png"
        };

        for (int i = 0; i < TOTAL_CANDY_IMAGES; i++) {
            try {
                candyImages[i] = ImageIO.read(new File("assets/" + myImageFiles[i]));
            } catch (IOException e) {
                System.out.println("Thiếu ảnh: assets/" + myImageFiles[i]);
            }
        }

        try {
            bgImage = ImageIO.read(new File("assets/background.png"));
        } catch (IOException e) {
            bgImage = null;
        }
    }

    public BufferedImage getBlankImage() {
        return candyImages[25];
    }

    public BufferedImage getBackgroundImage() {
        return bgImage;
    }

    // logic use stripe horizonatl and vertical photo
    private BufferedImage getCandyImage(int color, SpecialType type) {
        if (type == SpecialType.CHOCO)
            return candyImages[24];
        int offset = 0;
        switch (type) {
            case NORMAL:
                offset = 0;
                break;
            case STRIPE_H:
                offset = 1;
                break;
            case STRIPE_V:
                offset = 2;
                break;
            case WRAPPED:
                offset = 3;
                break;
        }
        return candyImages[color * 4 + offset];
    }

    // prevent having 3 candy the same type spawn at game start
    private void initializeBoard() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int color;
                do {
                    color = rand.nextInt(6);
                } while ((c >= 2 && grid[r][c - 1] != null && grid[r][c - 1].color == color && grid[r][c - 2] != null
                        && grid[r][c - 2].color == color) ||
                        (r >= 2 && grid[r - 1][c] != null && grid[r - 1][c].color == color && grid[r - 2][c] != null
                                && grid[r - 2][c].color == color));
                grid[r][c] = new Candy(color, SpecialType.NORMAL, getCandyImage(color, SpecialType.NORMAL));
            }
        }
    }

    public void updateUI() {
        scoreLabel.setText("<html><center>SCORE<br><font size='6'>" + score + "</font><br><br>MOVES<br><font size='6'>"
                + movesLeft + "</font></center></html>");
    }

    // =========================================================
    // LOGIC CHECK DEADLOCK (KẸT MAP) VÀ AUTO SHUFFLE
    // =========================================================
    private boolean isMatchPresentFast() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS - 2; c++) {
                if (grid[r][c] != null && grid[r][c + 1] != null && grid[r][c + 2] != null &&
                        grid[r][c].color != -1 && grid[r][c].color == grid[r][c + 1].color
                        && grid[r][c].color == grid[r][c + 2].color)
                    return true;
            }
        }
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS - 2; r++) {
                if (grid[r][c] != null && grid[r + 1][c] != null && grid[r + 2][c] != null &&
                        grid[r][c].color != -1 && grid[r][c].color == grid[r + 1][c].color
                        && grid[r][c].color == grid[r + 2][c].color)
                    return true;
            }
        }
        return false;
    }

    public boolean hasPossibleMoves() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != null && grid[r][c].specialType == SpecialType.CHOCO)
                    return true; // Choco luôn có thể vuốt

                // Thử vuốt sang phải
                if (c < COLS - 1) {
                    swapCandies(r, c, r, c + 1);
                    boolean match = isMatchPresentFast();
                    swapCandies(r, c, r, c + 1);
                    if (match)
                        return true;
                }
                // Thử vuốt xuống dưới
                if (r < ROWS - 1) {
                    swapCandies(r, c, r + 1, c);
                    boolean match = isMatchPresentFast();
                    swapCandies(r, c, r + 1, c);
                    if (match)
                        return true;
                }
            }
        }
        return false;
    }

    public void shuffleBoard() {
        List<Candy> list = new ArrayList<>();
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] != null)
                    list.add(grid[r][c]);

        do {
            Collections.shuffle(list, rand);
            int idx = 0;
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (grid[r][c] != null)
                        grid[r][c] = list.get(idx++);
                }
            }
        } while (isMatchPresentFast() || !hasPossibleMoves());
        resetLastSwap();
    }
    // =========================================================

    public void activateChocoCombo(int r1, int c1, int r2, int c2) {
        Candy cA = grid[r1][c1];
        Candy cB = grid[r2][c2];
        if (cA == null || cB == null)
            return;

        Candy choco = (cA.specialType == SpecialType.CHOCO) ? cA : cB;
        Candy target = (cA.specialType == SpecialType.CHOCO) ? cB : cA;

        boolean[][] toRemove = new boolean[ROWS][COLS];
        toRemove[r1][c1] = true;
        toRemove[r2][c2] = true;

        if (target.specialType == SpecialType.CHOCO) {
            for (int r = 0; r < ROWS; r++)
                for (int c = 0; c < COLS; c++)
                    toRemove[r][c] = true;
            score += 200;
        } else if (target.specialType == SpecialType.STRIPE_H || target.specialType == SpecialType.STRIPE_V) {
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (grid[r][c] != null && grid[r][c].color == target.color) {
                        grid[r][c].specialType = rand.nextBoolean() ? SpecialType.STRIPE_H : SpecialType.STRIPE_V;
                        grid[r][c].image = getCandyImage(target.color, grid[r][c].specialType);
                        toRemove[r][c] = true;
                    }
                }
            }
            score += 100;
        } else if (target.specialType == SpecialType.WRAPPED) {
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (grid[r][c] != null && grid[r][c].color == target.color) {
                        grid[r][c].specialType = SpecialType.WRAPPED;
                        grid[r][c].image = getCandyImage(target.color, SpecialType.WRAPPED);
                        toRemove[r][c] = true;
                    }
                }
            }
            score += 150;
        } else {
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++)
                    if (grid[r][c] != null && grid[r][c].color == target.color)
                        toRemove[r][c] = true;
            }
            score += 50;
        }
        triggerSpecials(toRemove, null);
        executeRemoval(toRemove);
    }

    public void activateStripedStriped(int r, int c) {
        boolean[][] toRemove = new boolean[ROWS][COLS];
        for (int i = 0; i < COLS; i++)
            toRemove[r][i] = true;
        for (int i = 0; i < ROWS; i++)
            toRemove[i][c] = true;
        score += 40;
        triggerSpecials(toRemove, null);
        executeRemoval(toRemove);
    }

    public void activateWrappedWrapped(int r, int c) {
        boolean[][] toRemove = new boolean[ROWS][COLS];
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int tr = r + i, tc = c + j;
                if (tr >= 0 && tr < ROWS && tc >= 0 && tc < COLS)
                    toRemove[tr][tc] = true;
            }
        }
        score += 80;
        triggerSpecials(toRemove, null);
        executeRemoval(toRemove);
    }

    public void activateStripedWrapped(int r, int c) {
        boolean[][] toRemove = new boolean[ROWS][COLS];
        for (int i = -1; i <= 1; i++) {
            int tr = r + i, tc = c + i;
            if (tr >= 0 && tr < ROWS)
                for (int col = 0; col < COLS; col++)
                    toRemove[tr][col] = true;
            if (tc >= 0 && tc < COLS)
                for (int row = 0; row < ROWS; row++)
                    toRemove[row][tc] = true;
        }
        score += 60;
        triggerSpecials(toRemove, null);
        executeRemoval(toRemove);
    }

    private void executeRemoval(boolean[][] toRemove) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (toRemove[row][col] && grid[row][col] != null) {
                    grid[row][col] = null;
                    score += 10;
                }
            }
        }
        updateUI();
    }

    private void triggerSpecials(boolean[][] toRemove, Candy[][] newSpecials) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (toRemove[r][c] && grid[r][c] != null && grid[r][c].specialType != SpecialType.NORMAL
                            && !grid[r][c].activated) {
                        if (newSpecials != null && newSpecials[r][c] != null)
                            continue;
                        grid[r][c].activated = true;

                        if (grid[r][c].specialType == SpecialType.STRIPE_H) {
                            for (int i = 0; i < COLS; i++) {
                                if (!toRemove[r][i] && (newSpecials == null || newSpecials[r][i] == null)) {
                                    toRemove[r][i] = true;
                                    changed = true;
                                }
                            }
                        } else if (grid[r][c].specialType == SpecialType.STRIPE_V) {
                            for (int i = 0; i < ROWS; i++) {
                                if (!toRemove[i][c] && (newSpecials == null || newSpecials[i][c] == null)) {
                                    toRemove[i][c] = true;
                                    changed = true;
                                }
                            }
                        } else if (grid[r][c].specialType == SpecialType.WRAPPED) {
                            for (int rr = r - 1; rr <= r + 1; rr++) {
                                for (int cc = c - 1; cc <= c + 1; cc++) {
                                    if (rr >= 0 && rr < ROWS && cc >= 0 && cc < COLS && !toRemove[rr][cc]) {
                                        if (newSpecials == null || newSpecials[rr][cc] == null) {
                                            toRemove[rr][cc] = true;
                                            changed = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean checkMatches() {
        boolean[][] toRemove = new boolean[ROWS][COLS];
        int[][] hLength = new int[ROWS][COLS];
        int[][] vLength = new int[ROWS][COLS];
        boolean foundMatch = false;

        for (int r = 0; r < ROWS; r++) {
            int matchLen = 1;
            for (int c = 0; c < COLS - 1; c++) {
                if (grid[r][c] != null && grid[r][c + 1] != null && grid[r][c].color == grid[r][c + 1].color
                        && grid[r][c].color != -1) {
                    matchLen++;
                } else {
                    if (matchLen >= 3) {
                        for (int i = 0; i < matchLen; i++) {
                            toRemove[r][c - i] = true;
                            hLength[r][c - i] = matchLen;
                        }
                        foundMatch = true;
                    }
                    matchLen = 1;
                }
            }
            if (matchLen >= 3) {
                for (int i = 0; i < matchLen; i++) {
                    toRemove[r][COLS - 1 - i] = true;
                    hLength[r][COLS - 1 - i] = matchLen;
                }
                foundMatch = true;
            }
        }

        for (int c = 0; c < COLS; c++) {
            int matchLen = 1;
            for (int r = 0; r < ROWS - 1; r++) {
                if (grid[r][c] != null && grid[r + 1][c] != null && grid[r][c].color == grid[r + 1][c].color
                        && grid[r][c].color != -1) {
                    matchLen++;
                } else {
                    if (matchLen >= 3) {
                        for (int i = 0; i < matchLen; i++) {
                            toRemove[r - i][c] = true;
                            vLength[r - i][c] = matchLen;
                        }
                        foundMatch = true;
                    }
                    matchLen = 1;
                }
            }
            if (matchLen >= 3) {
                for (int i = 0; i < matchLen; i++) {
                    toRemove[ROWS - 1 - i][c] = true;
                    vLength[ROWS - 1 - i][c] = matchLen;
                }
                foundMatch = true;
            }
        }

        if (foundMatch) {
            Candy[][] newSpecialsToSpawn = new Candy[ROWS][COLS];
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (toRemove[r][c]) {
                        int hl = hLength[r][c];
                        int vl = vLength[r][c];
                        int color = grid[r][c].color;
                        boolean isSwapPoint = (r == swapR1 && c == swapC1) || (r == swapR2 && c == swapC2);
                        boolean isIntersection = (hl >= 3 && vl >= 3);
                        boolean isAnchor = isSwapPoint || isIntersection;

                        if (!isAnchor) {
                            if (hl >= 4 && vl < 3) {
                                boolean hasSwap = false;
                                for (int i = 0; i < COLS; i++)
                                    if (hLength[r][i] == hl
                                            && ((r == swapR1 && i == swapC1) || (r == swapR2 && i == swapC2)))
                                        hasSwap = true;
                                if (!hasSwap && (c == 0 || hLength[r][c - 1] != hl))
                                    isAnchor = true;
                            }
                            if (vl >= 4 && hl < 3) {
                                boolean hasSwap = false;
                                for (int i = 0; i < ROWS; i++)
                                    if (vLength[i][c] == vl
                                            && ((i == swapR1 && c == swapC1) || (i == swapR2 && c == swapC2)))
                                        hasSwap = true;
                                if (!hasSwap && (r == 0 || vLength[r - 1][c] != vl))
                                    isAnchor = true;
                            }
                        }

                        if (isAnchor) {
                            if (hl >= 5 || vl >= 5) {
                                newSpecialsToSpawn[r][c] = new Candy(-1, SpecialType.CHOCO,
                                        getCandyImage(-1, SpecialType.CHOCO));
                                toRemove[r][c] = false;
                                score += 50;
                            } else if (hl >= 3 && vl >= 3) {
                                newSpecialsToSpawn[r][c] = new Candy(color, SpecialType.WRAPPED,
                                        getCandyImage(color, SpecialType.WRAPPED));
                                toRemove[r][c] = false;
                                score += 40;
                            } else if (hl == 4) {
                                newSpecialsToSpawn[r][c] = new Candy(color, SpecialType.STRIPE_V,
                                        getCandyImage(color, SpecialType.STRIPE_V));
                                toRemove[r][c] = false;
                                score += 30;
                            } else if (vl == 4) {
                                newSpecialsToSpawn[r][c] = new Candy(color, SpecialType.STRIPE_H,
                                        getCandyImage(color, SpecialType.STRIPE_H));
                                toRemove[r][c] = false;
                                score += 30;
                            }
                        }
                    }
                }
            }

            triggerSpecials(toRemove, newSpecialsToSpawn);
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (newSpecialsToSpawn[r][c] != null)
                        grid[r][c] = newSpecialsToSpawn[r][c];
                    else if (toRemove[r][c]) {
                        grid[r][c] = null;
                        score += 10;
                    }
                }
            }
            updateUI();
        }
        return foundMatch;
    }

    public void applyGravity() {
        for (int c = 0; c < COLS; c++) {
            int emptyRow = ROWS - 1;
            for (int r = ROWS - 1; r >= 0; r--) {
                if (grid[r][c] != null) {
                    Candy temp = grid[r][c];
                    grid[r][c] = null;
                    grid[emptyRow][c] = temp;
                    emptyRow--;
                }
            }
            for (int r = emptyRow; r >= 0; r--) {
                int color = rand.nextInt(6);
                grid[r][c] = new Candy(color, SpecialType.NORMAL, getCandyImage(color, SpecialType.NORMAL));
            }
        }
    }

    public void swapCandies(int r1, int c1, int r2, int c2) {
        Candy temp = grid[r1][c1];
        grid[r1][c1] = grid[r2][c2];
        grid[r2][c2] = temp;
        swapR1 = r1;
        swapC1 = c1;
        swapR2 = r2;
        swapC2 = c2;
    }

    public Candy getCandy(int r, int c) {
        return grid[r][c];
    }

    public void resetLastSwap() {
        swapR1 = -1;
        swapC1 = -1;
        swapR2 = -1;
        swapC2 = -1;
    }

    // ==========================================
    // HÀM RESET GAME KHI CHƠI LẠI
    // ==========================================
    public void resetGame() {
        this.score = 0;
        this.movesLeft = 20; // Trả lại 20 lượt
        this.isGameOver = false;
        this.isVictory = false;
        this.inputLocked = false;
        this.needsGravity = false;
        resetLastSwap();

        // Tạo lại mảng kẹo mới tinh
        grid = new Candy[ROWS][COLS];
        initializeBoard();
        updateUI(); // Cập nhật lại bảng điểm UI bên trái
    }
}
