package com.mp3player;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MP3Player {
    private Player player;
    private FileInputStream fileInputStream;
    private String filePath;

    public MP3Player(String filePath) {
        this.filePath = filePath;
    }

    public boolean play() {
        try {
            fileInputStream = new FileInputStream(filePath);
            player = new Player(fileInputStream);
            
            // Create a new thread for playing music
            new Thread(() -> {
                try {
                    player.play();
                } catch (JavaLayerException e) {
                    System.out.println("Error playing MP3 file: " + e.getMessage());
                }
            }).start();
            
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
            return false;
        } catch (JavaLayerException e) {
            System.out.println("JavaLayer error: " + e.getMessage());
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
