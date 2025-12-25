package view;

import model.Model;
import model.Sound;
import presenter.*;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GamePanel gamePanel;
    private Model model;
    private MainMenuPresenter menuPresenter;
    private Sound bgm = new Sound();

    public MainFrame() {
        this.setTitle("Miaw Miaw Boom");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1024, 768);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Halaman 1: Leaderboard
        MainMenu MainMenu = new MainMenu();
        menuPresenter = new MainMenuPresenter(MainMenu, this);
        mainPanel.add(MainMenu, "Leaderboard");

        this.add(mainPanel);
        this.setVisible(true);
        playMusic(0);
    }

    public void switchToGame(String username) {
        // 1. Bikin Model
        model = new Model(username);
        
        // 2. Bikin View (SEKARANG KOSONGAN)
        gamePanel = new GamePanel(); // [FIX] Hapus parameter model
        
        // 3. Bikin Presenter
        GamePresenter presenter = new GamePresenter(model, gamePanel, this);
        
        // 4. Setup Listener
        gamePanel.addKeyListener(presenter);
        gamePanel.addMouseListener(presenter);
        gamePanel.setFocusable(true);

        playMusic(1);

        mainPanel.add(gamePanel, "Game");
        cardLayout.show(mainPanel, "Game");
        gamePanel.requestFocusInWindow();
        
        presenter.startGame();
    }
    
    public void showLeaderboard() {
        if (menuPresenter != null) {
            menuPresenter.loadData();
        }
        playMusic(0);
        cardLayout.show(mainPanel, "Leaderboard");
    }

    public void playMusic(int i) {
        bgm.stop();
        bgm.setFile(i);
        bgm.play();
        bgm.loop();
    }

    public void stopMusic() {
        bgm.stop();
    }
}