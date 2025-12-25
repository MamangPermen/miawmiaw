package model;

import java.awt.*;
import java.util.*;
import javax.swing.ImageIcon;

// HAPUS implements ActionListener, KeyListener, dll. Model skrg murni data.
public class Model { 
    
    // --- KONFIGURASI LAYAR ---
    int frameWidth = 1024;
    int frameHeight = 768;

    // --- KOMPONEN ---
    private Database db; // View dihapus dari sini, Model gak boleh tau soal View

    // --- OBJEK GAME ---
    Player player;
    ArrayList<Enemy> enemies;
    ArrayList<Bullet> bullets;
    ArrayList<Obstacle> obstacles;

    // --- GAMBAR ---
    Image bgImage, playerImage, enemyImage, playerBulletImage, obsImage, enemyBulletImage;
    Image uiBoard, uiButton;

    // Font
    Font pixelFont;

    // --- GAME STATE ---
    // Timer dihapus dari sini, sekarang dipegang GamePresenter
    
    boolean gameOver = false;
    boolean gameStarted = false;
    boolean isPaused = false;
    
    // Statistik
    String username;
    int score = 0;
    int missedBulletsCounter = 0;

    // Lainnya
    int pressedButtonIndex = 0;

    // --- KONSTRUKTOR ---
    public Model(String username) {
        this.username = username;
        this.db = new Database(); 

        loadImages();

        // spawn player di tengah layar
        player = new Player(frameWidth / 2 - 32, frameHeight / 2 - 32, 64, 64, playerImage, username);
        
        // load data user dari database
        loadUserData();

        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        obstacles = new ArrayList<>();

        generateObstacles();
    }

    private void loadUserData() {
        try {
            // Cek apakah user ini udah pernah main sebelumnya?
            String sql = "SELECT * FROM tbenefit WHERE username = '" + username + "'";
            java.sql.ResultSet rs = db.executeQuery(sql);
            
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
                
                System.out.println("Data Loaded: Score=" + score + ", Ammo=" + savedAmmo);
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
            System.out.println("Gagal load user data");
        }
    }

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
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/assets/pixel_font.ttf")).deriveFont(24f);

