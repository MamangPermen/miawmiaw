package presenter;

import model.*;
import view.IGamePanel;
import view.MainFrame;
import java.awt.event.*;
import javax.swing.Timer;

// Kelas GamePresenter adalah pengendali utama alur permainan (Game Loop).
// Kelas ini bertanggung jawab menangani input user (Keyboard/Mouse), memperbarui
// logika Model secara berkala (Tick), dan menyinkronkan data Model ke View (Render).

public class GamePresenter implements IGamePresenter, ActionListener, KeyListener, MouseListener 
{
    // Atribut buat nyimpen referensi ke Model, View, dan MainFrame
    private Model model;
    private IGamePanel view;
    private MainFrame mainFrame;
    private Timer gameLoop;
    private Timer enemySpawner;
    private Sound sfx = new Sound();

    // metode konstruktor buat inisialisasi GamePresenter dengan Model, View, dan MainFrame
    public GamePresenter(Model model, IGamePanel view, MainFrame mainFrame) {
        this.model = model;
        this.view = view;
        this.mainFrame = mainFrame;

        // Setup Timer Game Loop
        gameLoop = new Timer(1000 / 60, this); 
        
        // Setup Timer Spawn Musuh
        enemySpawner = new Timer(2000, e -> { // musuh spawn tiap 2 detik
            if (!model.isGameOver() && !model.isPaused()) {
                model.spawnEnemy(); 
            }
        });

        // Pasang Listener Input
        this.view.addKeyListener(this);
        this.view.addMouseListener(this);
    }

    // metode buat memulai permainan
    @Override
    public void startGame() {
        model.setGameStarted(true);
        gameLoop.start();
        enemySpawner.start();
        view.requestFocus(); // Minta fokus buat keyboard
    }

    // metode buat menghentikan permainan
    @Override
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
        // Kirim Aset
        view.setGameAssets(
            model.getBgImage(), model.getUiBoard(), model.getUiButton(), model.getPixelFont()
        );
        
        // Kirim Objek Game
        view.setGameObjects(
            model.getPlayer(), model.getEnemies(), model.getBullets(), model.getObstacles()
        );

        // Kirim Status & Statistik
        // Hitung kills
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

        // Kirim Index Tombol
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
    public void mousePressed(MouseEvent e) { 
        int mx = e.getX();
        int my = e.getY();

        // CEK MENU (Game Over / Pause)
        if (model.isGameOver() || model.isPaused()) {
            int btnIndex = view.getMenuButtonAt(mx, my);
            
            if (btnIndex != 0) {
                model.setPressedButtonIndex(btnIndex);
                view.repaint();
            }
            return; // Stop disini kalau lagi menu
        }

        // LOGIC GAMEPLAY (Nembak)
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (model.getPlayer().getAmmo() > 0) {
                model.shootPlayerBullet(mx, my); 
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