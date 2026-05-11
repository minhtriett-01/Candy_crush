import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;

public class GamePanel extends JPanel {
    private Board board;
    private InputHandler inputHandler;
    private Timer cascadeTimer;

    private boolean isAnimatingSwap = false;
    private int animR1, animC1, animR2, animC2;
    private float animProgress = 0f;
    private Timer animTimer;

    public GamePanel(Board board) {
        this.board = board;
        setOpaque(false);
        inputHandler = new InputHandler(board, this);
        addMouseListener(inputHandler);
        setupCascadeTimer();
    }

    // TÍNH TOÁN VỊ TRÍ ĐỂ CĂN GIỮA BẢNG GAME
    public int getOffsetX() {
        return (getWidth() - (board.COLS * board.TILE_SIZE)) / 2;
    }

    public int getOffsetY() {
        return (getHeight() - (board.ROWS * board.TILE_SIZE)) / 2;
    }

    private void setupCascadeTimer() {
        cascadeTimer = new Timer(350, e -> {
            if (board.needsGravity) {
                board.applyGravity();
                board.needsGravity = false;
                repaint();
            } else {
                if (board.checkMatches()) {
                    board.needsGravity = true;
                    repaint();
                } else {
                    cascadeTimer.stop();
                    // KIỂM TRA SHUFFLE VÀ GAME OVER
                    if (!board.hasPossibleMoves()) {
                        System.out.println("Deadlock detected! Shuffling board...");
                        board.shuffleBoard();
                        repaint();
                    }
                    if (board.movesLeft <= 0) {
                        board.isGameOver = true;
                        repaint();
                    }
                    board.inputLocked = false;
                    board.resetLastSwap();
                }
            }
        });
    }

    public void startCascade() {
        cascadeTimer.start();
    }

    public void playSwapAnimation(int r1, int c1, int r2, int c2, Runnable onComplete) {
        isAnimatingSwap = true;
        animR1 = r1;
        animC1 = c1;
        animR2 = r2;
        animC2 = c2;
        animProgress = 0f;

        if (animTimer != null && animTimer.isRunning())
            animTimer.stop();
        animTimer = new Timer(10, e -> {
            animProgress += 0.08f;
            if (animProgress >= 1.0f) {
                animProgress = 1.0f;
                animTimer.stop();
                isAnimatingSwap = false;
                onComplete.run();
            }
            repaint();
        });
        animTimer.start();
    }

    private void drawCandy(Graphics2D g2d, Candy candy, int pixelX, int pixelY) {
        if (candy != null && candy.image != null) {
            g2d.setColor(new Color(0, 0, 0, 70));
            g2d.fillOval(pixelX + 15, pixelY + 20, board.TILE_SIZE - 30, board.TILE_SIZE - 25);
            g2d.drawImage(candy.image, pixelX + 5, pixelY + 5, board.TILE_SIZE - 10, board.TILE_SIZE - 10, null);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ox = getOffsetX();
        int oy = getOffsetY();
        BufferedImage blankImage = board.getBlankImage();

        for (int r = 0; r < board.ROWS; r++) {
            for (int c = 0; c < board.COLS; c++) {
                int px = ox + c * board.TILE_SIZE;
                int py = oy + r * board.TILE_SIZE;

                if (blankImage != null) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.drawImage(blankImage, px + 2, py + 2, board.TILE_SIZE - 4, board.TILE_SIZE - 4, null);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }

                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(px, py, board.TILE_SIZE, board.TILE_SIZE);

                if (isAnimatingSwap && ((r == animR1 && c == animC1) || (r == animR2 && c == animC2)))
                    continue;

                Candy candy = board.getCandy(r, c);
                drawCandy(g2d, candy, px, py);

                if (r == inputHandler.getSelectedRow() && c == inputHandler.getSelectedCol()) {
                    g2d.setColor(new Color(255, 255, 255, 200));
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawRoundRect(px + 5, py + 5, board.TILE_SIZE - 10, board.TILE_SIZE - 10, 25, 25);
                }
            }
        }

        if (isAnimatingSwap) {
            Candy candy1 = board.getCandy(animR1, animC1);
            Candy candy2 = board.getCandy(animR2, animC2);

            int startX1 = ox + animC2 * board.TILE_SIZE;
            int startY1 = oy + animR2 * board.TILE_SIZE;
            int endX1 = ox + animC1 * board.TILE_SIZE;
            int endY1 = oy + animR1 * board.TILE_SIZE;
            int startX2 = ox + animC1 * board.TILE_SIZE;
            int startY2 = oy + animR1 * board.TILE_SIZE;
            int endX2 = ox + animC2 * board.TILE_SIZE;
            int endY2 = oy + animR2 * board.TILE_SIZE;

            drawCandy(g2d, candy1, (int) (startX1 + (endX1 - startX1) * animProgress),
                    (int) (startY1 + (endY1 - startY1) * animProgress));
            drawCandy(g2d, candy2, (int) (startX2 + (endX2 - startX2) * animProgress),
                    (int) (startY2 + (endY2 - startY2) * animProgress));
        }

        // VẼ MÀN HÌNH GAME OVER
        if (board.isGameOver) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(new Color(255, 100, 150));
            g2d.setFont(new Font("Arial", Font.BOLD, 80));
            String msg = "OUT OF MOVES!";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            g2d.drawString(msg, x, getHeight() / 2);
        }
    }
}