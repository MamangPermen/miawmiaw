package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter; 
import java.io.InputStream;

public class MainMenu extends JPanel 
{
    private JTextField usernameField; // Input field buat username
    
    // Aset
    private Image bgImage, boardImage, btnImage;
    private Font pixelFont;
    
    // DATA TAMPILAN (Diatur oleh Presenter)
    private Object[][] leaderboardData;
    private int scrollOffset = 0;   
    private int selectedRowIndex = -1; 
    private int pressedButtonType = 0; 
    private boolean isDraggingScroll = false;

    // KOORDINAT & RECTANGLE
    public Rectangle playBtnRect, quitBtnRect, scrollBarRect;
    public int boardX, boardY, boardW, boardH;
    public int scrollTrackY, scrollTrackHeight;
    public final int maxVisibleRows = 7; 

    // Konstruktor
    public MainMenu() {
        this.setLayout(null); 
        loadAssets(); // muat aset gambar & font
        setupComponents(); // setup komponen swing
    }

    // metode buat muat aset gambar & font
    private void loadAssets() {
        try {
            // Muat Gambar
            bgImage = new ImageIcon(getClass().getResource("/assets/Background.png")).getImage();
            boardImage = new ImageIcon(getClass().getResource("/assets/ui_board.png")).getImage();
            btnImage = new ImageIcon(getClass().getResource("/assets/ui_button.png")).getImage();
            
            // Muat Font Pixel
            try (InputStream is = getClass().getResourceAsStream("/assets/pixel_font.ttf")) {
                pixelFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(24f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(pixelFont);
            }
        } catch (Exception e) { // fallback kalo gagal
            pixelFont = new Font("SansSerif", Font.BOLD, 24);
            e.printStackTrace();
        }
    }

    // metode buat setup komponen swing
    private void setupComponents() {
        usernameField = new JTextField(15);
        // Set font pixel kalo berhasil dimuat
        if (pixelFont != null) {
            usernameField.setFont(pixelFont.deriveFont(20f));
        } else { // Fallback kalo gagal
            usernameField.setFont(new Font("SansSerif", Font.BOLD, 20));
        }
        // Set bounds, background, dan border
        usernameField.setBounds(0, 0, 0, 0); 
        usernameField.setBackground(new Color(255, 255, 255, 220)); 
        usernameField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(usernameField);
    }

    // helper method buat dapetin font aman (sans serif kalo pixelFont null)
    private Font getFontSafe(float size) {
        if (pixelFont != null) {
            return pixelFont.deriveFont(size);
        } else {
            return new Font("SansSerif", Font.BOLD, (int)size);
        }
    }

    // metode buat render tampilan
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background & Gelap
        if (bgImage != null) g.drawImage(bgImage, 0, 0, 1024, 768, null);
        g.setColor(new Color(0, 0, 0, 100)); 
        g.fillRect(0, 0, getWidth(), getHeight());

        // Judul
        g.setFont(getFontSafe(60f));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "STRIKE VAMPCATS", getWidth() / 2, 80);

        // Board
        boardW = 600;
        boardH = 450;
        boardX = (getWidth() - boardW) / 2;
        boardY = 120;
        if (boardImage != null) g.drawImage(boardImage, boardX, boardY, boardW, boardH, null);

        // Header Board
        g.setFont(getFontSafe(40f));
        drawCenteredString(g, "HALL OF FAME", getWidth() / 2, boardY + 35);
        // Sub-header Tabel
        g.setFont(getFontSafe(22f));
        g.setColor(Color.YELLOW);
        int startY = boardY + 110; // posisi Y awal tabel
        g.drawString("USERNAME", boardX + 60, startY);
        g.drawString("SCORE", boardX + 250, startY);
        g.drawString("MISSED", boardX + 350, startY);
        g.drawString("AMMO LEFT", boardX + 460, startY);

        g.setColor(Color.WHITE);
        g.drawLine(boardX + 50, startY + 10, boardX + boardW - 50, startY + 10);

