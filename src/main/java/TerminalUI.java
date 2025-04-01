import java.io.File;
import java.util.List;
import java.util.Scanner;

public class TerminalUI {
    private Scanner scanner;
    private File mp3Folder;
    private PlaylistManager playlistManager;
    private MP3Player currentPlayer;
    private MusicLibrary musicLibrary;
    private String mp3Directory; 

    public TerminalUI(String mp3Directory, String playlistsDirectory) {
        scanner = new Scanner(System.in);
        this.mp3Directory = mp3Directory;
        mp3Folder = new File(mp3Directory);
        if (!mp3Folder.exists()) {
            mp3Folder.mkdirs();
        }
        
        musicLibrary = new MusicLibrary(mp3Directory, mp3Directory + File.separator + "music_library.txt");
    
        musicLibrary.updateLibrary();
        
        playlistManager = new PlaylistManager(playlistsDirectory);
    }

    public void start() {
        while (true) {
            showMainMenu();
            System.out.print("\nYour choice: > ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    listAndPlaySong();
                    break;
                case "2":
                    if (currentPlayer != null) {
                        currentPlayer.stop();
                    }
                    System.out.println("\nThank you for using MP3 Player! Goodbye!\n");
                    System.exit(1);
                default:
                    System.out.println("\n[ERROR] Invalid choice. Please try again.");
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n================================");
        System.out.println("        MP3 PLAYER MENU        ");
        System.out.println("================================");
        System.out.println("[1] List and play songs");
        System.out.println("[2] Exit");
        System.out.println("================================");
    }

    private void listAndPlaySong() {
        List<String> songPaths = musicLibrary.getSongList();
        if (songPaths.isEmpty()) {
            System.out.println("\n[WARNING] No MP3 files found in: " + mp3Folder.getAbsolutePath());
            return;
        }
        System.out.println("\nAVAILABLE SONGS");
        System.out.println("================================");
        for (int i = 0; i < songPaths.size(); i++) {
            String fileName = new File(songPaths.get(i)).getName();
            System.out.println("(" + (i + 1) + ") " + fileName);
        }
        System.out.println("================================");
        System.out.print("Select a song number to play: > ");
        try {
            int selection = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (selection < 0 || selection >= songPaths.size()) {
                System.out.println("\n[ERROR] Invalid selection. Try again.");
                return;
            }
           
            if (currentPlayer != null) {
                currentPlayer.stop();
            }
            File selectedSong = new File(songPaths.get(selection));
            currentPlayer = new MP3Player(selectedSong);
            currentPlayer.play();
            System.out.println("\n[PLAYING] " + selectedSong.getName());
            playbackControls();
        } catch (NumberFormatException e) {
            System.out.println("\n[ERROR] Invalid input. Please enter a valid number.");
        }
    }

    private void playbackControls() {
        System.out.println("\nPLAYBACK CONTROLS");
        System.out.println("================================");
        System.out.println("[L] Toggle loop");
        System.out.println("[Q] Stop playback and return to main menu");
        System.out.println("================================");
        while (true) {
            System.out.print("Enter command: > ");
            String command = scanner.nextLine().trim().toLowerCase();
            if (command.equals("l")) {
                currentPlayer.setLoop(true);
                System.out.println("[LOOP] Looping enabled.");
            } else if (command.equals("q")) {
                currentPlayer.stop();
                System.out.println("[STOPPED] Playback stopped.");
                break;
            } else {
                System.out.println("\n[ERROR] Unknown command. Please try again.");
            }
        }
    }
}


