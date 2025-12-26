package model;

import java.awt.*;
import java.util.*;
import javax.swing.ImageIcon;

// Kelas Model adalah kelas utama yang menyimpan semua data dan logika game
// Kelas ini bertanggung jawab menyimpan state game (posisi player, musuh, skor),
// menghitung fisika (pergerakan & tabrakan), serta berinteraksi dengan Database 
// untuk menyimpan/memuat progress user.

public class Model 
{     
    // KONFIGURASI LAYAR
    int frameWidth = 1024;
    int frameHeight = 768;

    // KOMPONEN 
    private Database db;

    // OBJEK GAME 
    Player player;
    ArrayList<Enemy> enemies;
    ArrayList<Bullet> bullets;
    ArrayList<Obstacle> obstacles;

    // GAMBAR
    Image bgImage, playerImage, enemyImage, playerBulletImage, obsImage, enemyBulletImage;
    Image uiBoard, uiButton;

    // FONT
    Font pixelFont;

    // GAME STATE
    boolean gameOver = false;
    boolean gameStarted = false;
    boolean isPaused = false;
    
    // STATISTIK PLAYER
    String username;
    int score = 0;
    int missedBulletsCounter = 0;

    // Lainnya
    int pressedButtonIndex = 0; // index tombol yang ditekan
    private Sound sfxModel = new Sound(); // buat mainin SFX

    // KONSTRUKTOR
    public Model(String username) {
        this.username = username; // simpan username player
        this.db = new Database(); // inisialisasi koneksi database

        loadImages(); // load aset gambar

        // spawn player di tengah layar
        player = new Player(frameWidth / 2 - 32, frameHeight / 2 - 32, 64, 64, playerImage, username);
        
        // load data user dari database
        loadUserData();

        // inisialisasi list objek game
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        obstacles = new ArrayList<>();

        generateObstacles(); // buat obstacle di awal game
    }