            // register font biar bisa dipake di seluruh aplikasi
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(pixelFont);

        } catch (Exception e) {
            // Ini bakal nongol kalo lu salah ketik nama file
            System.out.println("Gagal load gambar: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    // --- LOGIC UTAMA (DIPANGGIL PRESENTER) ---
    // [FIX] Hapus parameter ActionEvent, ganti jadi public void biasa
    public void updateGameLogic() {
        // Cek status dipindah ke Presenter, tapi double check disini gapapa
        if (gameOver || !gameStarted || isPaused) {
            return; 
        }

        // --- 1. UPDATE PLAYER ---
        int pPrevX = player.getPosX();
        int pPrevY = player.getPosY();
        player.update();
        
        // Batas Layar Player
        if (player.getPosX() < 0) player.setPosX(0);
        if (player.getPosX() > frameWidth - player.getWidth()) player.setPosX(frameWidth - player.getWidth());
        if (player.getPosY() < 0) player.setPosY(0);
        if (player.getPosY() > frameHeight - player.getHeight()) player.setPosY(frameHeight - player.getHeight());

        // Player vs Batu
        Rectangle playerRect = player.getBounds();
        for (Obstacle o : obstacles) {
            if (playerRect.intersects(o.getBounds())) {
                player.setPosX(pPrevX);
                player.setPosY(pPrevY);
            }
        }
        
        // --- 2. UPDATE MUSUH ---
        for (Enemy en : enemies) {
            int ePrevX = en.getPosX();
            int ePrevY = en.getPosY(); 
            en.update(); 

            // A. LOGIKA TABRAKAN BATU (SLIDING)
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
            if (Math.random() < 0.006) { 
                shootEnemyBullet(en);
            }
        }

        // --- 3. UPDATE PELURU ---
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update(); 

            if (b.isEnemy()) {
                boolean kenaAtas = b.getPosY() <= 0;
                boolean kenaKiri = b.getPosX() <= 0;
                boolean kenaKanan = b.getPosX() >= frameWidth - b.getWidth();

                if (kenaAtas || kenaKiri || kenaKanan) {
                    handleMissedBullet(b, i); 
                    i--; 
                    continue; 
                }
            }
        }

        // --- 4. CEK TABRAKAN LAINNYA ---
        checkCollisions();
        cleanupObjects();
    }
    
    public void spawnEnemy() {
        Random rand = new Random();
        int randomX = rand.nextInt(frameWidth - 100);
        Enemy vampire = new Enemy(randomX, frameHeight, 64, 64, enemyImage);
        vampire.setVelY(-2); 
        enemies.add(vampire);
    }

    public void generateObstacles() {
        Random rand = new Random();
        obstacles.clear(); // Pastikan list bersih sebelum mulai
        
        // 1. Tentukan Jumlah (3 - 6)
        int count = rand.nextInt(5) + 3; 

        // Kita pake loop "while" dengan batas percobaan biar gak infinite loop
        int attempts = 0;
        while (obstacles.size() < count && attempts < 100) {
            attempts++;

            // 2. Tentukan Ukuran (PERSEGI: Width = Height)
            // Random antara 50px sampai 120px
            int size = rand.nextInt(71) + 80; 

            // 3. Tentukan Posisi Random
            int x = rand.nextInt(frameWidth - size - 100) + 50;
            int y = rand.nextInt(frameHeight - size - 100) + 50;
            
            // Bikin kotak bayangan buat ngecek tabrakan
            Rectangle newRect = new Rectangle(x, y, size, size);
            boolean aman = true;

            // --- CEK 1: JANGAN DEKETAN SAMA BATU LAIN ---
            for (Obstacle o : obstacles) {
                Rectangle existing = o.getBounds();
                
                // Kita "gelembungin" kotak batu lama sebesar 50px ke segala arah
                // Jadi kalo batu baru masuk area 50px ini, dianggap terlalu deket
                Rectangle safeZone = new Rectangle(
                    existing.x - 100, existing.y - 100, 
                    existing.width + 200, existing.height + 200
                );

                if (safeZone.intersects(newRect)) {
                    aman = false;
                    break;
                }
            }
            
            // --- CEK 2: JANGAN MUNCUL DI TENGAH (TEMPAT PLAYER) ---
            // Bikin area aman di tengah layar (200x200 pixel)
            Rectangle playerSpawnZone = new Rectangle(
                frameWidth / 2 - 100, frameHeight / 2 - 100, 200, 200
            );
            
            if (playerSpawnZone.intersects(newRect)) {
                aman = false;
            }

            // Kalo aman, baru masukin ke game
            if (aman) {
                obstacles.add(new Obstacle(x, y, size, size, obsImage));
            }
        }
    }

    private void shootEnemyBullet(Enemy en) {
        int startX = en.getPosX() + en.getWidth() / 2;
        int startY = en.getPosY(); 
        int targetX = player.getPosX() + player.getWidth() / 2;
        int targetY = player.getPosY() + player.getHeight() / 2;
        
        double diffX = targetX - startX;
        double diffY = targetY - startY;
        double distance = Math.sqrt((diffX * diffX) + (diffY * diffY));
        
        int speed = 4; 
        int velX = (int) ((diffX / distance) * speed);
        int velY = (int) ((diffY / distance) * speed);

        Bullet b = new Bullet(startX, startY, 24, 24, enemyBulletImage, true);
        b.setVelX(velX);
        b.setVelY(velY);
        bullets.add(b);
    }

    public void shootPlayerBullet(int targetX, int targetY) {
        if (player.getAmmo() <= 0) return; 

        int startX = player.getPosX() + player.getWidth() / 2;
        int startY = player.getPosY() + player.getHeight() / 2;

        double diffX = targetX - startX;
        double diffY = targetY - startY;
        double distance = Math.sqrt((diffX * diffX) + (diffY * diffY));

        int speed = 8;
        int velX = (int) ((diffX / distance) * speed);
        int velY = (int) ((diffY / distance) * speed);

        Bullet b = new Bullet(startX, startY, 24, 24, playerBulletImage, false);
        b.setVelX(velX);
        b.setVelY(velY);
        
        bullets.add(b);
        player.setAmmo(player.getAmmo() - 1); 
    }

    private void checkCollisions() {
        Rectangle playerRect = player.getBounds();

        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            Rectangle bulletRect = b.getBounds();

            for (Obstacle o : obstacles) {
                if (bulletRect.intersects(o.getBounds())) {
                    if (b.isEnemy()) {
                        handleMissedBullet(b, i);
                        i--; 
                    } else {
                        b.setPosY(-999); 
                    }
                    break; 
                }
            }
            
            if (i < 0 || i >= bullets.size() || bullets.get(i) != b) continue;

            if (b.isEnemy()) {
                if (bulletRect.intersects(playerRect)) triggerGameOver();
            } else {
                for (Enemy en : enemies) {
                    if (bulletRect.intersects(en.getBounds())) {
                        en.setPosY(-999); 
                        b.setPosY(-999); 
                        score += 10;
                        player.setScore(score);
                    }
                }
            }
        }
        
        for (Enemy en : enemies) {
            if (en.getBounds().intersects(playerRect)) triggerGameOver();
        }
    }

    private void handleMissedBullet(Bullet b, int indexToRemove) {
        missedBulletsCounter++;
        player.setMissedBullets(missedBulletsCounter);
        player.setAmmo(player.getAmmo() + 1);
        
        if (indexToRemove >= 0 && indexToRemove < bullets.size()) {
            bullets.remove(indexToRemove);
        }
    }

    private void cleanupObjects() {
        bullets.removeIf(b -> b.getPosY() < -100 || b.getPosY() > frameHeight + 100 || b.getPosX() < -50 || b.getPosX() > frameWidth + 50);
        enemies.removeIf(e -> e.getPosY() < -100);
    }

    private void triggerGameOver() {
        gameOver = true;
        saveDataToDB();
    }

    public void saveDataToDB() {
        try {
            String check = "SELECT * FROM tbenefit WHERE username = '" + username + "'";
            if (db.executeQuery(check).next()) {
                String sql = "UPDATE tbenefit SET skor = " + score + 
                             ", missed_bullets = " + missedBulletsCounter + 
                             ", ammo = " + player.getAmmo() + 
                             " WHERE username = '" + username + "'";
                db.insertUpdateDeleteQuery(sql);
            } else {
                String sql = "INSERT INTO tbenefit VALUES ('" + username + "', " + 
                             score + ", " + missedBulletsCounter + ", " + player.getAmmo() + ")";
                db.insertUpdateDeleteQuery(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- SETTER BUAT RESTART GAME (Dipanggil GamePresenter) ---
    
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void setScore(int score) {
        this.score = score;
        player.setScore(score); // Update data di object Player juga biar sinkron
    }

    public void setAmmo(int ammo) {
        // Ammo itu punyanya Player, jadi kita oper perintahnya ke Player
        player.setAmmo(ammo);
    }

    public void setMissedBullets(int missed) {
        this.missedBulletsCounter = missed;
        player.setMissedBullets(missed); // Sinkronisasi
    }

    // --- ACCESSOR METHODS (GETTER/SETTER KHUSUS) ---
    // [FIX] Ini method-method yang dicari GamePresenter
    
    public void setGameStarted(boolean started) {
        this.gameStarted = started;
    }

    public void togglePause() {
        this.isPaused = !this.isPaused;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPaused() {
        return isPaused;
    }

    // --- GETTER BUAT VIEW ---
    public Player getPlayer() { return player; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public ArrayList<Bullet> getBullets() { return bullets; }
    public ArrayList<Obstacle> getObstacles() { return obstacles; }
    public Image getBgImage() { return bgImage; }
    public Image getUiBoard() { return uiBoard; }
    public Image getUiButton() { return uiButton; }
    public Font getPixelFont() { return pixelFont; }
    public int getPressedButtonIndex() { return pressedButtonIndex; }
    public void setPressedButtonIndex(int index) { this.pressedButtonIndex = index; }
    public String getUsername() { return username; }
}