import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundEngine {

    public void playShoot() {
        playTone(880, 70, 0.2);
    }

    public void playHit() {
        playTone(220, 100, 0.35);
    }

    public void playLevelUp() {
        playTone(520, 110, 0.25);
        playTone(760, 110, 0.25);
    }

    public void playGameOver() {
        playTone(300, 200, 0.35);
    }

    public void playVictory() {
        playTone(660, 100, 0.25);
        playTone(880, 100, 0.25);
        playTone(1100, 120, 0.25);
    }

    private void playTone(int hz, int msecs, double volume) {
        Thread audioThread = new Thread(() -> {
            try {
                byte[] buf = new byte[msecs * 8];
                for (int i = 0; i < buf.length; i++) {
                    double angle = i / (44100.0 / hz) * 2.0 * Math.PI;
                    buf[i] = (byte) (Math.sin(angle) * 127.0 * volume);
                }

                AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
                Clip clip = AudioSystem.getClip();
                clip.open(format, buf, 0, buf.length);
                clip.start();
            } catch (Exception ignored) {
                // Silent fallback if audio output is unavailable.
            }
        }, "sound-engine-thread");
        audioThread.setDaemon(true);
        audioThread.start();
    }
}
