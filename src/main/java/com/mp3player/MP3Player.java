package com.mp3player;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import java.io.FileInputStream;
import java.io.IOException;

public class MP3Player {
    private Player player;
    private FileInputStream fileContent;
    private String filePath;
    private volatile boolean isPlaying;

    public MP3Player(String filePath) {
        this.filePath = filePath;
    }

    public boolean play() {
        try {
            fileContent = new FileInputStream(filePath);
            player = new Player(fileContent);
            isPlaying = true;

            new Thread(() -> {
                try {
                    player.play();
                } catch (JavaLayerException err) {
                    System.out.println("Error playing MP3 file: " + err.getMessage());
                } finally {
                    stop();
                }
            }).start();

            return true;
        } catch (IOException | JavaLayerException err) {
            System.out.println("Error: " + err.getMessage());
            return false;
        }
    }

    public void stop() {
        if (player != null) {
            player.close();
        }
        if (fileContent != null) {
            try {
                fileContent.close();
            } catch (IOException e) {
                System.out.println("Error closing file: " + e.getMessage());
            }
        }
        isPlaying = false;
    }
}

