package com.mp3player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicLibrary {

    private File libraryFile;
    private File musicFolder;


    public MusicLibrary(String musicFolderPath, String libraryFilePath) {
        this.musicFolder = new File(musicFolderPath);
        if (!musicFolder.exists()) {
            musicFolder.mkdirs();
        }
        this.libraryFile = new File(libraryFilePath);
        if (!libraryFile.exists()) {
            try {
                libraryFile.createNewFile();
                // updateLibrary();
            } catch (IOException e) {
                System.out.println("Error creating library file: " + e.getMessage());
            }
        }
    }

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
