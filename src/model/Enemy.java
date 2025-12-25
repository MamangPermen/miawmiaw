package model;
import java.awt.*;
import java.util.*;

public class Enemy extends GameObject 
{
    private int shootCooldown; // variabel untuk mengatur jeda tembakan musuh
    private boolean facingRight = false; // arah hadap musuh, default ke kiri

    public Enemy(int posX, int posY, int width, int height, Image image) {
        super(posX, posY, width, height, image);

        // untuk mengatur jeda tembakan musuh
        this.shootCooldown = new Random().nextInt(180);
    }

    // Getter Setter buat Cooldown
    public int getShootCooldown() { return shootCooldown; }
    public void setShootCooldown(int shootCooldown) { this.shootCooldown = shootCooldown; }
    public void decreaseCooldown() { this.shootCooldown--; }
    public boolean isFacingRight() { return facingRight;}
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
}