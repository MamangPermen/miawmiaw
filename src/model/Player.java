package model;
import java.awt.*;

public class Player extends GameObject 
{
    private String username;
    private int score;
    private int ammo;
    private int missedBullets; // Buat syarat nambah peluru
    private boolean facingRight = false; // arah hadap player, default ke kiri

    public Player(int posX, int posY, int width, int height, Image image, String username) {
        super(posX, posY, width, height, image);
        this.username = username;
        this.score = 0;
        this.ammo = 0;
        this.missedBullets = 0;
    }

    // buat ngatur arah hadap player berdasarkan velX
    @Override
    public void setVelX(int velX) {
        super.setVelX(velX);
        
        // Kalo gerak KANAN -> Set facingRight = TRUE
        if (velX > 0) {
            facingRight = true;
        }
        // Kalo gerak KIRI -> Set facingRight = FALSE
        else if (velX < 0) {
            facingRight = false;
        }
        // Kalo DIAM (velX == 0) -> JANGAN UBAH APA-APA (Biar inget terakhir kemana)
    }

    // Getter Setter Data Player
    public String getUsername() { return username; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getAmmo() { return ammo; }
    public void setAmmo(int ammo) { this.ammo = ammo; }
    public int getMissedBullets() { return missedBullets; }
    public void setMissedBullets(int missedBullets) { this.missedBullets = missedBullets; }
    public boolean isFacingRight() { return facingRight; }
}