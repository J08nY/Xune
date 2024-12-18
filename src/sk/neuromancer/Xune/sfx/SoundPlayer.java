package sk.neuromancer.Xune.sfx;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import static org.lwjgl.openal.AL10.*;

public class SoundPlayer {

    public enum SoundPlayerState {
        INITIAL, PLAYING, PAUSED, STOPPED;
    }

    private Sound sound;
    private int source;

    private FloatBuffer sourcePosition;
    private FloatBuffer sourceVelocity;
    private float pitch;
    private float gain;

    private SoundPlayerState state;

    public static final float[] ZERO_SOURCE_POSITION = new float[]{0.0f, 0.0f, 0.0f};
    public static final float[] ZERO_SOURCE_VELOCITY = new float[]{0.0f, 0.0f, 0.0f};
    public static final float DEFAULT_GAIN = 1.0f;
    public static final float DEFAULT_PITCH = 1.0f;

    public SoundPlayer(Sound sound) {
        this(sound, DEFAULT_PITCH, DEFAULT_GAIN, ZERO_SOURCE_POSITION, ZERO_SOURCE_VELOCITY);
    }

    public SoundPlayer(Sound sound, float pitch, float gain, float[] position, float[] velocity) {
        this.sound = sound;
        this.state = SoundPlayerState.INITIAL;

        this.pitch = pitch;
        this.gain = gain;

        FloatBuffer pos = BufferUtils.createFloatBuffer(3).put(position);
        pos.rewind();
        this.sourcePosition = pos;

        FloatBuffer vel = BufferUtils.createFloatBuffer(3).put(velocity);
        vel.rewind();
        this.sourceVelocity = vel;

        this.source = alGenSources();

        alSourcei(this.source, AL_BUFFER, sound.getBuffer());
        alSourcef(this.source, AL_PITCH, this.pitch);
        alSourcef(this.source, AL_GAIN, this.gain);
        alSourcefv(this.source, AL_POSITION, this.sourcePosition);
        alSourcefv(this.source, AL_VELOCITY, this.sourceVelocity);
    }

    public void play() {
        state = SoundPlayerState.PLAYING;
        alSourcePlay(source);
    }

    public void pause() {
        state = SoundPlayerState.PAUSED;
        alSourcePause(source);
    }

    public void stop() {
        state = SoundPlayerState.STOPPED;
        alSourceStop(source);
    }

    public SoundPlayerState getState() {
        switch (alGetSourcei(source, AL_SOURCE_STATE)) {
            case AL_PLAYING -> state = SoundPlayerState.PLAYING;
            case AL_STOPPED -> state = SoundPlayerState.STOPPED;
            case AL_PAUSED -> state = SoundPlayerState.PAUSED;
        }
        return state;
    }

    public void destroy() {
        alDeleteSources(source);
    }

}
