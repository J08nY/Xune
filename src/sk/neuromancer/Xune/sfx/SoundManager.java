package sk.neuromancer.Xune.sfx;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.sfx.SoundPlayer.SoundPlayerState;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.openal.AL10.AL_INVERSE_DISTANCE_CLAMPED;
import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundManager implements Tickable {
    private final Game game;
    private final long device;
    private final long context;

    private final List<SoundPlayer> players = new LinkedList<>();
    private final Sound[] sounds;

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
            "shot_2.wav",
            "tada_1.wav",
            "wilhelm.wav",
            "worm_death.wav",
            "worm_kill.wav",
            "duneshifter.wav"
    };

    public static final int SOUND_NONE = -1;
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
    public static final int SOUND_SHOT_2 = 10;
    public static final int SOUND_TADA_1 = 11;
    public static final int SOUND_WILHELM = 12;
    public static final int SOUND_WORM_DEATH = 13;
    public static final int SOUND_WORM_KILL = 14;

    public static final int TRACK_DUNESHIFTER = 15;

    private static SoundManager instance;

    public SoundManager(Game game) {
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

        alDistanceModel(AL_INVERSE_DISTANCE_CLAMPED);

        this.sounds = new Sound[SoundManager.soundNames.length];
        for (int i = 0; i < this.sounds.length; i++) {
            this.sounds[i] = new Sound(SoundManager.soundNames[i]);
        }
        instance = this;
    }

    @Override
    public void tick(int tickCount) {
        List<SoundPlayer> stopped = players.stream().filter(p -> p.getState() == SoundPlayerState.STOPPED).toList();
        stopped.forEach(SoundPlayer::destroy);
        players.removeAll(stopped);
    }

    public static SoundPlayer play(int soundIndex, boolean loop, float gain) {
        if (soundIndex == -1) {
            return null;
        }
        SoundPlayer player = new SoundPlayer(instance.sounds[soundIndex], loop, gain);
        instance.players.add(player);
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
        alcDestroyContext(context);
        alcCloseDevice(device);
    }

}
