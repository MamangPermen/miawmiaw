import view.MainFrame;

// Saya Nadhif Arva Anargya mengerjakan evaluasi Tugas Masa Depan dalam mata kuliah 
// Desain dan Pemrograman Berorientasi Objek untuk keberkahanNya maka saya 
// tidak melakukan kecurangan seperti yang telah dispesifikasikan. Aamiin.

// Kelas Main adalah titik masuk utama aplikasi
// Kelas ini bertanggung jawab untuk memulai aplikasi dengan menampilkan MainFrame

public class Main 
{
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new MainFrame();
        });
    }
}