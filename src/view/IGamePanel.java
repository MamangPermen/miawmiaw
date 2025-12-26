package view;

import java.awt.Image;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import model.Bullet;
import model.Enemy;
import model.Obstacle;
import model.Player;

// IGamePanel adalah kontrak yang mendefinisikan metode-metode View Game.
// Interface ini berfungsi agar Presenter dapat mengirimkan data aset dan objek game 
// ke View tanpa perlu mengetahui detail implementasi rendering-nya.

public interface IGamePanel 
{
    // Fungsi Dasar Swing
    void repaint();
    void requestFocus();
    int getWidth();
    int getHeight();

    // Setter Data (Update Tampilan)
    void setGameAssets(Image bg, Image board, Image btn, Font font);
    void setGameObjects(Player p, ArrayList<Enemy> e, ArrayList<Bullet> b, ArrayList<Obstacle> o);
    void setGameStats(String user, int sc, int am, int ms, int kl, boolean over, boolean pause);
    void setPressedButtonIndex(int index);

    // Hit Test API buat game over & pause menu
    int getMenuButtonAt(int mx, int my);

    // Wiring Listener
    void addKeyListener(KeyListener l);
    void addMouseListener(MouseListener l);
}