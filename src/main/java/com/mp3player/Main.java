
import com.mp3player.TerminalUI;

public class Main {

    public static void main(String[] args) {

        String mp3Directory = "./songs";
        String playlistsDirectory = "./playlists";

        TerminalUI ui = new TerminalUI(mp3Directory, playlistsDirectory);
        ui.start();
    }
}
