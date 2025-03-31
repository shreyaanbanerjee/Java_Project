import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MP3Player {

    private static final Logger LOGGER = Logger.getLogger(MP3Player.class.getName());

    private final File mp3File;
    private AdvancedPlayer player;
    private final ExecutorService playbackExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "MP3PlayerThread");
        t.setDaemon(true);
        return t;
    });
    private Future<?> playbackTask;

    private volatile boolean loop = false;
    private volatile boolean stopRequested = false;
    private volatile boolean paused = false;
    private volatile int currentFrame = 0;

    // Frame info structure
    private static class FrameInfo {
        final long offset;      // Byte offset in the file
        final float timeMillis; // Cumulative time in milliseconds at the start of the frame

        FrameInfo(long offset, float timeMillis) {
            this.offset = offset;
            this.timeMillis = timeMillis;
        }
    }
    private List<FrameInfo> frameData;
    private int totalFrames;
    private float totalMillis;
    private boolean analyzed = false;
    private final Object analysisLock = new Object();
    private final Object playerLock = new Object();

    public MP3Player(File mp3File) {
        this.mp3File = mp3File;
    }

    /**
     * Analyze the MP3 file to build frame index information for seeking.
     */
    public boolean analyzeFrames() {
        synchronized (analysisLock) {
            if (analyzed) {
                return true;
            }
            LOGGER.info("Starting MP3 frame analysis for: " + mp3File.getName());
            frameData = new ArrayList<>();
            totalFrames = 0;
            totalMillis = 0;
            boolean success = false;

            try (FileInputStream fis = new FileInputStream(mp3File);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                Bitstream bitstream = new Bitstream(bis);
                Header frameHeader;
                // Read and record each frame’s offset and cumulative time.
                while ((frameHeader = bitstream.readFrame()) != null) {
                    // Get the current file pointer directly from the channel.
                    long currentOffset = fis.getChannel().position();
                    frameData.add(new FrameInfo(currentOffset, totalMillis));
                    totalMillis += frameHeader.ms_per_frame();
                    totalFrames++;
                    bitstream.closeFrame();
                }
                success = true;
                LOGGER.info(String.format("Analysis complete: %d frames, %.2f seconds",
                        totalFrames, totalMillis / 1000.0f));
            } catch (IOException | JavaLayerException e) {
                LOGGER.log(Level.SEVERE, "Error analyzing MP3 file: " + e.getMessage(), e);
                frameData = null;
                totalFrames = 0;
                totalMillis = 0;
                success = false;
            }
            analyzed = success;
            return analyzed;
        }
    }

    /**
     * Starts or resumes playback.
     */
    public void play() {
        synchronized (analysisLock) {
            if (!analyzed) {
                if (!analyzeFrames()) {
                    LOGGER.severe("Cannot play: MP3 analysis failed.");
                    return;
                }
            }
            if (totalFrames == 0) {
                LOGGER.warning("Cannot play: No frames found in MP3 analysis.");
                return;
            }
        }
        synchronized (playerLock) {
            if (playbackTask != null && !playbackTask.isDone()) {
                if (paused) {
                    resumePlayback();
                } else {
                    LOGGER.warning("Playback already in progress.");
                }
                return;
            }
            // Reset to beginning if reached end
            if (currentFrame >= totalFrames) {
                currentFrame = 0;
            }
            stopRequested = false;
            paused = false;
            LOGGER.info("Starting playback from frame: " + currentFrame);
            playbackTask = playbackExecutor.submit(this::playbackLoop);
        }
    }

    private void resumePlayback() {
        synchronized (playerLock) {
            if (paused && player != null) {
                LOGGER.info("Resuming playback.");
                paused = false;
                stopRequested = false;
                if (playbackTask == null || playbackTask.isDone()) {
                    playbackTask = playbackExecutor.submit(this::playbackLoop);
                } else {
                    LOGGER.warning("Resume called but task already running?");
                }
            }
        }
    }

    /**
     * Stops playback.
     */
    public void stop() {
        synchronized (playerLock) {
            LOGGER.info("Stop requested.");
            stopRequested = true;
            paused = false;
            if (player != null) {
                player.close();
                player = null;
            }
            if (playbackTask != null) {
                playbackTask.cancel(true);
                playbackTask = null;
            }
        }
    }

    /**
     * Enable or disable looping.
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
        LOGGER.info("Looping set to: " + loop);
    }

    /**
     * Skip forward by a given number of seconds.
     */
    public void skipForward(float seconds) {
        seek(seconds);
    }

    /**
     * Skip backward by a given number of seconds.
     */
    public void skipBackward(float seconds) {
        seek(-seconds);
    }

    /**
     * Seek to an absolute time (in seconds) from the start.
     */
    public void seekToTime(float seconds) {
        synchronized (analysisLock) {
            if (!analyzed) {
                LOGGER.warning("Cannot seek: MP3 not analyzed yet.");
                return;
            }
            if (totalFrames == 0 || frameData == null) return;
            float targetMillis = seconds * 1000.0f;
            int targetFrame = findFrameForTime(targetMillis);
            seekToFrame(targetFrame);
        }
    }

    /**
     * Seeks relative to the current position.
     */
    private void seek(float secondsOffset) {
        synchronized (analysisLock) {
            if (!analyzed) {
                LOGGER.warning("Cannot seek: MP3 not analyzed yet.");
                return;
            }
            if (totalFrames == 0 || frameData == null) return;
            float currentMillis = (currentFrame < frameData.size())
                    ? frameData.get(currentFrame).timeMillis
                    : totalMillis;
            float targetMillis = currentMillis + (secondsOffset * 1000.0f);
            int targetFrame = findFrameForTime(targetMillis);
            seekToFrame(targetFrame);
        }
    }

    /**
     * Jumps playback to a specific frame.
     */
    private void seekToFrame(int targetFrame) {
        synchronized (playerLock) {
            if (totalFrames == 0) return;
            if (targetFrame < 0) {
                targetFrame = 0;
            } else if (targetFrame >= totalFrames) {
                targetFrame = totalFrames;
            }
            LOGGER.info("Seeking to frame: " + targetFrame);
            currentFrame = targetFrame;
            // If playing, stop and restart from new frame
            if (playbackTask != null && !playbackTask.isDone() && !paused) {
                stopRequested = true;
                if (player != null) {
                    player.close();
                    player = null;
                }
                if (playbackTask != null) {
                    playbackTask.cancel(true);
                    playbackTask = null;
                }
                play();
            } else if (paused) {
                paused = false;
                play();
            }
        }
    }

    /**
     * Finds the frame index closest to the given target time.
     */
    private int findFrameForTime(float targetMillis) {
        if (frameData == null || frameData.isEmpty() || targetMillis <= 0) {
            return 0;
        }
        if (targetMillis >= totalMillis) {
            return totalFrames;
        }
        int low = 0;
        int high = frameData.size() - 1;
        int result = 0;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (frameData.get(mid).timeMillis < targetMillis) {
                result = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }

    /**
     * Internal playback loop that handles playing from the current frame,
     * seeking, and looping.
     */
    private void playbackLoop() {
        PlaybackListener listener = new PlaybackListener() {
            @Override
            public void playbackFinished(PlaybackEvent evt) {
                int framesPlayed = evt.getFrame() + 1;
                synchronized (playerLock) {
                    if (!stopRequested) {
                        currentFrame += framesPlayed;
                        LOGGER.fine("Playback segment finished naturally. Current frame now: " + currentFrame);
                    } else {
                        LOGGER.fine("Playback segment finished due to stop request.");
                    }
                }
            }
            @Override
            public void playbackStarted(PlaybackEvent evt) {
                LOGGER.fine("Playback segment started.");
            }
        };

        while (!stopRequested) {
            int frameToStart = currentFrame;
            if (frameToStart >= totalFrames) {
                if (loop) {
                    LOGGER.info("Reached end, looping back to frame 0.");
                    currentFrame = 0;
                    frameToStart = 0;
                } else {
                    LOGGER.info("Reached end, playback finished.");
                    break;
                }
            }
            AdvancedPlayer localPlayer = null;
            try (FileInputStream fis = new FileInputStream(mp3File)) {
                // Calculate target offset using the recorded frame data.
                long targetOffset = (frameToStart < frameData.size())
                        ? frameData.get(frameToStart).offset
                        : mp3File.length();
                // Clamp offset to 0 if needed.
                targetOffset = Math.max(0, targetOffset);
                long skipped = fis.skip(targetOffset);
                if (skipped != targetOffset) {
                    LOGGER.warning("Could not skip to target offset " + targetOffset + ", skipped " + skipped);
                    break;
                }
                BufferedInputStream bis = new BufferedInputStream(fis);
                localPlayer = new AdvancedPlayer(bis);
                localPlayer.setPlayBackListener(listener);
                synchronized (playerLock) {
                    if (stopRequested) break;
                    player = localPlayer;
                }
                LOGGER.fine("Playing from frame " + frameToStart + " to " + (totalFrames - 1));
                localPlayer.play(frameToStart, totalFrames - 1);
            } catch (JavaLayerException jle) {
                synchronized (playerLock) {
                    if (stopRequested) {
                        LOGGER.fine("Playback interrupted by stop/seek as expected.");
                    } else {
                        LOGGER.log(Level.SEVERE, "Playback error: " + jle.getMessage(), jle);
                        stopRequested = true;
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "File IO error during playback: " + e.getMessage(), e);
                stopRequested = true;
            } finally {
                synchronized (playerLock) {
                    if (localPlayer != null) {
                        localPlayer.close();
                    }
                    if (player == localPlayer) {
                        player = null;
                    }
                }
            }
        }
        LOGGER.info("Playback loop finished.");
        synchronized (playerLock) {
            stopRequested = false;
            paused = false;
            playbackTask = null;
        }
    }

    // Main method for standalone testing.
    public static void main(String[] args) {
        // Use the original file path for testing.
        System.setProperty("java.util.logging.SimpleFormatter.format",
                           "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        File testFile = new File("path/to/your/test.mp3"); // DO NOT change this path!
        if (!testFile.exists()) {
            LOGGER.severe("Error: Test MP3 file not found at " + testFile.getAbsolutePath());
            LOGGER.severe("Please update the path in the main() method.");
            return;
        }
        MP3Player mp3Player = new MP3Player(testFile);
        if (!mp3Player.analyzeFrames()) {
            LOGGER.severe("Exiting due to analysis failure.");
            return;
        }
        try {
            LOGGER.info("--- Starting Playback ---");
            mp3Player.play();
            Thread.sleep(5000);

            LOGGER.info("--- Skipping Forward 15s ---");
            mp3Player.skipForward(15);
            Thread.sleep(5000);

            LOGGER.info("--- Skipping Backward 10s ---");
            mp3Player.skipBackward(10);
            Thread.sleep(5000);

            LOGGER.info("--- Seeking to 30s ---");
            mp3Player.seekToTime(30);
            Thread.sleep(5000);

            LOGGER.info("--- Enabling Loop ---");
            mp3Player.setLoop(true);
            float seekPos = (mp3Player.totalMillis / 1000.0f) - 10.0f;
            if (seekPos < 0) {
                seekPos = 0;
            }
            mp3Player.seekToTime(seekPos);
            LOGGER.info("Waiting to observe looping...");
            Thread.sleep(15000);

            LOGGER.info("--- Stopping Playback ---");
            mp3Player.stop();
            Thread.sleep(1000);

            LOGGER.info("--- Test Finished ---");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("Main thread interrupted.");
        } finally {
            mp3Player.playbackExecutor.shutdownNow();
            LOGGER.info("Playback executor shut down.");
        }
    }
}
