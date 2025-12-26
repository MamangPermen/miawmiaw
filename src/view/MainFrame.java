package view;

import presenter.*;
import javax.swing.*;
import java.awt.*;

// import dari model buat tipe data
import model.Model;
import model.Sound;

// Kelas MainFrame adalah jendela utama aplikasi
// Kelas ini berfungsi sebagai wadah utama untuk menampung panel-panel lain seperti MainMenu dan GamePanel

public class MainFrame extends JFrame 
{
    private CardLayout cardLayout; // layout buat ganti-ganti panel
    private JPanel mainPanel; // panel utama yang pake cardlayout
    private GamePanel gamePanel; // panel game, disimpen biar gampang diakses
    private Model model; // model game
    private MainMenuPresenter menuPresenter; // presenter buat main menu
    private Sound bgm = new Sound(); // background music

    // Konstruktor
    public MainFrame() {
        this.setTitle("Miaw Miaw Boom"); // Judul window
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Tutup program kalo jendela ditutup
        this.setSize(1024, 768); // Ukuran window (resolusi 1024x768)
        this.setResizable(false); // Gak bisa di-resize
        this.setLocationRelativeTo(null); // Biar muncul di tengah layar

        cardLayout = new CardLayout(); // Inisialisasi CardLayout
        mainPanel = new JPanel(cardLayout); // Inisialisasi mainPanel dengan CardLayout

        // Halaman 1: Leaderboard
        MainMenu MainMenu = new MainMenu();
        menuPresenter = new MainMenuPresenter(MainMenu, this);
        mainPanel.add(MainMenu, "Leaderboard"); // Tambahin MainMenu ke mainPanel

        // Tampilkan mainPanel di JFrame
        this.add(mainPanel);
        this.setVisible(true);
        playMusic(0); // main menu music
    }

    // metode buat ganti ke game panel
    public void switchToGame(String username) {
        // 1. Hapus Panel Game Lama kalo ada
        if (gamePanel != null) {
            mainPanel.remove(gamePanel);
            gamePanel = null; // Bantu GC bersihin memori
        }
        
        // 2. Bikin Model Baru
        model = new Model(username);
        
        // 3. Bikin View Baru
        gamePanel = new GamePanel(); 
        
        // 4. Bikin Presenter Baru
        GamePresenter presenter = new GamePresenter(model, gamePanel, this);
        
        // 5. Setup Listener
        gamePanel.addKeyListener(presenter);
        gamePanel.addMouseListener(presenter);
        gamePanel.setFocusable(true);

        playMusic(1); // in-game music

        // 6. ADD PANEL BARU
        mainPanel.add(gamePanel, "Game"); // Add yang baru
        
        // Refresh Tampilan biar CardLayout sadar ada perubahan
        mainPanel.revalidate();
        mainPanel.repaint();
        
        // 7. TAMPILIN PANEL GAME
        cardLayout.show(mainPanel, "Game");
        gamePanel.requestFocusInWindow();
        
        presenter.startGame(); // Mulai game loop
    }
    
    // metode buat ganti ke leaderboard panel
    public void showLeaderboard() {
        if (menuPresenter != null) {
            menuPresenter.loadData();
        }
        playMusic(0); // main menu music
        cardLayout.show(mainPanel, "Leaderboard");
    }

    // metode buat mainin musik latar
    public void playMusic(int i) {
        bgm.stop();
        bgm.setFile(i);
        bgm.play();
        bgm.loop();
    }

    // metode buat stop musik latar
    public void stopMusic() {
        bgm.stop();
    }
}