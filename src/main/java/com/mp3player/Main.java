// Main.java

import com.mp3player.TerminalUI;

public class Main {

    public static void main(String[] args) {

        String mp3Directory = "./songs";         // Folder for your MP3s (your music library)
        String playlistsDirectory = "./playlists"; // Folder for playlists

        TerminalUI ui = new TerminalUI(mp3Directory, playlistsDirectory);
        ui.start();
    }
}
