package model;
import java.awt.*;

public class Bullet extends GameObject {
    private boolean isEnemy; // true = peluru alien, false = peluru player

    public Bullet(int posX, int posY, int width, int height, Image image, boolean isEnemy) {
        super(posX, posY, width, height, image);
        this.isEnemy = isEnemy;
    }

    public boolean isEnemy() { return isEnemy; }
}