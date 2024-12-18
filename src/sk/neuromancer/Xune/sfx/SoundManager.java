package sk.neuromancer.Xune.sfx;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.openal.ALContext;

import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.sfx.SoundPlayer.SoundPlayerState;

public class SoundManager implements Tickable {
    private Game game;
    private ALContext context;

    private List<SoundPlayer> players = new ArrayList<SoundPlayer>();
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
            "tada_1.wav"
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

    public SoundManager(Game game) {//TODO nejako manazovat Gain..
        this.game = game;
        this.context = ALContext.create();
        this.context.makeCurrent();

        this.sounds = new Sound[SoundManager.soundNames.length];
        for (int i = 0; i < this.sounds.length; i++) {
            this.sounds[i] = new Sound(SoundManager.soundNames[i]);
        }
    }

    @Override
    public void tick(int tickCount) {
        for (int i = 0; i < players.size(); i++) {
            SoundPlayer player = players.get(i);
            if (player.getState() == SoundPlayerState.STOPPED) {
                players.remove(i);
                player.destroy();
            }
        }
    }

    public void play(int soundIndex) {
        SoundPlayer player = new SoundPlayer(this.sounds[soundIndex]);
        players.add(player);
        player.play();
    }

    public void quit() {
        for (SoundPlayer p : players) {
            p.destroy();
        }
        for (Sound s : sounds) {
            s.destroy();
        }
        context.getDevice().destroy();
        context.destroy();
    }

}
