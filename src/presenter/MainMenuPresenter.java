package presenter;

import model.Database;
import view.MainMenu;
import view.MainFrame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class MainMenuPresenter {
    private MainMenu view;
    private MainFrame mainFrame; 
    private Database db;

    public MainMenuPresenter(MainMenu view, MainFrame mainFrame) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.db = new Database(); // Init DB sekali aja di sini

        loadData(); // Load data awal
        initController(); // Pasang Listener Mouse
    }

    // --- LOGIC INPUT (PINDAHAN DARI VIEW) ---
    private void initController() {
        // Kita bikin MouseAdapter di sini, Logicnya MVP banget!
        MouseAdapter handler = new MouseAdapter() {
            
            // 1. SCROLL WHEEL
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                updateScroll(e.getWheelRotation());
            }

            // 2. MOUSE PRESSED (Cek Tombol & Scroll)
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                // Akses Rectangle langsung dari View (Variable Public)
                if (view.scrollBarRect != null && view.scrollBarRect.contains(mx, my)) {
                    view.setIsDraggingScroll(true); // Suruh View berubah warna
                    return;
                }
                
                if (view.playBtnRect != null && view.playBtnRect.contains(mx, my)) {
                    view.setPressedButtonType(1); // Suruh View "mendelep" tombol play
                } else if (view.quitBtnRect != null && view.quitBtnRect.contains(mx, my)) {
                    view.setPressedButtonType(2); // Suruh View "mendelep" tombol quit
                }

                checkTableClick(mx, my);
            }

            // 3. MOUSE DRAGGED (Matematika Scroll)
            @Override
            public void mouseDragged(MouseEvent e) {
                Object[][] data = view.getLeaderboardData();
                if (data == null) return;
                
                // Ambil info track scroll dari View
                int trackY = view.scrollTrackY;
                int trackH = view.scrollTrackHeight;
                int currentOffset = view.getScrollOffset();

                // Logic Drag
                if (trackH > 0) { 
                     int my = e.getY();
                     int relativeY = my - trackY;
                     
                     // Clamp (Batesin biar ga keluar jalur)
                     if (relativeY < 0) relativeY = 0;
                     if (relativeY > trackH) relativeY = trackH;
                     
                     // Hitung Persentase & Offset Baru
                     float percentage = (float) relativeY / trackH;
                     int totalItems = data.length - view.maxVisibleRows;
                     int newOffset = (int) (percentage * totalItems);
                     
                     // Update kalo ada perubahan
                     if (newOffset != currentOffset) {
                         // Kita reuse updateScroll tapi dengan selisihnya
                         updateScroll(newOffset - currentOffset); 
                     }
                }
            }

            // 4. MOUSE RELEASED (Eksekusi Aksi)
            @Override
            public void mouseReleased(MouseEvent e) {
                view.setIsDraggingScroll(false); // Stop drag visuals
                view.setPressedButtonType(0);    // Reset tombol visuals

                int mx = e.getX();
                int my = e.getY();

                // Cek Klik Tombol Play (Pake Rect dari View)
                if (view.playBtnRect != null && view.playBtnRect.contains(mx, my)) {
                    String name = view.getUsernameField().getText().trim();
                    if (!name.isEmpty()) {
                        mainFrame.switchToGame(name);
                    } else {
                        JOptionPane.showMessageDialog(view, "Isi username dulu atuh!");
                    }
                }
                
                // Cek Klik Tombol Quit
                else if (view.quitBtnRect != null && view.quitBtnRect.contains(mx, my)) {
                    System.exit(0);
                }
            }
        };

        // PASANG LISTENER KE VIEW
        view.addMouseListenerToPanel(handler);
    }

    // --- HELPER METHODS ---

    private void updateScroll(int amount) {
        Object[][] data = view.getLeaderboardData();
        if (data == null) return;
        
        int current = view.getScrollOffset();
        int next = current + amount;
        
        // Clamp logic (Biar ga minus atau kelebihan)
        int maxOffset = Math.max(0, data.length - view.maxVisibleRows);
        if (next < 0) next = 0;
        if (next > maxOffset) next = maxOffset;
        
        view.setScrollOffset(next); // Update View
    }

    private void checkTableClick(int mx, int my) {
        Object[][] data = view.getLeaderboardData();
        if (data == null) return;

        // Ambil koordinat tabel dari View variables
        int startY = view.boardY + 110;
        int rowY = startY + 40;
        int rowHeight = 35;
        
        int offset = view.getScrollOffset();
        int limit = Math.min(data.length, offset + view.maxVisibleRows);

        // Loop logika klik baris
        for (int i = offset; i < limit; i++) {
            if (my >= rowY - 25 && my <= rowY + 5) {
                if (mx >= view.boardX + 50 && mx <= view.boardX + view.boardW - 50) {
                    
                    // UPDATE VIEW: Highlight & Isi Textbox
                    view.setSelectedRowIndex(i);
                    view.getUsernameField().setText(data[i][0].toString());
                    return;
                }
            }
            rowY += rowHeight;
        }
    }

    // --- METHOD LOAD DATA (DATABASE) ---
    public void loadData() {
        try {
            // 1. Query Data
            String sql = "SELECT username, skor, missed_bullets, ammo FROM tbenefit ORDER BY skor DESC";
            ResultSet rs = db.executeQuery(sql);
            
            // 2. Masukin ke List
            ArrayList<Object[]> dataList = new ArrayList<>();
            while (rs.next()) {
                Object[] row = {
                    rs.getString("username"),
                    rs.getInt("skor"),
                    rs.getInt("missed_bullets"),
                    rs.getInt("ammo")
                };
                dataList.add(row);
            }
            
            // 3. Convert ke Array 2D
            Object[][] dataArray = new Object[dataList.size()][4];
            for (int i = 0; i < dataList.size(); i++) {
                dataArray[i] = dataList.get(i);
            }
            
            // 4. Kirim ke View buat Digambar
            view.setLeaderboardData(dataArray);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gagal load leaderboard");
        }
    }
}