        // ISI TABEL
        if (leaderboardData != null) {
            int rowY = startY + 40;
            // Gunakan scrollOffset dari Presenter
            int endIndex = Math.min(leaderboardData.length, scrollOffset + maxVisibleRows);

            for (int i = scrollOffset; i < endIndex; i++) { // Loop baris yang mau ditampilin
                Object[] row = leaderboardData[i];
                
                // Highlight Selection
                if (i == selectedRowIndex) {
                    g.setColor(new Color(255, 255, 255, 50)); 
                    g.fillRect(boardX + 50, rowY - 25, boardW - 100, 30);
                }

                // Teks Baris
                g.setColor(Color.WHITE);
                g.drawString(row[0].toString(), boardX + 60, rowY);
                g.drawString(row[1].toString(), boardX + 250, rowY);
                g.drawString(row[2].toString(), boardX + 370, rowY);
                g.drawString(row[3].toString(), boardX + 490, rowY);

                rowY += 35; 
            }
            
            // SCROLL BAR
            if (leaderboardData.length > maxVisibleRows) {
                int trackX = boardX + boardW - 25;
                scrollTrackY = boardY + 110;  
                scrollTrackHeight = 250;      
                
                // Track Background
                g.setColor(new Color(255, 255, 255, 30));
                g.fillRect(trackX + 5, scrollTrackY, 4, scrollTrackHeight);

                // Hitung Posisi Jempol (Thumb)
                int totalScrollableItems = leaderboardData.length - maxVisibleRows;
                int thumbHeight = Math.max(30, scrollTrackHeight / (totalScrollableItems + 1)); 
                int scrollablePixelRange = scrollTrackHeight - thumbHeight;
                
                // Posisi Y berdasarkan scrollOffset dari Presenter
                int thumbY = scrollTrackY + (scrollOffset * scrollablePixelRange / totalScrollableItems);

                // Update Rect biar Presenter tau posisinya
                scrollBarRect = new Rectangle(trackX, thumbY, 14, thumbHeight);

                // Gambar Jempol
                if (isDraggingScroll) g.setColor(new Color(200, 200, 200)); 
                else g.setColor(Color.WHITE);
                
                g.fillRoundRect(scrollBarRect.x, scrollBarRect.y, scrollBarRect.width, scrollBarRect.height, 10, 10);
            }
        }

        // INPUT FIELD
        int inputY = boardY + boardH + 20;
        g.setFont(getFontSafe(20f));
        g.setColor(Color.WHITE);
        g.drawString("Username:", boardX + 80, inputY + 20);
        usernameField.setBounds(boardX + 200, inputY, 200, 30);

        // 8. TOMBOL
        drawButton(g, "PLAY", 1, inputY + 50, true);
        drawButton(g, "QUIT", 2, inputY + 50, false);
    }

    // metode bantu buat gambar tombol
    private void drawButton(Graphics g, String text, int type, int y, boolean isLeft) {
        int w = 180;
        int h = 55;
        int x = isLeft ? (getWidth() / 2) - w - 10 : (getWidth() / 2) + 10;
        
        if (type == 1) playBtnRect = new Rectangle(x, y, w, h);
        else quitBtnRect = new Rectangle(x, y, w, h);

        g.setFont(getFontSafe(24f));

        if (pressedButtonType == type) {
            g.drawImage(btnImage, x + 2, y + 2, w - 4, h - 4, null);
            drawCenteredString(g, text, x + w/2, y + 33); 
        } else {
            g.drawImage(btnImage, x, y, w, h, null);
            drawCenteredString(g, text, x + w/2, y + 33);
        }
    }

    // metode bantu buat gambar string di tengah
    private void drawCenteredString(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x - (fm.stringWidth(text) / 2), y);
    }

    // Metode buat nambahin Mouse Listener dari Presenter
    public void addMouseListenerToPanel(MouseAdapter listener) {
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
        this.addMouseWheelListener(listener);
    }

    // Setter buat Update Tampilan
    public void setScrollOffset(int offset) { this.scrollOffset = offset; repaint(); }
    public void setSelectedRowIndex(int index) { this.selectedRowIndex = index; repaint(); }
    public void setPressedButtonType(int type) { this.pressedButtonType = type; repaint(); }
    public void setIsDraggingScroll(boolean isDragging) { this.isDraggingScroll = isDragging; repaint(); }
    
    // Getter buat State & Data
    public Object[][] getLeaderboardData() { return leaderboardData; }
    public int getScrollOffset() { return scrollOffset; }
    public JTextField getUsernameField() { return usernameField; }

    // Setter Data Awal
    public void setLeaderboardData(Object[][] data) {
        this.leaderboardData = data;
        this.scrollOffset = 0;
        this.selectedRowIndex = -1;
        repaint();
    }
}