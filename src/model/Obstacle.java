package model;

import java.awt.Image;

public class Obstacle extends GameObject // kelas untuk obstacle turunan dari GameObject
{
    public Obstacle(int posX, int posY, int width, int height, Image image) {
        super(posX, posY, width, height, image);
    }
}