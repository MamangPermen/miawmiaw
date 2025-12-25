package model;
import java.awt.*;

public abstract class GameObject 
{
    // Pake protected biar subclass bisa akses langsung
    protected int posX, posY;
    protected int width, height;
    protected int velX, velY; // Butuh X juga buat gerak Kiri-Kanan
    protected Image image;

    public GameObject(int posX, int posY, int width, int height, Image image) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.image = image;
        this.velX = 0;
        this.velY = 0;
    }

    // GETTER
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Image getImage() { return image; }
    public int getVelX() { return velX; }
    public int getVelY() { return velY; }

    // SETTER
    public void setPosX(int posX) { this.posX = posX; }
    public void setPosY(int posY) { this.posY = posY; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setImage(Image image) { this.image = image; }
    public void setVelX(int velX) { this.velX = velX; }
    public void setVelY(int velY) { this.velY = velY; }

    public void update() {
        posX += velX;
        posY += velY;
    }

    // Buat deteksi tabrakan
    public Rectangle getBounds() {
        return new Rectangle(posX, posY, width, height);
    }
}