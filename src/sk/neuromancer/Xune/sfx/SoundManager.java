package sk.neuromancer.Xune.sfx;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.sfx.SoundPlayer.SoundPlayerState;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundManager implements Tickable {
    private Game game;
    private long device;
    private long context;

    private Queue<SoundPlayer> players = new LinkedList<>();
    private Sound[] sounds;

    private static final String[] soundNames = new String[]{
            "blip_1.wav",
            "explosion_1.wav",
            "explosion_2.wav",
            "explosion_3.wav",
            "hit_1.wav",
            "hit_2.wav",
            "hit_3.wav",
            "laser_1.wav",
            "long_explosion_1.wav",
            "shot_1.wav",
            "tada_1.wav",
            "duneshifter.wav"
    };

    public static final int SOUND_BLIP_1 = 0;
    public static final int SOUND_EXPLOSION_1 = 1;
    public static final int SOUND_EXPLOSION_2 = 2;
    public static final int SOUND_EXPLOSION_3 = 3;
    public static final int SOUND_HIT_1 = 4;
    public static final int SOUND_HIT_2 = 5;
    public static final int SOUND_HIT_3 = 6;
    public static final int SOUND_LASER_1 = 7;
    public static final int SOUND_LONG_EXPLOSION_1 = 8;
    public static final int SOUND_SHOT_1 = 9;
    public static final int SOUND_TADA_1 = 10;

    public static final int TRACK_DUNESHIFTER = 11;

    public SoundManager(Game game) {//TODO nejako manazovat Gain..
        this.game = game;
        this.device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        this.context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        this.sounds = new Sound[SoundManager.soundNames.length];
        for (int i = 0; i < this.sounds.length; i++) {
            this.sounds[i] = new Sound(SoundManager.soundNames[i]);
        }
    }

    @Override
    public void tick(int tickCount) {
        //TODO: This is no longer valid as they can loop.
        SoundPlayer head = players.peek();
        if (head != null && head.getState() == SoundPlayerState.STOPPED) {
            players.remove();
            head.destroy();
        }
    }

    public SoundPlayer play(int soundIndex, boolean loop) {
        SoundPlayer player = new SoundPlayer(this.sounds[soundIndex], loop);
        players.add(player);
        player.play();
        return player;
    }

    public void quit() {
        for (SoundPlayer p : players) {
            p.destroy();
        }
        for (Sound s : sounds) {
            s.destroy();
        }
    }

}
