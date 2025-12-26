package presenter;

import model.Database;
import view.IMainMenu;
import view.MainFrame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.swing.JOptionPane;

// Kelas MainMenuPresenter adalah penghubung logika (Presenter) untuk Menu Utama.
// Kelas ini bertanggung jawab mengambil data leaderboard dari Model, menangani logika
// scroll & klik tombol, serta memerintahkan View untuk update tampilan.

public class MainMenuPresenter implements IMainMenuPresenter
{
    // Atribut buat nyimpen referensi ke View dan MainFrame
    private IMainMenu view;
    private MainFrame mainFrame; 
    private Database db;

    // metode konstruktor buat inisialisasi MainMenuPresenter dengan View dan MainFrame
    public MainMenuPresenter(IMainMenu view, MainFrame mainFrame) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.db = new Database(); // Init DB

        loadData(); // Load data awal
        initController(); // Pasang Listener Mouse
    }

    // LOGIC INPUT
    private void initController() {
        MouseAdapter handler = new MouseAdapter() {
            
            // SCROLL WHEEL
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                updateScroll(e.getWheelRotation());
            }

            // MOUSE PRESSED (Cek Tombol & Scroll)
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                // Cek Scroll Bar
                if (view.isScrollBarHit(mx, my)) {
                    view.setIsDraggingScroll(true);
                    return;
                }
                // Cek Tombol Play / Quit
                if (view.isPlayBtnHit(mx, my)) {
                    view.setPressedButtonType(1);
                } else if (view.isQuitBtnHit(mx, my)) {
                    view.setPressedButtonType(2);
                }

                checkTableClick(mx, my); // Cek klik di tabel leaderboard
            }

            // MOUSE DRAGGED (Scroll Tabel)
            @Override
            public void mouseDragged(MouseEvent e) {
                Object[][] data = view.getLeaderboardData();
                if (data == null) return;
                
                // Ambil info track scroll dari View
                int trackY = view.getScrollTrackY();
                int trackH = view.getScrollTrackHeight();
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
                     int totalItems = data.length - view.getMaxVisibleRows();
                     int newOffset = (int) (percentage * totalItems);
                     
                     // Update kalo ada perubahan
                     if (newOffset != currentOffset) {
                         // reuse updateScroll tapi dengan selisihnya
                         updateScroll(newOffset - currentOffset); 
                     }
                }
            }

            // MOUSE RELEASED (Eksekusi Aksi)
            @Override
            public void mouseReleased(MouseEvent e) {
                view.setIsDraggingScroll(false); // Stop drag visuals
                view.setPressedButtonType(0);    // Reset tombol visuals

                int mx = e.getX();
                int my = e.getY();

                // Cek Klik Tombol Play
                if (view.isPlayBtnHit(mx, my)) {
                    String name = view.getUsernameField().getText().trim();
                    if (!name.isEmpty()) {
                        mainFrame.switchToGame(name);
                    } else { // kalo kosong, tampilkan peringatan suruh isi username
                        JOptionPane.showMessageDialog((java.awt.Component)view, "Isi username dulu atuh!");
                    }
                }
                
                // Cek Klik Tombol Quit
                else if (view.isQuitBtnHit(mx, my)) {
                    shutDown(); // Panggil method shutdown buat nutup koneksi DB
                    System.exit(0);
                }
            }
        };

        // PASANG LISTENER KE VIEW
        view.addMouseListenerToPanel(handler);
    }

    // HELPER METHODS
    // metode buat ngupdate scroll berdasarkan input scroll wheel atau drag
    private void updateScroll(int amount) {
        Object[][] data = view.getLeaderboardData();
        if (data == null) return;
        
        int current = view.getScrollOffset();
        int next = current + amount;
        
        // Clamp logic (Biar ga minus atau kelebihan)
        int maxOffset = Math.max(0, data.length - view.getMaxVisibleRows());
        if (next < 0) next = 0;
        if (next > maxOffset) next = maxOffset;
        
        view.setScrollOffset(next); // Update View
    }

    // metode buat ngecek klik di tabel leaderboard
    private void checkTableClick(int mx, int my) {
        // tanya View index baris yang diklik
        int index = view.getRowIndexAt(mx, my);
        
        if (index != -1) {
            Object[][] data = view.getLeaderboardData();
            if (data != null) {
                view.setSelectedRowIndex(index);
                view.getUsernameField().setText(data[index][0].toString());
            }
        }
    }

    // metode buat load data leaderboard dari database
    public void loadData() {
        java.sql.ResultSet rs = null;
        try {
            // Query Data
            String sql = "SELECT username, skor, missed_bullets, ammo FROM tbenefit ORDER BY skor DESC";
            rs = db.executeQuery(sql);
            
            // Masukin ke List
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
            
            // Convert ke Array 2D
            Object[][] dataArray = new Object[dataList.size()][4];
            for (int i = 0; i < dataList.size(); i++) {
                dataArray[i] = dataList.get(i);
            }
            
            // Kirim ke View buat Digambar
            view.setLeaderboardData(dataArray);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gagal load leaderboard");
        } finally {
            // tutup ResultSet
            try {
                if (rs != null) rs.getStatement().close();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    // metode buat nutup koneksi database
    @Override
    public void shutDown() {
        db.closeConnection();
    }
}