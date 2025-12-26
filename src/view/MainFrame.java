package view;

import presenter.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// import dari model buat tipe data
import model.Model;
import model.Sound;

// Kelas MainFrame adalah jendela utama aplikasi
// Kelas ini berfungsi sebagai wadah utama untuk menampung panel-panel lain seperti MainMenu dan GamePanel
// Kelas ini juga mengelola pergantian antar panel dan pemutaran musik latar

public class MainFrame extends JFrame 
{
    private CardLayout cardLayout; // layout buat ganti-ganti panel
    private JPanel mainPanel; // panel utama yang pake cardlayout
    private GamePanel gamePanel; // panel game, disimpen biar gampang diakses
    private Model model; // model game
    private IMainMenuPresenter menuPresenter; // presenter buat main menu
    private IGamePresenter gamePresenter; // presenter buat game
    private Sound bgm = new Sound(); // background music

    // Konstruktor
    public MainFrame() {
        this.setTitle("Miaw Miaw Boom"); // Judul window
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle close operation manually biar db ketutup
        this.setSize(1024, 768); // Ukuran window (resolusi 1024x768)
        this.setResizable(false); // Gak bisa di-resize
        this.setLocationRelativeTo(null); // Biar muncul di tengah layar

        cardLayout = new CardLayout(); // Inisialisasi CardLayout
        mainPanel = new JPanel(cardLayout); // Inisialisasi mainPanel dengan CardLayout

        // Window Listener buat handle event tutup window
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Logic Tutup Aplikasi
                if (gamePresenter != null) {
                    gamePresenter.stopGame();
                }
                if (menuPresenter != null) {
                    menuPresenter.shutDown(); // Tutup DB Menu
                }
                System.exit(0);
            }
        });

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
        // Hapus Panel Game Lama kalo ada
        if (gamePanel != null) {
            mainPanel.remove(gamePanel);
            gamePanel = null; // Bantu GC bersihin memori
        }
        
        // Bikin Model & Panel Baru
        model = new Model(username);
        gamePanel = new GamePanel(); 
        
        // Bikin Presenter Baru
        gamePresenter = new GamePresenter(model, gamePanel, this);
        
        gamePanel.setFocusable(true);

        playMusic(1); // in-game music

        // ADD PANEL BARU
        mainPanel.add(gamePanel, "Game");
        mainPanel.revalidate();
        mainPanel.repaint();
        
        // TAMPILIN PANEL GAME
        cardLayout.show(mainPanel, "Game");
        gamePanel.requestFocusInWindow();
        
        gamePresenter.startGame(); // Mulai game loop
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