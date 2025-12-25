package model;
import java.awt.*;
import java.util.*;

public class Enemy extends GameObject // kelas untuk musuh turunan dari GameObject
{
    private int shootCooldown; // variabel untuk mengatur jeda tembakan musuh
    private boolean facingRight = false; // arah hadap musuh, default ke kiri

    public Enemy(int posX, int posY, int width, int height, Image image) {
        super(posX, posY, width, height, image);

        // untuk mengatur jeda tembakan musuh
        this.shootCooldown = new Random().nextInt(60);
    }

    // Kurangi cooldown tiap frame
    public void tickCooldown() {
        if (shootCooldown > 0) shootCooldown--;
    }

    public void resetCooldown() {
        shootCooldown = 90; // Tiap 1 detik baru bisa nembak lagi
    }

    // Getter Setter buat Cooldown
    public int getShootCooldown() { return shootCooldown; }
    public boolean isFacingRight() { return facingRight;}
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
}