package com.mp3player;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static final String RESET = "\u001B[0m", BLUE = "\u001B[36m", GREEN = "\u001B[32m", RED = "\u001B[31m", BOLD = "\u001B[1m";

    public static void main(String[] args) {
        File assetsDir = new File("assets");
        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            System.out.println(RED + BOLD + "\n⚠ ERROR: 'assets' directory not found!" + RESET);
            return;
        }

        File[] mp3Files = assetsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if (mp3Files == null || mp3Files.length == 0) {
            System.out.println(RED + BOLD + "\n⚠ ERROR: No MP3 files found in 'assets' directory!" + RESET);
            return;
        }

        System.out.println(BLUE + "\n🎵 Available MP3 Files:" + RESET);
        for (int i = 0; i < mp3Files.length; i++) {
            System.out.println(GREEN + (i + 1) + ". " + mp3Files[i].getName() + RESET);
        }

        System.out.println(BLUE + "\n⌨ Enter the number of the song you want to play:" + RESET);
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(RED + "Invalid input. Please enter a number." + RESET);
            return;
        }

        if (choice < 1 || choice > mp3Files.length) {
            System.out.println(RED + "Invalid choice. Please select a valid number from the list." + RESET);
            return;
        }

        String filePath = mp3Files[choice - 1].getAbsolutePath();
        MP3Player player = new MP3Player(filePath);

        System.out.println(BLUE + "\n🎵 MP3 Player" + RESET);
        System.out.println("──────────────────────────────────────");
        System.out.println(GREEN + "▶ Playing: " + filePath + RESET);

        boolean success = player.play();
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
