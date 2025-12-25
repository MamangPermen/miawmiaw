package view;

import model.Model;
import presenter.GamePresenter;
import presenter.MainMenuPresenter;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GamePanel gamePanel;
    private Model model;
    private MainMenuPresenter menuPresenter;

    public MainFrame() {
        this.setTitle("Miaw Miaw Boom");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1024, 768);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        // Setup CardLayout buat ganti-ganti halaman
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Halaman 1: Leaderboard
        MainMenu MainMenu = new MainMenu();
        menuPresenter = new MainMenuPresenter(MainMenu, this);
        mainPanel.add(MainMenu, "Leaderboard");

        // Halaman 2: Game (Nanti di-init pas tombol play diklik)
        // Kita siapin wadahnya dulu
        
        this.add(mainPanel);
        this.setVisible(true);
    }

    // Method ini dipanggil dari MainMenu pas klik PLAY
    public void switchToGame(String username) {
        // 1. Bikin Model (Data)
        model = new Model(username);
        
        // 2. Bikin View (Tampilan)
        gamePanel = new GamePanel(model); // View masih butuh baca data Model buat ngegambar
        
        // 3. Bikin PRESENTER (Otak)
        // Presenter ngejodohin Model & View
        GamePresenter presenter = new GamePresenter(model, gamePanel, this);
        
        // 4. Daftarin Presenter sebagai pendengar Input di View
        gamePanel.addKeyListener(presenter);
        gamePanel.addMouseListener(presenter);
        gamePanel.setFocusable(true);

        // 5. Tampilin
        mainPanel.add(gamePanel, "Game");
        cardLayout.show(mainPanel, "Game");
        
        // 6. Jalankan via Presenter
        presenter.startGame();
    }
    
    public void showLeaderboard() {
        // 1. Reload Data Leaderboard via Presenter
        if (menuPresenter != null) {
            menuPresenter.loadData();
        }
        
        // 2. Ganti Tampilan
        cardLayout.show(mainPanel, "Leaderboard");
    }
}