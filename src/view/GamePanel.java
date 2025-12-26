package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// import dari model buat tipe data
import model.Bullet;
import model.Enemy;
import model.Obstacle;
import model.Player;

// Kelas gamepanel adalah panel khusus buat ngegambar in game
// kelas ini berfungsi sebagai wadah visualisasi game

public class GamePanel extends JPanel 
{    
    // Objek Game
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private ArrayList<Obstacle> obstacles;
    
    // Aset Gambar (Diset sekali aja dari Presenter)
    private Image bgImage, uiBoard, uiButton; // aset UI
    private Font pixelFont; // font pixel
    
    // Data Status
    private String username = "";
    private int score = 0;
    private int ammo = 0;
    private int missed = 0;
    private int kills = 0;
    
    // State Game
    private boolean isGameOver = false; // status game over
    private boolean isPaused = false; // status pause
    private int pressedButtonIndex = 0; // index tombol yang ditekan (buat efek tombol)

    // Konstruktor
    public GamePanel() {
        this.setFocusable(true); 
        this.setBackground(Color.BLACK); 
    }

    // metode buat nyetel aset game dari presenter
    public void setGameAssets(Image bg, Image board, Image btn, Font font) {
        this.bgImage = bg;
        this.uiBoard = board;
        this.uiButton = btn;
        this.pixelFont = font;
    }

    // metode buat nyetel objek game dari presenter
    public void setGameObjects(Player p, ArrayList<Enemy> e, ArrayList<Bullet> b, ArrayList<Obstacle> o) {
        this.player = p;
        this.enemies = e;
        this.bullets = b;
        this.obstacles = o;
    }

    // metode buat nyetel data status game dari presenter
    public void setGameStats(String user, int sc, int am, int ms, int kl, boolean over, boolean pause) {
        this.username = user;
        this.score = sc;
        this.ammo = am;
        this.missed = ms;
        this.kills = kl; // Data mateng
        this.isGameOver = over;
        this.isPaused = pause;
    }

    // metode buat nyetel index tombol yang ditekan (buat efek tekan tombol)
    public void setPressedButtonIndex(int index) {
        this.pressedButtonIndex = index;
    }

    // metode paintComponent buat ngegambar ulang panel
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Pastikan asset udah ada biar ga NullPointerException
        if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        
        // Kalo Player belum diset, jangan gambar dulu
        if (player == null) return;

        // Gambar Player
        if (player.isFacingRight()) {
            // Kalau statusnya madep kanan -> Gambar dibalik
            drawFlipped(g, player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight());
        } else {
            // Kalau statusnya madep kiri -> Gambar normal
            g.drawImage(player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight(), null);
        }

        // Gambar Musuh
        if (enemies != null) {
            for (Enemy e : enemies) {
                if (e.isFacingRight()) {
                    drawFlipped(g, e.getImage(), e.getPosX(), e.getPosY(), e.getWidth(), e.getHeight());
                } else {
                    g.drawImage(e.getImage(), e.getPosX(), e.getPosY(), e.getWidth(), e.getHeight(), null);
                }
            }
        }

        // Gambar Peluru
        if (bullets != null) {
            for (Bullet b : bullets) {
                g.drawImage(b.getImage(), b.getPosX(), b.getPosY(), b.getWidth(), b.getHeight(), null);
            }
        }

        // Gambar Box
        if (obstacles != null) {
            for (Obstacle o : obstacles) {
                g.drawImage(o.getImage(), o.getPosX(), o.getPosY(), o.getWidth(), o.getHeight(), null);
            }
        }

        // HUD
        drawHUD(g);

        // Overlay Status
        if (isGameOver) {
            drawGameOverMenu(g);
        } else if (isPaused) {
            drawMenu(g, "PAUSED", "", "RESUME", "BACK TO MENU");
        }
    }
    
    // metode bantu buat gambar HUD
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

    // metode bantu buat gambar menu game over
    private void drawGameOverMenu(Graphics g) {
        // Latar Gelap
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Board Game Over
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int boardW = 380; 
        int boardH = 420; 
        int boardY = centerY - (boardH / 2);
        
        if (uiBoard != null) g.drawImage(uiBoard, centerX - (boardW / 2), boardY, boardW, boardH, null); // Gambar board
        Font mainFont = (pixelFont != null) ? pixelFont : new Font("SansSerif", Font.BOLD, 20); // set font

        // Judul
        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(40f));
        drawCenteredString(g, "GAME OVER!", centerX, boardY + 85);
        // Info Stats
        g.setFont(mainFont.deriveFont(22f));
        g.setColor(Color.YELLOW); 
        drawCenteredString(g, "Username: " + username, centerX, boardY + 120);
        drawCenteredString(g, "Final Score: " + score, centerX, boardY + 150);
        drawCenteredString(g, "Total Vampcats Killed: " + kills, centerX, boardY + 180);
        // Instruksi
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(mainFont.deriveFont(16f)); 
        drawCenteredString(g, "Click 'Back to Menu' to save & exit", centerX, boardY + 230);
        // Tombol Back to Menu
        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(24f));
        
        int btnW = 220;
        int btnH = 60;
        int btnY = boardY + 260; 

        if (pressedButtonIndex == 2) { // Tombol ditekan
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2) + 2, btnY + 2, btnW - 4, btnH - 4, null);
        } else { // Tombol normal
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2), btnY, btnW, btnH, null);
        }
        
        drawCenteredString(g, "BACK TO MENU", centerX, btnY + 33);
    }

    // metode bantu buat gambar menu pause
    private void drawMenu(Graphics g, String title, String infoText, String btn1Text, String btn2Text) {
        // Latar Gelap
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int boardW = 300; 
        int boardH = 350;
        int boardY = centerY - (boardH / 2);
        
        if(uiBoard != null) g.drawImage(uiBoard, centerX - (boardW / 2), boardY, boardW, boardH, null); // Gambar board
        Font mainFont = (pixelFont != null) ? pixelFont : new Font("SansSerif", Font.BOLD, 20); // set font

        // judul
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
        
        // Tombol 1
        int btn1Y = boardY + 140;
        if (pressedButtonIndex == 1) {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2) + 2, btn1Y + 2, btnW - 4, btnH - 4, null);
        } else {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2), btn1Y, btnW, btnH, null);
        }
        drawCenteredString(g, btn1Text, centerX, btn1Y + 33); 

        // Tombol 2
        int btn2Y = boardY + 220; 
        if (pressedButtonIndex == 2) {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2) + 2, btn2Y + 2, btnW - 4, btnH - 4, null);
        } else {
            if(uiButton != null) g.drawImage(uiButton, centerX - (btnW / 2), btn2Y, btnW, btnH, null);
        }
        drawCenteredString(g, btn2Text, centerX, btn2Y + 33);
    }

    // metode bantu buat gambar string di tengah
    private void drawCenteredString(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        g.drawString(text, x - (textW / 2), y);
    }

    // metode bantu buat gambar gambar dibalik (mirror), digunakan buat player & musuh saat menghadap kanan
    private void drawFlipped(Graphics g, Image img, int x, int y, int w, int h) {
        Graphics2D g2d = (Graphics2D) g; // cast ke Graphics2D buat transformasi
        int imgWidth = img.getWidth(null);
        int imgHeight = img.getHeight(null);
        g2d.drawImage(img, x, y, x + w, y + h, imgWidth, 0, 0, imgHeight, null);
    }
}