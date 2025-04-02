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

    // // Simulated method to skip forward 10 seconds.
    // public void skipForward() {
    //     // Implementation would require tracking frames and using AdvancedPlayer’s play(int start, int end) methods.
    //     System.out.println("Skipping forward 10 seconds (simulated)...");
    // }
    // // Simulated method for fast forwarding.
    // public void fastForward() {
    //     // Implementation would similarly adjust the playback position.
    //     System.out.println("Fast forwarding (simulated)...");
    // }
    // The run method is used for playback in a thread.
    @Override
    public void run() {
        do {
            try (FileInputStream fis = new FileInputStream(mp3File)) {
                player = new AdvancedPlayer(fis);
                // This call blocks until playback completes.
                player.play();
            } catch (Exception e) {
                System.out.println("Error playing file: " + e.getMessage());
            }
            // If looping is enabled and stop hasn’t been requested, play the song again.
        } while (loop && !stopRequested);
        // Optionally signal auto-play next song if integrated in the UI
        System.out.println("Playback finished.");
    }
}
