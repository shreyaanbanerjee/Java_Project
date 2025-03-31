// MusicLibrary.java
import java.io.*;
import java.util.*;

public class MusicLibrary {
    private File libraryFile;
    private File musicFolder;
    
    /**
     * @param musicFolderPath The folder where your MP3 files are stored.
     * @param libraryFilePath The file path where the list of music files will be saved.
     */
    public MusicLibrary(String musicFolderPath, String libraryFilePath) {
        this.musicFolder = new File(musicFolderPath);
        if (!musicFolder.exists()) {
            musicFolder.mkdirs();
        }
        this.libraryFile = new File(libraryFilePath);
        if (!libraryFile.exists()) {
            try {
                libraryFile.createNewFile();
                updateLibrary();
            } catch(IOException e) {
                System.out.println("Error creating library file: " + e.getMessage());
            }
        }
    }
    
    // Scan the music folder and update the library file with the list of MP3 file paths.
    public void updateLibrary() {
        File[] songs = musicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        try (PrintWriter pw = new PrintWriter(new FileWriter(libraryFile))) {
            if (songs != null) {
                for (File song : songs) {
                    pw.println(song.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating music library: " + e.getMessage());
        }
    }
    
    // Read the library file and return a list of song file paths.
    public List<String> getSongList() {
        List<String> songList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(libraryFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                songList.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading music library: " + e.getMessage());
        }
        return songList;
    }
}
