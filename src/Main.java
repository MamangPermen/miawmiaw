import view.MainFrame;

public class Main {
    public static void main(String[] args) {
        // Jalanin di Thread GUI yang aman
        javax.swing.SwingUtilities.invokeLater(() -> {
            new MainFrame();
        });
    }
}