package model;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sound 
{
    Clip clip; // objek Clip buat mainin suara
    URL soundURL[] = new URL[10]; // siapin 10 slot suara

    public Sound() {
        // Load file suara ke dalam array
        // Pastikan nama file sesuai sama yang ada di folder assets
        soundURL[0] = getClass().getResource("/assets/sound/menu.wav");
        soundURL[1] = getClass().getResource("/assets/sound/game.wav");
        soundURL[2] = getClass().getResource("/assets/sound/shoot.wav");
        soundURL[3] = getClass().getResource("/assets/sound/hit.wav");
    }

    public void setFile(int i) { // pilih file suara berdasarkan index
        // Muat file suara
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            System.out.println("Gagal load file suara index: " + i);
        }
    }

    public void play() { // mainkan sound
        if (clip != null) {
            clip.setFramePosition(0); // Mulai dari awal
            clip.start();
        }
    }

    public void loop() { // loop sound
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() { // stop sound
        if (clip != null) {
            clip.stop();
        }
    }
}