    private void loadUserData() {
        java.sql.ResultSet rs = null;
        try {
            // Cek apakah user ini udah pernah main sebelumnya?
            String sql = "SELECT * FROM tbenefit WHERE username = '" + username + "'";
            rs = db.executeQuery(sql);
            
            if (rs.next()) {
                // KALO ADA DATA LAMA (USER LAMA)
                // Kita ambil "Tabungan" skor dan peluru dia
                this.score = rs.getInt("skor");
                this.missedBulletsCounter = rs.getInt("missed_bullets");
                int savedAmmo = rs.getInt("ammo");

                // Set ke Player & Model
                player.setScore(this.score);
                player.setAmmo(savedAmmo);
                player.setMissedBullets(this.missedBulletsCounter);
            } else {
                // KALO USER BARU (Belum ada di DB)
                // Set Default Awal
                this.score = 0;
                this.missedBulletsCounter = 0;
                player.setScore(0);
                player.setAmmo(0); // Modal Awal User Baru
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // tutup ResultSet
            try {
                if (rs != null) {
                    // Nutup Statement otomatis nutup ResultSet juga
                    rs.getStatement().close(); 
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    // Metode buat load gambar dan font dari folder assets
    private void loadImages() {
        try {
            // Langsung panggil nama file yang udah lu crop tadi
            bgImage = new ImageIcon(getClass().getResource("/assets/Background.png")).getImage();
            playerImage = new ImageIcon(getClass().getResource("/assets/Main.png")).getImage();
            enemyImage = new ImageIcon(getClass().getResource("/assets/Vampire.png")).getImage();
            playerBulletImage = new ImageIcon(getClass().getResource("/assets/Fire.png")).getImage();
            obsImage = new ImageIcon(getClass().getResource("/assets/Box.png")).getImage();
            enemyBulletImage = new ImageIcon(getClass().getResource("/assets/BulletEnemy.png")).getImage();
            uiBoard = new ImageIcon(getClass().getResource("/assets/ui_board.png")).getImage();
            uiButton = new ImageIcon(getClass().getResource("/assets/ui_button.png")).getImage();
            
            // Pake Try-With-Resources buat InputStream Font
            try (java.io.InputStream is = getClass().getResourceAsStream("/assets/pixel_font.ttf")) {
                pixelFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(24f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(pixelFont);
            }
        } catch (Exception e) {
            pixelFont = new Font("SansSerif", Font.BOLD, 24);
            e.printStackTrace(); 
        }
    }

    // LOGIC UTAMA (metode untuk update logika game setiap frame)
    public void updateGameLogic() {
        // Cek status dipindah ke Presenter
        if (gameOver || !gameStarted || isPaused) {
            return; 
        }

        // UPDATE PLAYER
        int pPrevX = player.getPosX();
        int pPrevY = player.getPosY();
        player.update();
        
        // Batas Layar Player
        if (player.getPosX() < 0) player.setPosX(0);
        if (player.getPosX() > frameWidth - player.getWidth()) player.setPosX(frameWidth - player.getWidth());
        if (player.getPosY() < 0) player.setPosY(0);
        if (player.getPosY() > frameHeight - player.getHeight()) player.setPosY(frameHeight - player.getHeight());

        // Logika untuk gabisa nembus obstacle
        Rectangle playerRect = player.getBounds();
        for (Obstacle o : obstacles) {
            if (playerRect.intersects(o.getBounds())) {
                player.setPosX(pPrevX);
                player.setPosY(pPrevY);
            }
        }
        
        // UPDATE MUSUH
        for (Enemy en : enemies) {
            int ePrevX = en.getPosX();
            int ePrevY = en.getPosY(); 
            en.update(); 
            en.tickCooldown(); // kurangi cooldown biar gak nembak sembarangan

            // A. LOGIKA TABRAKAN box (SLIDING)
            Rectangle enemyRect = en.getBounds();
            for (Obstacle o : obstacles) {
                if (enemyRect.intersects(o.getBounds())) {
                    en.setPosY(ePrevY); // Balikin Y biar gak nembus
                    
                    double enemyCenter = en.getPosX() + (en.getWidth() / 2.0);
                    double obstacleCenter = o.getPosX() + (o.getWidth() / 2.0);
                    
                    // Logic Geser Kiri/Kanan
                    if (enemyCenter < obstacleCenter) {
                        // Geser ke KIRI (Speed naik jadi 3 biar licin)
                        en.setPosX(en.getPosX() - 3);
                        en.setFacingRight(false); // Madep Kiri (Normal)
                    } else {
                        // Geser ke KANAN
                        en.setPosX(en.getPosX() + 3);
                        en.setFacingRight(true);  // Madep Kanan (Flip)
                    }
                }
            }

            // logic nembak ke player
            if (en.getShootCooldown() <= 0) {
                shootEnemyBullet(en);
                en.resetCooldown(); // Reset timer biar nunggu lagi
            }
        }

        // UPDATE PELURU
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update(); 

            if (b.isEnemy()) {
                boolean kenaAtas = b.getPosY() <= 0;
                boolean kenaKiri = b.getPosX() <= 0;
                boolean kenaKanan = b.getPosX() >= frameWidth - b.getWidth();
                boolean kenaBawah = b.getPosY() >= frameHeight;

                if (kenaAtas || kenaKiri || kenaKanan || kenaBawah) {
                    handleMissedBullet(b, i); 
                    i--; 
                    continue; 
                }
            }
        }

        // CEK TABRAKAN LAINNYA
        checkCollisions();
        cleanupObjects();
    }
    
    // Metode buat spawn musuh di posisi acak di bawah layar
    public void spawnEnemy() {
        Random rand = new Random();
        int randomX = rand.nextInt(frameWidth - 100);
        Enemy vampire = new Enemy(randomX, frameHeight, 64, 64, enemyImage);
        vampire.setVelY(-2); 
        enemies.add(vampire);
    }

    // Metode buat generate obstacle di posisi acak
    public void generateObstacles() {
        Random rand = new Random();
        obstacles.clear(); // Pastikan list bersih sebelum mulai
        
        // Tentukan Jumlah box (4-7)
        int count = rand.nextInt(3) + 4;

        int attempts = 0;
        while (obstacles.size() < count && attempts < 100) {
            attempts++;

            // ukuran box random antara 50px sampai 120px
            int size = rand.nextInt(71) + 80; 

            // Tentukan Posisi Random
            int x = rand.nextInt(frameWidth - size - 100) + 50;
            int y = rand.nextInt(frameHeight - size - 100) + 50;
            
            // Bikin kotak bayangan buat ngecek tabrakan
            Rectangle newRect = new Rectangle(x, y, size, size);
            boolean aman = true;

            // CEK 1: JANGAN DEKETAN SAMA box LAIN
            int j = 0;
            // Loop jalan selama masih ada box DAN status masih aman
            while (j < obstacles.size() && aman) {
                Obstacle o = obstacles.get(j);
                Rectangle existing = o.getBounds();
                
                // Area aman
                Rectangle safeZone = new Rectangle(
                    existing.x - 100, existing.y - 100, 
                    existing.width + 200, existing.height + 200
                );

                if (safeZone.intersects(newRect)) {
                    aman = false;
                }
                j++;
            }
            
            // CEK 2: JANGAN MUNCUL DI TENGAH (TEMPAT PLAYER)
            if (aman) {
                Rectangle playerSpawnZone = new Rectangle(
                    frameWidth / 2 - 100, frameHeight / 2 - 100, 200, 200
                );
                
                if (playerSpawnZone.intersects(newRect)) {
                    aman = false;
                }
            }

            // Kalo aman, baru masukin ke game
            if (aman) {
                obstacles.add(new Obstacle(x, y, size, size, obsImage));
            }
        }
    }

    // Metode buat nembak peluru musuh ke arah player
    private void shootEnemyBullet(Enemy en) {
        // tentuin target (posisi player)
        int startX = en.getPosX() + en.getWidth() / 2;
        int startY = en.getPosY(); 
        int targetX = player.getPosX() + player.getWidth() / 2;
        int targetY = player.getPosY() + player.getHeight() / 2;
        
        // hitung velocity biar nembak ke player
        double diffX = targetX - startX;
        double diffY = targetY - startY;
        double distance = Math.sqrt((diffX * diffX) + (diffY * diffY));
        
        int speed = 5; 
        int velX = (int) ((diffX / distance) * speed);
        int velY = (int) ((diffY / distance) * speed);

        // buat peluru baru
        Bullet b = new Bullet(startX, startY, 24, 24, enemyBulletImage, true);
        b.setVelX(velX);
        b.setVelY(velY);
        bullets.add(b);
    }

    // Metode buat nembak peluru player ke arah target (mouse click)
    public void shootPlayerBullet(int targetX, int targetY) {
        if (player.getAmmo() <= 0) return; // ga ada peluru, gabisa nembak

        // tentukan titik spawn peluru (tengah player)
        int startX = player.getPosX() + player.getWidth() / 2;
        int startY = player.getPosY() + player.getHeight() / 2;
        // hitung selisih posisi antara Mouse (Target) dan Player (Asal)
        double diffX = targetX - startX;
        double diffY = targetY - startY;
        double distance = Math.sqrt((diffX * diffX) + (diffY * diffY));

        int speed = 8;
        int velX = (int) ((diffX / distance) * speed);
        int velY = (int) ((diffY / distance) * speed);

        // buat peluru baru
        Bullet b = new Bullet(startX, startY, 24, 24, playerBulletImage, false);
        b.setVelX(velX);
        b.setVelY(velY);
        // update list peluru dan kurangi ammo player
        bullets.add(b);
        player.setAmmo(player.getAmmo() - 1); 
    }

    // Metode buat ngecek tabrakan antar objek game
    private void checkCollisions() {
        Rectangle playerRect = player.getBounds();

        // CEK PELURU
        int i = 0;
        while (i < bullets.size()) {
            Bullet b = bullets.get(i);
            Rectangle bulletRect = b.getBounds();
            boolean bulletRemoved = false; // Flag penanda peluru dihapus

            // CEK TABRAKAN DENGAN OBSTACLE
            int j = 0;
            // Loop obstacle, berhenti kalo obstacle abis ATAU peluru udah meledak
            while (j < obstacles.size() && !bulletRemoved) { 
                Obstacle o = obstacles.get(j);
                if (bulletRect.intersects(o.getBounds())) {
                    if (b.isEnemy()) {
                        // Peluru musuh kena box -> jadi miss dan ammo player nambah
                        handleMissedBullet(b, i);
                    } else {
                        // Peluru player kena box -> Hapus peluru
                        bullets.remove(i);
                    }
                    bulletRemoved = true;
                }
                j++;
            }
            if (bulletRemoved) {
                continue; 
            }

            // CEK TABRAKAN DENGAN ENTITY (PLAYER / MUSUH)
            if (b.isEnemy()) {
                if (bulletRect.intersects(playerRect)) { // jika player kena peluru musuh
                    // Bunyiin SFX Mati (Index 3)
                    sfxModel.setFile(3);
                    sfxModel.play();
                    
                    triggerGameOver(); //game over
                    return; // Keluar method biar gak error
                }
            } else { // jika musuh ketembak
                int k = 0;
                while (k < enemies.size() && !bulletRemoved) {
                    Enemy en = enemies.get(k);
                    if (bulletRect.intersects(en.getBounds())) {
                        // Bunyiin SFX Hit (Index 3)
                        sfxModel.setFile(3);
                        sfxModel.play();

                        en.setPosY(-999); // Lempar musuh jauh-jauh
                        bullets.remove(i); // Hapus peluru
                        
                        // Tambah skor player
                        score += 10;
                        player.setScore(score);
                        
                        bulletRemoved = true; // hapus peluru
                    }
                    k++;
                }
            }
            if (!bulletRemoved) {
                i++;
            }
        }
        
        // CEK TABRAKAN BADAN (Player vs Musuh)
        int m = 0;
        while (m < enemies.size()) {
            Enemy en = enemies.get(m);
            if (en.getBounds().intersects(playerRect)) { // kalo tabrakan mati
                // Bunyiin SFX Mati (Index 3)
                sfxModel.setFile(3);
                sfxModel.play();

                triggerGameOver(); //game over
                return;
            }
            m++;
        }
    }

    // metode buat handle peluru musuh yang meleset
    private void handleMissedBullet(Bullet b, int indexToRemove) {
        missedBulletsCounter++; // tambah counter peluru meleset
        player.setMissedBullets(missedBulletsCounter); // update di player juga
        player.setAmmo(player.getAmmo() + 1); // kasih 1 peluru ke player
        
        if (indexToRemove >= 0 && indexToRemove < bullets.size()) {
            bullets.remove(indexToRemove);
        }
    }

    // metode buat bersihin objek yang udah keluar layar
    private void cleanupObjects() {
        bullets.removeIf(b -> b.getPosY() < -100 || b.getPosY() > frameHeight + 100 || b.getPosX() < -50 || b.getPosX() > frameWidth + 50);
        enemies.removeIf(e -> e.getPosY() < -100);
    }

    // Metode buat nge-trigger game over
    private void triggerGameOver() {
        gameOver = true;
        saveDataToDB();
    }

    // Metode buat nyimpen data player ke database
    public void saveDataToDB() {
        java.sql.ResultSet rs = null;
        try {
            String check = "SELECT * FROM tbenefit WHERE username = '" + username + "'"; // Cek ada atau nggak
            rs = db.executeQuery(check);
            
            if (rs.next()) { // Kalo ADA, update data lama
                String sql = "UPDATE tbenefit SET skor = " + score + 
                             ", missed_bullets = " + missedBulletsCounter + 
                             ", ammo = " + player.getAmmo() + 
                             " WHERE username = '" + username + "'";
                db.insertUpdateDeleteQuery(sql);
            } else { // Kalo GA ADA, insert data baru
                String sql = "INSERT INTO tbenefit VALUES ('" + username + "', " + 
                             score + ", " + missedBulletsCounter + ", " + player.getAmmo() + ")";
                db.insertUpdateDeleteQuery(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // tutup ResultSet
            try {
                if (rs != null) rs.getStatement().close();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    public void setGameStarted(boolean started) { // set status game started
        this.gameStarted = started;
    }

    public void togglePause() { // toggle status pause
        this.isPaused = !this.isPaused;
    }

    // GETTER & SETTER buat akses data dari Presenter & View
    // Buat ambil skor & username
    public int getScore() { return score; }
    public String getUsername() { return username; }

    // Buat ambil status game
    public boolean isGameOver() { return gameOver; }
    public boolean isPaused() { return isPaused; }
    
    // Buat ambil aset gambar
    public Image getBgImage() { return bgImage; }
    public Image getUiBoard() { return uiBoard; }
    public Image getUiButton() { return uiButton; }
    public Font getPixelFont() { return pixelFont; }

    // Buat ambil objek game
    public Player getPlayer() { return player; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public ArrayList<Bullet> getBullets() { return bullets; }
    public ArrayList<Obstacle> getObstacles() { return obstacles; }
    
    // Buat tombol
    public int getPressedButtonIndex() { return pressedButtonIndex; }
    public void setPressedButtonIndex(int index) { this.pressedButtonIndex = index; }
}