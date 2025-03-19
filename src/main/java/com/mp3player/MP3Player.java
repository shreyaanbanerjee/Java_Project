package com.mp3player;

import java.io.FileInputStream;
import java.io.IOException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

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
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide the path to an MP3 file");
            return;
        }

        String filePath = args[0];
        MP3Player player = new MP3Player(filePath);

        System.out.println("Playing: " + filePath);
        boolean success = player.play();

        if (success) {
            System.out.println("Playback started. Press Enter to stop...");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.stop();
        }
    }
}
