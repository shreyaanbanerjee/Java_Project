package com.mp3player;

import java.io.File;
import java.io.FileInputStream;

import javazoom.jl.player.advanced.AdvancedPlayer;

public class MP3Player implements Runnable {

    private File mp3File;
    private AdvancedPlayer player;
    private boolean loop;
    private boolean stopRequested;
    private Thread playbackThread;

    public MP3Player(File mp3File) {
        this.mp3File = mp3File;
        this.loop = false;
        this.stopRequested = false;
    }

    // Start playback in a new thread.
    public void play() {
        stopRequested = false;
        playbackThread = new Thread(this);
        playbackThread.start();
    }

    // Stop playback.
    public void stop() {
        stopRequested = true;
        if (player != null) {
            player.close();
        }
    }

    // Toggle looping.
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    @Override
    public void run() {
        do {
            try (FileInputStream fis = new FileInputStream(mp3File)) {
                player = new AdvancedPlayer(fis);
                player.play();
            } catch (Exception e) {
                System.out.println("Error playing file: " + e.getMessage());
            }
        } while (loop && !stopRequested);
        System.out.println("Playback finished.");
    }
}
