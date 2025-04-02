// PlaylistManager.java
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class PlaylistManager {
    private File playlistsRoot;

    public PlaylistManager(String playlistsDirectory) {
        playlistsRoot = new File(playlistsDirectory);
        if (!playlistsRoot.exists()) {
            playlistsRoot.mkdirs();
        }
    }

    // Create a new playlist folder.
    public boolean createPlaylist(String playlistName) {
        File playlist = new File(playlistsRoot, playlistName);
        if (!playlist.exists()) {
            return playlist.mkdirs();
        }
        return false;
    }

    // Rename a playlist.
    public boolean renamePlaylist(String oldName, String newName) {
        File oldFolder = new File(playlistsRoot, oldName);
        File newFolder = new File(playlistsRoot, newName);
        return oldFolder.exists() && oldFolder.renameTo(newFolder);
    }

    // Add a song to a playlist (copies the file).
    public boolean addSongToPlaylist(File songFile, String playlistName) {
        File playlistFolder = new File(playlistsRoot, playlistName);
        if (!playlistFolder.exists()) {
            System.out.println("Playlist does not exist.");
            return false;
        }
        try {
            Path target = Paths.get(playlistFolder.getAbsolutePath(), songFile.getName());
            Files.copy(songFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("Error adding song: " + e.getMessage());
            return false;
        }
    }

    // Remove a song from a playlist.
    public boolean removeSongFromPlaylist(String songName, String playlistName) {
        File songFile = new File(playlistsRoot + File.separator + playlistName, songName);
        if (songFile.exists()) {
            return songFile.delete();
        }
        return false;
    }
}
