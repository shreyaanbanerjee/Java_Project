
import com.mp3player.TerminalUI;

public class Main {

    public static void main(String[] args) {

        String mp3Directory = "./songs";

        TerminalUI ui = new TerminalUI(mp3Directory);
        ui.start();
    }
}
