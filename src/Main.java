import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java Candy Crush - Masterpiece");
            CardLayout cardLayout = new CardLayout();
            JPanel rootPanel = new JPanel(cardLayout);

            BufferedImage tempBg = null;
            try {
                tempBg = ImageIO.read(new File("assets/menu_bg.png"));
            } catch (Exception e) {
            }
            final BufferedImage menuBgImage = tempBg;

            // 1. Dùng BorderLayout thay vì GridBagLayout
            JPanel menuPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (menuBgImage != null)
                        g.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
                    else {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setPaint(new GradientPaint(0, 0, new Color(20, 10, 40), getWidth(), getHeight(),
                                new Color(80, 20, 100)));
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };

            JButton playBtn = new JButton("PLAY GAME");
            playBtn.setFont(new Font("Arial", Font.BOLD, 32));
            playBtn.setForeground(Color.WHITE);
            playBtn.setBackground(new Color(255, 80, 120));
            playBtn.setFocusPainted(false);
            playBtn.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50));
            playBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            playBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    playBtn.setBackground(new Color(255, 120, 150));
                }

                public void mouseExited(MouseEvent e) {
                    playBtn.setBackground(new Color(255, 80, 120));
                }
            });

            // 2. Tạo một lớp đệm (bottomPanel) để chứa nút Play
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.setOpaque(false); // Làm trong suốt để thấy ảnh nền
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 80, 0)); // Cách mép đáy 80px (sửa ở đây nếu muốn cao/thấp)
            bottomPanel.add(playBtn);

            // 3. Khóa chết vị trí: Chữ ở TÂM, Nút ở ĐÁY
            menuPanel.add(bottomPanel, BorderLayout.SOUTH);

            // Bảng điểm Update hiển thị cả Điểm và Lượt đi
            JLabel scoreLabel = new JLabel(
                    "<html><center>SCORE<br><font size='6'>0</font><br><br>MOVES<br><font size='6'>20</font></center></html>",
                    SwingConstants.CENTER);
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 28));
            scoreLabel.setForeground(Color.WHITE);

            JPanel leftPanel = new JPanel(new GridBagLayout());
            leftPanel.setPreferredSize(new Dimension(300, 0));
            leftPanel.setOpaque(false);
            leftPanel.add(scoreLabel);

            Board board = new Board(scoreLabel);
            GamePanel gamePanel = new GamePanel(board);

            JPanel gameContainer = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    BufferedImage bg = board.getBackgroundImage();
                    if (bg != null)
                        g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
                    else {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setPaint(new GradientPaint(0, 0, new Color(30, 15, 50), getWidth(), getHeight(),
                                new Color(70, 30, 90)));
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };

            gameContainer.add(leftPanel, BorderLayout.WEST);
            gameContainer.add(gamePanel, BorderLayout.CENTER);

            rootPanel.add(menuPanel, "MENU");
            rootPanel.add(gameContainer, "GAME");

            playBtn.addActionListener(e -> cardLayout.show(rootPanel, "GAME"));

            frame.add(rootPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Full-screen support 
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        });
    }
}