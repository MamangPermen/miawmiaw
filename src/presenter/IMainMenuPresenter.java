package presenter;

import java.awt.event.MouseAdapter;
import javax.swing.JTextField;

// Interface IMainMenuPresenter adalah kontrak untuk Presenter Menu.
// Interface ini mendefinisikan perintah-perintah eksternal yang bisa diterima oleh 
// Presenter Menu, seperti memuat data awal atau menutup koneksi database.

public interface IMainMenuPresenter 
{
    void loadData(); // Load data awal
    void shutDown(); // Tutup koneksi DB
}