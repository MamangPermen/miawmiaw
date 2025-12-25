package model;
import java.awt.*;

public class Player extends GameObject 
{
    private String username;
    private int score;
    private int ammo;
    private int missedBullets; // Buat syarat nambah peluru

    public Player(int posX, int posY, int width, int height, Image image, String username) {
        super(posX, posY, width, height, image);
        this.username = username;
        this.score = 0;
        this.ammo = 0;
        this.missedBullets = 0;
    }

    // Getter Setter Data Player
    public String getUsername() { return username; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getAmmo() { return ammo; }
    public void setAmmo(int ammo) { this.ammo = ammo; }
    public int getMissedBullets() { return missedBullets; }
    public void setMissedBullets(int missedBullets) { this.missedBullets = missedBullets; }
}