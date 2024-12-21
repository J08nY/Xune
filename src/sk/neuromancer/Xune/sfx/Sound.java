package sk.neuromancer.Xune.sfx;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

public class Sound {
    private int buffer;

    public Sound(String fileName) {
        try {

            BufferedInputStream buffStream = new BufferedInputStream(getClass().getResourceAsStream("/sk/neuromancer/Xune/wav/" + fileName));
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
                    alBufferData(this.buffer, getFormat(numChannels, bitsPerSample), dataBuffer, sampleRate);
                    break;
                } else {
                    buffStream.skip(chunkSize);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getFormat(int numChannels, int bitsPerSample) {
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
}
