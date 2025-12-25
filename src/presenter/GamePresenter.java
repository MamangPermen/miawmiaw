package presenter;

import model.Model;
import view.GamePanel;
import view.MainFrame;
import java.awt.event.*;
import javax.swing.Timer;

public class GamePresenter implements ActionListener, KeyListener, MouseListener {
    private Model model;
    private GamePanel view;
    private MainFrame mainFrame;
    private Timer gameLoop;
    private Timer enemySpawner;

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

    public void startGame() {
        model.setGameStarted(true);
        gameLoop.start();
        enemySpawner.start();
        view.requestFocus(); // Minta fokus buat keyboard
    }

    public void stopGame() {
        if (gameLoop != null) gameLoop.stop();
        if (enemySpawner != null) enemySpawner.stop();
    }

    // --- GAME LOOP (Jantungnya Game) ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (model.isGameOver() || model.isPaused()) {
            view.repaint(); // Tetap repaint buat nampilin teks Pause/Game Over
            return;
        }

        // 1. SURUH MODEL MIKIR (Update Posisi)
        model.updateGameLogic(); 

        // 2. SURUH VIEW GAMBAR ULANG
        view.repaint();
    }

    // --- INPUT LISTENER (Keyboard & Mouse) ---
    // Presenter yang dengerin, terus nyuruh Model gerak
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Pastikan game sudah mulai & gak pause
        if (model.isGameOver() || model.isPaused()) return;

        // Oper perintah ke Model
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) model.getPlayer().setVelY(-5);
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) model.getPlayer().setVelY(5);
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) model.getPlayer().setVelX(-5);
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) model.getPlayer().setVelX(5);
        
        if (key == KeyEvent.VK_SPACE) model.togglePause();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_S || key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) model.getPlayer().setVelY(0);
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_D || key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) model.getPlayer().setVelX(0);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        // 1. LOGIC KLIK PAS GAME OVER (Menu Khusus: Cuma 1 Tombol)
        if (model.isGameOver()) {
            int centerX = view.getWidth() / 2;
            int centerY = view.getHeight() / 2;
            int boardH = 400; // Tinggi board Game Over (sesuai GamePanel)
            int boardY = centerY - (boardH / 2);

            int btnW = 220; // Lebar tombol Game Over
            int btnH = 60;
            int btnY = boardY + 260; // Posisi Y tombol Back to Menu

            // Cek Kena Tombol Back to Menu?
            if (mx >= centerX - (btnW / 2) && mx <= centerX + (btnW / 2) && my >= btnY && my <= btnY + btnH) {
                model.setPressedButtonIndex(2); // Index 2 = Quit/Back
                view.repaint();
            }
            return;
        }

        // 2. LOGIC KLIK PAS PAUSE (Menu Standar: 2 Tombol)
        // [INI YANG KETINGGALAN TADI]
        if (model.isPaused()) {
            int centerX = view.getWidth() / 2;
            int centerY = view.getHeight() / 2;
            int boardH = 350; // Tinggi board Pause (Standar)
            int boardY = centerY - (boardH / 2);
            int btnW = 200;
            int btnH = 60;

            // Tombol 1: RESUME (Koordinat Y = boardY + 140)
            int btn1Y = boardY + 140;
            if (mx >= centerX - (btnW / 2) && mx <= centerX + (btnW / 2) && my >= btn1Y && my <= btn1Y + btnH) {
                model.setPressedButtonIndex(1); // Index 1 = Resume
                view.repaint();
                return;
            }

            // Tombol 2: BACK TO MENU (Koordinat Y = boardY + 220)
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
            model.shootPlayerBullet(mx, my);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (model.getPressedButtonIndex() != 0) {
            
            // Tombol Resume (Khusus Pause Menu)
            if (model.getPressedButtonIndex() == 1 && model.isPaused()) {
                model.togglePause();
            } 
            
            // TOMBOL BACK TO MENU (Buat Game Over & Pause)
            else if (model.getPressedButtonIndex() == 2) {
                stopGame(); 
                model.saveDataToDB(); // SAVE OTOMATIS
                mainFrame.showLeaderboard(); 
            }

            model.setPressedButtonIndex(0);
            view.repaint();
        }
    }
    
    // Method Restart Game (Reset semua variabel)
    private void restartGame() {
        model.setScore(0);
        model.setAmmo(0);
        model.setMissedBullets(0);
        model.setGameOver(false);
        model.setGameStarted(true);
        model.getEnemies().clear();
        model.getBullets().clear();
        model.getObstacles().clear();
        model.generateObstacles(); // Generate ulang batu
        
        // Reset Posisi Player
        model.getPlayer().setPosX(1024 / 2 - 32);
        model.getPlayer().setPosY(768 / 2 - 32);
    }

    // Method interface lain (kosongin aja)
    public void keyTyped(KeyEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}