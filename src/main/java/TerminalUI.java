import java.io.File;
import java.util.List;
import java.util.Scanner;

public class TerminalUI {
    private Scanner scanner;
    private File mp3Folder;
    private PlaylistManager playlistManager;
    private MP3Player currentPlayer;
    private MusicLibrary musicLibrary;
    private String mp3Directory; // Keep track of the folder path

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
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    listAndPlaySong();
                    break;
                // case "2":
                //     managePlaylists();
                //     break;
                case "2":
                    if (currentPlayer != null) {
                        currentPlayer.stop();
                    }
                    System.out.println("Exiting MP3 Player.");
                    System.exit(1);
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n==== MP3 Player Main Menu ====");
        System.out.println("1. List and play songs");
        //System.out.println("2. Manage playlists");
        System.out.println("2. Exit");
        System.out.print("Enter your choice: ");
    }

    private void listAndPlaySong() {
        
        List<String> songPaths = musicLibrary.getSongList();
        if (songPaths.isEmpty()) {
            System.out.println("No MP3 files found in " + mp3Folder.getAbsolutePath());
            return;
        }
        System.out.println("\nAvailable Songs:");
        for (int i = 0; i < songPaths.size(); i++) {
            
            String fileName = new File(songPaths.get(i)).getName();
            System.out.println((i + 1) + ". " + fileName);
        }
        System.out.print("Select a song number to play: ");
        try {
            int selection = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (selection < 0 || selection >= songPaths.size()) {
                System.out.println("Invalid selection.");
                return;
            }
           
            if (currentPlayer != null) {
                currentPlayer.stop();
            }
            File selectedSong = new File(songPaths.get(selection));
            currentPlayer = new MP3Player(selectedSong);
            currentPlayer.play();
            System.out.println("Playing: " + selectedSong.getName());
            playbackControls();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void playbackControls() {
        System.out.println("\nPlayback Controls:");
        System.out.println("l - Toggle loop");
        // System.out.println("s - Skip forward 10 seconds");
        // System.out.println("f - Fast forward");
        System.out.println("q - Stop playback and return to main menu");
        while (true) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();
            if (command.equals("l")) {
                currentPlayer.setLoop(true);
                System.out.println("Looping enabled.");
            } 
            // else if (command.equals("s")) {
            //     currentPlayer.skipForward();
            // } 
            // else if (command.equals("f")) {
            //     currentPlayer.fastForward();
            // } 
            else if (command.equals("q")) {
                currentPlayer.stop();
                break;
            } else {
                System.out.println("Unknown command.");
            }
        }
    }

    // private void managePlaylists() {
    //     while (true) {
    //         System.out.println("\n==== Playlist Manager ====");
    //         System.out.println("1. Create new playlist");
    //         System.out.println("2. Rename playlist");
    //         System.out.println("3. Add song to playlist");
    //         System.out.println("4. Remove song from playlist");
    //         System.out.println("5. Exit playlist manager");
    //         System.out.print("Enter your choice: ");
    //         String choice = scanner.nextLine().trim();
    //         switch (choice) {
    //             case "1":
    //                 System.out.print("Enter new playlist name: ");
    //                 String newPlaylist = scanner.nextLine().trim();
    //                 if (playlistManager.createPlaylist(newPlaylist)) {
    //                     System.out.println("Playlist created successfully.");
    //                 } else {
    //                     System.out.println("Could not create playlist (maybe it already exists).");
    //                 }
    //                 break;
    //             case "2":
    //                 System.out.print("Enter current playlist name: ");
    //                 String oldName = scanner.nextLine().trim();
    //                 System.out.print("Enter new playlist name: ");
    //                 String renamed = scanner.nextLine().trim();
    //                 if (playlistManager.renamePlaylist(oldName, renamed)) {
    //                     System.out.println("Playlist renamed successfully.");
    //                 } else {
    //                     System.out.println("Could not rename playlist.");
    //                 }
    //                 break;
    //             case "3":
    //                 System.out.print("Enter the full path of the MP3 file to add: ");
    //                 String songPath = scanner.nextLine().trim();
    //                 File songFile = new File(songPath);
    //                 if (!songFile.exists()) {
    //                     System.out.println("File does not exist.");
    //                     break;
    //                 }
    //                 System.out.print("Enter the playlist name: ");
    //                 String targetPlaylist = scanner.nextLine().trim();
    //                 if (playlistManager.addSongToPlaylist(songFile, targetPlaylist)) {
    //                     System.out.println("Song added to playlist.");
    //                 } else {
    //                     System.out.println("Failed to add song to playlist.");
    //                 }
    //                 break;
    //             case "4":
    //                 System.out.print("Enter the playlist name: ");
    //                 String plName = scanner.nextLine().trim();
    //                 System.out.print("Enter the song name (with extension): ");
    //                 String songName = scanner.nextLine().trim();
    //                 if (playlistManager.removeSongFromPlaylist(songName, plName)) {
    //                     System.out.println("Song removed from playlist.");
    //                 } else {
    //                     System.out.println("Failed to remove song.");
    //                 }
    //                 break;
    //             case "5":
    //                 return;
    //             default:
    //                 System.out.println("Invalid choice.");
    //         }
    //     }
    // }
}