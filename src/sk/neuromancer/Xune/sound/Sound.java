package sk.neuromancer.Xune.sound;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.util.opus.OpusFile.*;

public abstract class Sound {
    protected int buffer;
    private final String fileName;

    Sound(String fileName) {
        this.fileName = fileName;
    }

    private static int getALFormat(int numChannels, int bitsPerSample) {
        int format = -1;
        if (numChannels == 1) {
            if (bitsPerSample == 16) {
                format = AL_FORMAT_MONO16;
            } else if (bitsPerSample == 8) {
                format = AL_FORMAT_MONO8;
            }
        } else if (numChannels == 2) {
            if (bitsPerSample == 16) {
                format = AL_FORMAT_STEREO16;
            } else if (bitsPerSample == 8) {
                format = AL_FORMAT_STEREO8;
            }
        }
        return format;
    }

    public int getBuffer() {
        return buffer;
    }

    public void destroy() {
        alDeleteBuffers(buffer);
    }

    public static Sound create(String fileName) {
        if (fileName.endsWith(".wav")) {
            return new Wav(fileName);
        } else if (fileName.endsWith(".opus")) {
            return new Opus(fileName);
        } else {
            throw new IllegalArgumentException("Unsupported sound file format.");
        }
    }

    public static class Wav extends Sound {

        public Wav(String fileName) {
            super(fileName);

            try (InputStream stream = getClass().getResourceAsStream("/sk/neuromancer/Xune/aud/" + fileName)) {
                assert stream != null;
                BufferedInputStream buffStream = new BufferedInputStream(stream);
                byte[] fileType = new byte[4];
                buffStream.read(fileType);
                if (fileType[0] != 'R' || fileType[1] != 'I' || fileType[2] != 'F' || fileType[3] != 'F') {
                    throw new IOException("Not a valid WAV file.");
                }
                buffStream.skip(4);
                byte[] format = new byte[4];
                buffStream.read(format);
                if (format[0] != 'W' || format[1] != 'A' || format[2] != 'V' || format[3] != 'E') {
                    throw new IOException("Not a valid WAV file.");
                }

                buffStream.skip(10);
                byte[] numChannelsBA = new byte[2];
                buffStream.read(numChannelsBA);

                byte[] sampleRateBA = new byte[4];
                buffStream.read(sampleRateBA);

                buffStream.skip(6);
                byte[] bitsPerSampleBA = new byte[2];
                buffStream.read(bitsPerSampleBA);

                while (true) {
                    byte[] chunkID = new byte[4];
                    buffStream.read(chunkID);
                    byte[] chunkSizeBA = new byte[4];
                    buffStream.read(chunkSizeBA);

                    ByteBuffer convertor = ByteBuffer.wrap(chunkSizeBA);
                    convertor.order(ByteOrder.LITTLE_ENDIAN);
                    int chunkSize = convertor.getInt();

                    if (chunkID[0] == 'd' && chunkID[1] == 'a' && chunkID[2] == 't' && chunkID[3] == 'a') {
                        byte[] data = new byte[chunkSize];
                        buffStream.read(data);

                        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(chunkSize);
                        dataBuffer.order(ByteOrder.nativeOrder());
                        ByteBuffer srcDataBuffer = ByteBuffer.wrap(data);
                        srcDataBuffer.order(ByteOrder.LITTLE_ENDIAN);

                        int numChannels = ByteBuffer.wrap(numChannelsBA).order(ByteOrder.LITTLE_ENDIAN).getShort();
                        int sampleRate = ByteBuffer.wrap(sampleRateBA).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        int bitsPerSample = ByteBuffer.wrap(bitsPerSampleBA).order(ByteOrder.LITTLE_ENDIAN).getShort();

                        if (bitsPerSample == 16) {
                            ShortBuffer dataShort = dataBuffer.asShortBuffer();
                            ShortBuffer srcShort = srcDataBuffer.asShortBuffer();
                            while (srcShort.hasRemaining())
                                dataShort.put(srcShort.get());
                        } else if (bitsPerSample == 8) {
                            while (srcDataBuffer.hasRemaining())
                                dataBuffer.put(srcDataBuffer.get());
                        }
                        dataBuffer.rewind();

                        this.buffer = alGenBuffers();
                        alBufferData(this.buffer, getALFormat(numChannels, bitsPerSample), dataBuffer, sampleRate);
                        break;
                    } else {
                        buffStream.skip(chunkSize);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Opus extends Sound {

        public Opus(String fileName) {
            super(fileName);

            try (MemoryStack stack = MemoryStack.stackPush();
                 InputStream stream = getClass().getResourceAsStream("/sk/neuromancer/Xune/aud/" + fileName)) {
                assert stream != null;
                ByteBuffer buffer = MemoryUtil.memAlloc(stream.available());
                Channels.newChannel(stream).read(buffer);
                buffer.flip();

                IntBuffer eOut = stack.mallocInt(1);
                long opusFile = op_open_memory(buffer, eOut);
                if (opusFile == NULL) {
                    throw new RuntimeException("Failed to open Opus file");
                }
                int e = eOut.get();
                if (e != 0) {
                    throw new RuntimeException("Failed to open Opus file: " + e);
                }

                int channels = op_channel_count(opusFile, -1);
                long totalSamples = op_pcm_total(opusFile, -1);
                if (totalSamples < 0) {
                    throw new RuntimeException("Failed to get total samples");
                }

                ShortBuffer pcm = memAllocShort((int) totalSamples * 2);
                int samples;
                int totalDecodedSamples = 0;
                while ((samples = op_read_stereo(opusFile, pcm)) > 0) {
                    pcm.position(pcm.position() + samples * 2);
                    totalDecodedSamples += samples;
                }
                if (samples < 0) {
                    throw new RuntimeException("Failed to decode Opus data: " + samples);
                }

                pcm.flip();

                if (totalDecodedSamples != totalSamples) {
                    throw new RuntimeException("Failed to decode all samples: " + totalDecodedSamples + " != " + totalSamples);
                }

                this.buffer = alGenBuffers();
                if (alGetError() != AL_NO_ERROR) {
                    throw new RuntimeException("Failed to generate OpenAL buffer");
                }

                alBufferData(this.buffer, getALFormat(2, 16), pcm, 48000);
                if (alGetError() != AL_NO_ERROR) {
                    throw new RuntimeException("Failed to buffer OpenAL data");
                }

                op_free(opusFile);
                memFree(pcm);
                memFree(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
