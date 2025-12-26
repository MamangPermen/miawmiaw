package presenter;

// Interface IGamePresenter adalah kontrak untuk Presenter Game.
// Interface ini mendefinisikan perintah dasar pengendalian game (Start/Stop) 
// yang dapat dipanggil oleh MainFrame atau komponen lain.

public interface IGamePresenter 
{
    void startGame();
    void stopGame();
}