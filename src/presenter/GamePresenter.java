package presenter;

import model.*;
import view.GamePanel;
import view.MainFrame;
import java.awt.event.*;
import javax.swing.Timer;

// kelas GamePresenter adalah presenter untuk menghubungkan Model dan GamePanel (View)
// kelas ini bertanggung jawab untuk mengelola logika game, input pengguna, dan pembaruan tampilan

public class GamePresenter implements ActionListener, KeyListener, MouseListener 
{
    // Atribut buat nyimpen referensi ke Model, View, dan MainFrame
    private Model model;
    private GamePanel view;
    private MainFrame mainFrame;
    private Timer gameLoop;
    private Timer enemySpawner;
    private Sound sfx = new Sound();

    // metode konstruktor buat inisialisasi GamePresenter dengan Model, View, dan MainFrame
    public GamePresenter(Model model, GamePanel view, MainFrame mainFrame) {
        this.model = model;
        this.view = view;
        this.mainFrame = mainFrame;

        // Setup Timer Game Loop
        gameLoop = new Timer(1000 / 60, this); 
        
        // Setup Timer Spawn Musuh
        enemySpawner = new Timer(2000, e -> {
            if (!model.isGameOver() && !model.isPaused()) {
                model.spawnEnemy(); 
            }
        });
    }

    // metode buat memulai permainan
    public void startGame() {
        model.setGameStarted(true);
        gameLoop.start();
        enemySpawner.start();
        view.requestFocus(); // Minta fokus buat keyboard
    }

    // metode buat menghentikan permainan
    public void stopGame() {
        if (gameLoop != null) gameLoop.stop();
        if (enemySpawner != null) enemySpawner.stop();
    }

    // metode buat GAME LOOP (Jantungnya Game)
    @Override
    public void actionPerformed(ActionEvent e) {
        if (model.isGameOver()) { // KALO GAME OVER
            mainFrame.stopMusic();
            // KIRIM DATA TERAKHIR SEBELUM STOP
            updateView(); 
            view.repaint();
            stopGame();
            return;
        }

        if (model.isPaused()) { // KALO PAUSE
            // KIRIM DATA BIAR TAMPILAN GAK ILANG PAS PAUSE
            updateView();
            view.repaint();
            return;
        }

        model.updateGameLogic(); // update logika game di Model
        updateView(); // kirim data terbaru ke View
        view.repaint(); // minta View repaint ulang
    }

    // metode buat ngirim data dari Model ke View
    private void updateView() {
        // 1. Kirim Aset
        view.setGameAssets(
            model.getBgImage(), model.getUiBoard(), model.getUiButton(), model.getPixelFont()
        );
        
        // 2. Kirim Objek Game
        view.setGameObjects(
            model.getPlayer(), model.getEnemies(), model.getBullets(), model.getObstacles()
        );
        
        // 3. Kirim Status & Statistik
        // Hitung kills disini
        int kills = model.getScore() / 10; 
        
        view.setGameStats(
            model.getUsername(), 
            model.getScore(), 
            model.getPlayer().getAmmo(), 
            model.getPlayer().getMissedBullets(),
            kills,
            model.isGameOver(),
            model.isPaused()
        );
        
        // 4. Kirim Index Tombol
        view.setPressedButtonIndex(model.getPressedButtonIndex());
    }

    // INPUT LISTENER (Keyboard & Mouse)
    @Override
    public void keyPressed(KeyEvent e) { // Tangkap Tombol yang Ditekan
        int key = e.getKeyCode();

        // Pastikan game sudah mulai & gak pause
        if (model.isGameOver() || model.isPaused()) return;

        // Oper perintah ke Model
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) model.getPlayer().setVelY(-5); // Pindah ke atas
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) model.getPlayer().setVelY(5); // Pindah ke bawah
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) model.getPlayer().setVelX(-5); // Pindah ke kiri
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) model.getPlayer().setVelX(5); // Pindah ke kanan
        
        if (key == KeyEvent.VK_SPACE) model.togglePause(); // Toggle Pause
    }

    @Override
    public void keyReleased(KeyEvent e) { // Tangkap Tombol yang Dilepas
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_S || key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) model.getPlayer().setVelY(0); // Stop gerak vertikal
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_D || key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) model.getPlayer().setVelX(0); // Stop gerak horizontal
    }

    @Override
    public void mousePressed(MouseEvent e) { // Tangkap Klik Mouse
        int mx = e.getX();
        int my = e.getY();

        // 1. LOGIC KLIK PAS GAME OVER
        if (model.isGameOver()) {
            // Tombol Back to Menu
            int centerX = view.getWidth() / 2;
            int centerY = view.getHeight() / 2;
            int boardH = 400;
            int boardY = centerY - (boardH / 2);

            int btnW = 220; // Lebar tombol Game Over
            int btnH = 60;
            int btnY = boardY + 260; // Posisi Y tombol Back to Menu

            // Cek Kena Tombol Back to Menu
            if (mx >= centerX - (btnW / 2) && mx <= centerX + (btnW / 2) && my >= btnY && my <= btnY + btnH) {
                model.setPressedButtonIndex(2); // Index 2 = Quit/Back
                view.repaint();
            }
            return;
        }

        // 2. LOGIC KLIK PAS PAUSE
        if (model.isPaused()) {
            int centerX = view.getWidth() / 2;
            int centerY = view.getHeight() / 2;
            int boardH = 350; // Tinggi board Pause (Standar)
            int boardY = centerY - (boardH / 2);
            int btnW = 200;
            int btnH = 60;

            // Tombol 1: RESUME
            int btn1Y = boardY + 140;
            if (mx >= centerX - (btnW / 2) && mx <= centerX + (btnW / 2) && my >= btn1Y && my <= btn1Y + btnH) {
                model.setPressedButtonIndex(1); // Index 1 = Resume
                view.repaint();
                return;
            }

            // Tombol 2: BACK TO MENU
            int btn2Y = boardY + 220;
            if (mx >= centerX - (btnW / 2) && mx <= centerX + (btnW / 2) && my >= btn2Y && my <= btn2Y + btnH) {
                model.setPressedButtonIndex(2); // Index 2 = Back
                view.repaint();
                return;
            }
            return;
        }

        // 3. Logic Nembak (Cuma jalan kalo gak lagi Menu Game Over / Pause)
        if (e.getButton() == MouseEvent.BUTTON1) {
            // cek ammo dulu sebelum nembak
            if (model.getPlayer().getAmmo() > 0) {
                model.shootPlayerBullet(mx, my); // player nembak

                // Mainkan Sound Effect Nembak
                sfx.setFile(2);
                sfx.play();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) { // Tangkap Lepas Klik Mouse
        if (model.getPressedButtonIndex() != 0) {
            
            // Tombol Resume (Khusus Pause Menu)
            if (model.getPressedButtonIndex() == 1 && model.isPaused()) {
                model.togglePause();
            } 
            
            // Tombol BACK TO MENU (Buat Game Over & Pause)
            else if (model.getPressedButtonIndex() == 2) {
                stopGame(); 
                model.saveDataToDB(); // SAVE OTOMATIS
                mainFrame.showLeaderboard(); 
            }

            model.setPressedButtonIndex(0);
            view.repaint();
        }
    }

    // Method interface lain
    public void keyTyped(KeyEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}