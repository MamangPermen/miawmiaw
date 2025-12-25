package model;

public class User {
    private String username;
    private int totalScore;      // Skor kumulatif/tersimpan
    private int savedAmmo;       // Peluru sisa game sebelumnya
    private int savedMissed;     // Statistik

    public User(String username, int totalScore, int savedMissed, int savedAmmo) {
        this.username = username;
        this.totalScore = totalScore;
        this.savedMissed = savedMissed;
        this.savedAmmo = savedAmmo;
    }

    // Getter Setter Data
    public String getUsername() { return username; }
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public int getSavedAmmo() { return savedAmmo; }
    public void setSavedAmmo(int savedAmmo) { this.savedAmmo = savedAmmo; }
    public int getSavedMissed() { return savedMissed; }
    public void setSavedMissed(int savedMissed) { this.savedMissed = savedMissed; }
}