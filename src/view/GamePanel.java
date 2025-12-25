package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// import dari model buat tipe data
import model.Bullet;
import model.Enemy;
import model.Obstacle;
import model.Player;

public class GamePanel extends JPanel {
    // KITA HAPUS REFERENCE KE MODEL!
    // Gantinya, kita siapin wadah buat data-data yang mau digambar
    
    // Objek Game
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private ArrayList<Obstacle> obstacles;
    
    // Aset Gambar (Diset sekali aja dari Presenter)
    private Image bgImage, uiBoard, uiButton;
    private Font pixelFont;
    
    // Data Status
    private String username = "";
    private int score = 0;
    private int ammo = 0;
    private int missed = 0;
    private int kills = 0;
    
    // State Game
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private int pressedButtonIndex = 0;

    public GamePanel() {
        this.setFocusable(true); 
        this.setBackground(Color.BLACK); 
    }

    // --- METODE BUAT UPDATE DATA (DIPANGGIL PRESENTER) ---
    // Ini cara Presenter "nyuapin" data ke View tanpa View tau soal Model
    public void setGameAssets(Image bg, Image board, Image btn, Font font) {
        this.bgImage = bg;
        this.uiBoard = board;
        this.uiButton = btn;
        this.pixelFont = font;
    }

    public void setGameObjects(Player p, ArrayList<Enemy> e, ArrayList<Bullet> b, ArrayList<Obstacle> o) {
        this.player = p;
        this.enemies = e;
        this.bullets = b;
        this.obstacles = o;
    }

    public void setGameStats(String user, int sc, int am, int ms, int kl, boolean over, boolean pause) {
        this.username = user;
        this.score = sc;
        this.ammo = am;
        this.missed = ms;
        this.kills = kl; // Data mateng
        this.isGameOver = over;
        this.isPaused = pause;
    }

    public void setPressedButtonIndex(int index) {
        this.pressedButtonIndex = index;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Pastikan asset udah ada biar ga NullPointerException
        if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        
        // Kalo Player belum diset (awal banget), jangan gambar dulu
        if (player == null) return;

        // 2. Gambar Player
        if (player.isFacingRight()) {
            // Kalau statusnya madep kanan -> Gambar dibalik
            drawFlipped(g, player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight());
        } else {
            // Kalau statusnya madep kiri -> Gambar normal
            g.drawImage(player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight(), null);
        }

        // 3. Gambar Musuh
        if (enemies != null) {
            for (Enemy e : enemies) {
                if (e.isFacingRight()) {
                    drawFlipped(g, e.getImage(), e.getPosX(), e.getPosY(), e.getWidth(), e.getHeight());
                } else {
                    g.drawImage(e.getImage(), e.getPosX(), e.getPosY(), e.getWidth(), e.getHeight(), null);
                }
            }
        }

        // 4. Gambar Peluru
        if (bullets != null) {
            for (Bullet b : bullets) {
                g.drawImage(b.getImage(), b.getPosX(), b.getPosY(), b.getWidth(), b.getHeight(), null);
            }
        }

        // 5. Gambar Batu
        if (obstacles != null) {
            for (Obstacle o : obstacles) {
                g.drawImage(o.getImage(), o.getPosX(), o.getPosY(), o.getWidth(), o.getHeight(), null);
            }
        }

        // --- 6. HUD ---
        drawHUD(g);

        // 7. Overlay Status
        if (isGameOver) {
            drawGameOverMenu(g);
        } else if (isPaused) {
            drawMenu(g, "PAUSED", "", "RESUME", "BACK TO MENU");
        }
    }
    
    private void drawHUD(Graphics g) {
        int hudX = 10;
        int hudY = 10;
        int hudW = 160; 
        int hudH = 105;
        
        if (uiBoard != null) g.drawImage(uiBoard, hudX, hudY, hudW, hudH, null);

        if (pixelFont != null) g.setFont(pixelFont.deriveFont(20f)); 
        else g.setFont(new Font("Arial", Font.BOLD, 16));

        g.setColor(Color.WHITE); 
        g.drawString("Score : " + score, hudX + 20, hudY + 35);
        g.drawString("Ammo  : " + ammo, hudX + 20, hudY + 60);
        g.drawString("Missed: " + missed, hudX + 20, hudY + 85);
    }

    private void drawGameOverMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int boardW = 380; 
        int boardH = 420; 
        int boardY = centerY - (boardH / 2);
        
        if (uiBoard != null) g.drawImage(uiBoard, centerX - (boardW / 2), boardY, boardW, boardH, null);

        Font mainFont = (pixelFont != null) ? pixelFont : new Font("SansSerif", Font.BOLD, 20);

        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(40f));
        drawCenteredString(g, "GAME OVER!", centerX, boardY + 85);

        g.setFont(mainFont.deriveFont(22f));
        g.setColor(Color.YELLOW); 
        
        // pake variabel kills yang dikirim Presenter
        drawCenteredString(g, "Username: " + username, centerX, boardY + 120);
        drawCenteredString(g, "Final Score: " + score, centerX, boardY + 150);
        drawCenteredString(g, "Total Vampcats Killed: " + kills, centerX, boardY + 180);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(mainFont.deriveFont(16f)); 
        drawCenteredString(g, "Click 'Back to Menu' to save & exit", centerX, boardY + 230);

        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(24f));
        
        int btnW = 220;
        int btnH = 60;
        int btnY = boardY + 260; 

        if (pressedButtonIndex == 2) {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2) + 2, btnY + 2, btnW - 4, btnH - 4, null);
        } else {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2), btnY, btnW, btnH, null);
        }
        
        drawCenteredString(g, "BACK TO MENU", centerX, btnY + 33);
    }

    private void drawMenu(Graphics g, String title, String infoText, String btn1Text, String btn2Text) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int boardW = 300; 
        int boardH = 350;
        int boardY = centerY - (boardH / 2);
        
        if(uiBoard != null) g.drawImage(uiBoard, centerX - (boardW / 2), boardY, boardW, boardH, null);

        Font mainFont = (pixelFont != null) ? pixelFont : new Font("SansSerif", Font.BOLD, 20);

        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(40f)); 
        drawCenteredString(g, title, centerX, boardY + 85); 

        if (!infoText.isEmpty()) {
            g.setFont(mainFont.deriveFont(20f)); 
            g.setColor(Color.YELLOW); 
            drawCenteredString(g, infoText, centerX, boardY + 115);
        }

        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(24f)); 

        int btnW = 200;
        int btnH = 60;
        int btn1Y = boardY + 140;
        
        if (pressedButtonIndex == 1) {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2) + 2, btn1Y + 2, btnW - 4, btnH - 4, null);
        } else {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2), btn1Y, btnW, btnH, null);
        }
        drawCenteredString(g, btn1Text, centerX, btn1Y + 33); 

        int btn2Y = boardY + 220; 
        if (pressedButtonIndex == 2) {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2) + 2, btn2Y + 2, btnW - 4, btnH - 4, null);
        } else {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2), btn2Y, btnW, btnH, null);
        }
        drawCenteredString(g, btn2Text, centerX, btn2Y + 33);
    }

    private void drawCenteredString(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        g.drawString(text, x - (textW / 2), y);
    }

    private void drawFlipped(Graphics g, Image img, int x, int y, int w, int h) {
        Graphics2D g2d = (Graphics2D) g;
        int imgWidth = img.getWidth(null);
        int imgHeight = img.getHeight(null);
        g2d.drawImage(img, x, y, x + w, y + h, imgWidth, 0, 0, imgHeight, null);
    }
}