package com.mp3player;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import java.io.FileInputStream;
import java.io.IOException;

public class MP3Player {
    private Player player;
    private FileInputStream fileInputStream;
    private String filePath;
    private volatile boolean isPlaying;

    public MP3Player(String filePath) {
        this.filePath = filePath;
    }

    public boolean play() {
        try {
            fileInputStream = new FileInputStream(filePath);
            player = new Player(fileInputStream);
            isPlaying = true;

            new Thread(() -> {
                try {
                    player.play();
                } catch (JavaLayerException e) {
                    System.out.println("Error playing MP3 file: " + e.getMessage());
                } finally {
                    stop();
                }
            }).start();

            return true;
        } catch (IOException | JavaLayerException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public void stop() {
        if (player != null) {
            player.close();
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                System.out.println("Error closing file: " + e.getMessage());
            }
        }
        isPlaying = false;
    }
}

