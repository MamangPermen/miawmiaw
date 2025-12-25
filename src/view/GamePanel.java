package view;

import model.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private Model model;

    public GamePanel(Model model) {
        this.model = model;
        this.setFocusable(true); // Biar bisa baca input keyboard
        this.setBackground(Color.BLACK); // Background hitam biar kerasa luar angkasa
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Gambar Background
        g.drawImage(model.getBgImage(), 0, 0, getWidth(), getHeight(), null);

        // 2. Gambar Player
        Player p = model.getPlayer();
        // Cek Velocity X
        if (p.getVelX() > 0) {
            // Lagi jalan ke KANAN -> Gambar asli (Kiri) harus DIBALIK
            drawFlipped(g, p.getImage(), p.getPosX(), p.getPosY(), p.getWidth(), p.getHeight());
        } else {
            // Lagi jalan ke KIRI (atau diem) -> Gambar asli (Kiri) biarin NORMAL
            g.drawImage(p.getImage(), p.getPosX(), p.getPosY(), p.getWidth(), p.getHeight(), null);
        }

        // 3. Gambar Musuh
        ArrayList<Enemy> enemies = model.getEnemies();
        for (Enemy e : enemies) {
            if (e.isFacingRight()) {
                // Musuh lagi mau ke KANAN -> Gambar asli (Kiri) harus DIBALIK
                drawFlipped(g, e.getImage(), e.getPosX(), e.getPosY(), e.getWidth(), e.getHeight());
            } else {
                // Musuh lagi mau ke KIRI -> Gambar asli (Kiri) biarin NORMAL
                g.drawImage(e.getImage(), e.getPosX(), e.getPosY(), e.getWidth(), e.getHeight(), null);
            }
        }

        // 4. Gambar Peluru
        ArrayList<Bullet> bullets = model.getBullets();
        for (Bullet b : bullets) {
            g.drawImage(b.getImage(), b.getPosX(), b.getPosY(), b.getWidth(), b.getHeight(), null);
        }

        // 5. Gambar Batu (Obstacle)
        ArrayList<Obstacle> obstacles = model.getObstacles();
        for (Obstacle o : obstacles) {
            g.drawImage(o.getImage(), o.getPosX(), o.getPosY(), o.getWidth(), o.getHeight(), null);
        }

        // --- 6. HUD (SCORE & AMMO) DENGAN BOARD ---
        
        // A. Gambar Papan Background
        int hudX = 10;
        int hudY = 10;
        int hudW = 160; 
        int hudH = 105; // [FIX] Gedein dari 80 jadi 105 biar muat 3 baris
        
        if (model.getUiBoard() != null) {
            g.drawImage(model.getUiBoard(), hudX, hudY, hudW, hudH, null);
        }

        // B. Set Font
        if (model.getPixelFont() != null) {
            g.setFont(model.getPixelFont().deriveFont(20f)); 
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 16));
        }

        // C. Tulis Info
        g.setColor(Color.WHITE); 
        
        // Jarak antar baris sekitar 25px
        g.drawString("Score : " + model.getPlayer().getScore(), hudX + 20, hudY + 35);
        g.drawString("Ammo  : " + model.getPlayer().getAmmo(), hudX + 20, hudY + 60);
        g.drawString("Missed: " + model.getPlayer().getMissedBullets(), hudX + 20, hudY + 85);

        // 7. Overlay Status (Pause / Game Over)
        if (model.isGameOver()) {
            drawGameOverMenu(g);
        } else if (model.isPaused()) {
            drawMenu(g, "PAUSED", "", "RESUME", "BACK TO MENU");
        }
    }

    private void drawGameOverMenu(Graphics g) {
        // 1. Gelapin Layar
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // 2. Gambar Papan (Background Menu)
        int boardW = 380; // Agak lebarin dikit biar muat teks panjang
        int boardH = 420; // Tinggian dikit
        int boardY = centerY - (boardH / 2);
        
        if (model.getUiBoard() != null) {
            g.drawImage(model.getUiBoard(), centerX - (boardW / 2), boardY, boardW, boardH, null);
        }

        // 3. Setup Font
        Font mainFont = model.getPixelFont();
        if (mainFont == null) mainFont = new Font("SansSerif", Font.BOLD, 20);

        // --- JUDUL ---
        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(40f));
        drawCenteredString(g, "GAME OVER!", centerX, boardY + 85);

        // --- STATISTIK ---
        g.setFont(mainFont.deriveFont(22f));
        g.setColor(Color.YELLOW); // Warna kuning biar kontras
        
        // Hitung Kills (Asumsi 1 kill = 10 score)
        int kills = model.getPlayer().getScore() / 10; 

        // Tampilin Data
        drawCenteredString(g, "Username: " + model.getUsername(), centerX, boardY + 120);
        drawCenteredString(g, "Final Score: " + model.getPlayer().getScore(), centerX, boardY + 150);
        drawCenteredString(g, "Vampcats Killed: " + kills, centerX, boardY + 180);

        // --- INSTRUKSI SAVE ---
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(mainFont.deriveFont(16f)); // Font kecil aja
        drawCenteredString(g, "Click 'Back to Menu' to save & exit", centerX, boardY + 230);

        // --- TOMBOL SATU-SATUNYA (BACK TO MENU) ---
        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(24f));
        
        int btnW = 220;
        int btnH = 60;
        int btnY = boardY + 260; // Posisi di bawah teks instruksi

        // Cek Efek Klik (Kita pake index 2 buat Back Menu)
        if (model.getPressedButtonIndex() == 2) {
            g.drawImage(model.getUiButton(), centerX - (btnW / 2) + 2, btnY + 2, btnW - 4, btnH - 4, null);
        } else {
            g.drawImage(model.getUiButton(), centerX - (btnW / 2), btnY, btnW, btnH, null);
        }
        
        drawCenteredString(g, "BACK TO MENU", centerX, btnY + 33);
    }

    private void drawMenu(Graphics g, String title, String infoText, String btn1Text, String btn2Text) {
        // 1. Gelapin Layar
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // 2. Gambar Papan
        int boardW = 300; 
        int boardH = 350;
        int boardY = centerY - (boardH / 2);
        g.drawImage(model.getUiBoard(), centerX - (boardW / 2), boardY, boardW, boardH, null);

        // --- FIX FONT DISINI ---
        // Kita paksa ambil font dari Model lagi, jangan pake g.getFont() (karena itu isinya Arial bekas HUD)
        Font mainFont = model.getPixelFont();
        if (mainFont == null) mainFont = new Font("SansSerif", Font.BOLD, 20); // Cadangan

        // 3. JUDUL (GAME OVER / PAUSED)
        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(40f)); // Set Pixel Font Ukuran 40
        // [FIX POSISI] Turunin angka Y dari +60 jadi +85 biar gak nabrak bingkai atas
        drawCenteredString(g, title, centerX, boardY + 85); 

        // 4. SKOR
        if (!infoText.isEmpty()) {
            g.setFont(mainFont.deriveFont(20f)); // Set Pixel Font Ukuran 20
            g.setColor(Color.YELLOW); 
            // [FIX POSISI] Turunin dikit
            drawCenteredString(g, infoText, centerX, boardY + 115);
        }

        // Balikin warna putih buat tombol
        g.setColor(Color.WHITE);
        g.setFont(mainFont.deriveFont(24f)); // Set Pixel Font Ukuran 24 buat Tombol

        // --- UPDATE BAGIAN GAMBAR TOMBOL ---
        int btnW = 200;
        int btnH = 60;
        
        // --- TOMBOL 1 (ATAS) ---
        int btn1Y = boardY + 140;
        if (model.getPressedButtonIndex() == 1) {
            // EFEK DITEKAN: Kecilin ukuran 4px, geser kanan-bawah 2px
            g.drawImage(model.getUiButton(), centerX - (btnW / 2) + 2, btn1Y + 2, btnW - 4, btnH - 4, null);
        } else {
            // NORMAL
            g.drawImage(model.getUiButton(), centerX - (btnW / 2), btn1Y, btnW, btnH, null);
        }
        drawCenteredString(g, btn1Text, centerX, btn1Y + 33); 

        // --- TOMBOL 2 (BAWAH) ---
        int btn2Y = boardY + 220; 
        if (model.getPressedButtonIndex() == 2) {
            // EFEK DITEKAN
            g.drawImage(model.getUiButton(), centerX - (btnW / 2) + 2, btn2Y + 2, btnW - 4, btnH - 4, null);
        } else {
            // NORMAL
            g.drawImage(model.getUiButton(), centerX - (btnW / 2), btn2Y, btnW, btnH, null);
        }
        drawCenteredString(g, btn2Text, centerX, btn2Y + 33);
    }

    // Helper buat nengahin tulisan
    private void drawCenteredString(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        g.drawString(text, x - (textW / 2), y);
    }

    // Helper buat bikin tulisan di tengah
    private void drawOverlay(Graphics g, String title, String sub) {
        g.setColor(new Color(0, 0, 0, 150)); // Layar agak gelap transparan
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, x, getHeight() / 2 - 20);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(sub)) / 2;
        g.drawString(sub, x, getHeight() / 2 + 20);
    }

    // Helper buat gambar terbalik (mirror)
    private void drawFlipped(Graphics g, Image img, int x, int y, int w, int h) {
        Graphics2D g2d = (Graphics2D) g;
        
        // 1. Ambil ukuran ASLI gambar (dari file png-nya)
        int imgWidth = img.getWidth(null);
        int imgHeight = img.getHeight(null);
        
        // 2. Gambar dengan membalik koordinat Sumber (Source)
        // Parameter: (img, destX1, destY1, destX2, destY2, srcX1, srcY1, srcX2, srcY2, observer)
        
        // Source X1 = imgWidth (Kanan), Source X2 = 0 (Kiri) -> Ini yang bikin kebalik
        // Kita tarik FULL dari ujung kanan ke ujung kiri gambar asli, lalu tempel ke kotak tujuan (w, h)
        g2d.drawImage(img, x, y, x + w, y + h, imgWidth, 0, 0, imgHeight, null);
    }
}