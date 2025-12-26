package view;

import java.awt.event.MouseAdapter;
import javax.swing.JTextField;

// IMainMenu adalah kontrak yang mendefinisikan metode-metode View Menu.
// Interface ini berfungsi untuk memisahkan implementasi visual (Swing) dari logika Presenter,
// memastikan Presenter hanya mengakses metode yang diizinkan (seperti update tabel atau hit-test).

public interface IMainMenu 
{
    // Update Tampilan
    void setLeaderboardData(Object[][] data);
    void setScrollOffset(int offset);
    void setSelectedRowIndex(int index);
    void setPressedButtonType(int type);
    void setIsDraggingScroll(boolean isDragging);
    void repaint();

    // Ambil Data dari Tampilan
    Object[][] getLeaderboardData();
    int getScrollOffset();
    JTextField getUsernameField();
    int getRowIndexAt(int mx, int my);

    // Koordinat & Hit Test API
    int getMaxVisibleRows();
    int getScrollTrackY();
    int getScrollTrackHeight();
    boolean isScrollBarHit(int mx, int my);
    boolean isPlayBtnHit(int mx, int my);
    boolean isQuitBtnHit(int mx, int my);
    
    // Wiring Listener
    void addMouseListenerToPanel(MouseAdapter listener);
}