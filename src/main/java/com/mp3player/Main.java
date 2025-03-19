package com.mp3player;

import java.io.IOException;

public class Main {
    // ANSI escape codes for colored text
    private static final String RESET = "\u001B[0m", BLUE = "\u001B[36m", GREEN = "\u001B[32m",  RED = "\u001B[31m", BOLD = "\u001B[1m";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(RED + BOLD + "\n⚠ ERROR: No file path provided!" + RESET);
            System.out.println("Usage: java -jar mp3player.jar <MP3_FILE_PATH>");
            return;
        }

        String filePath = args[0];
        MP3Player player = new MP3Player();

        System.out.println(BLUE + "\n🎵 MP3 Player" + RESET);
        System.out.println("──────────────────────────────────────");
        System.out.println(GREEN + "▶ Playing: " + filePath + RESET);

        boolean success = player.play(filePath);

        if (success) {
            System.out.println(BLUE + "\n⌨ Press Enter to stop playback..." + RESET);
            try {
                System.in.read();
            } catch (IOException e) {
                System.out.println(RED + "Error: " + e.getMessage() + RESET);
            }
            player.stop();
            System.out.println(GREEN + "⏹ Playback stopped." + RESET);
        }
    }
}

