package sk.neuromancer.Xune.sfx;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO8;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO8;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.Util.checkALError;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class Sound {
    private int buffer;

    public Sound(String fileName) {
        try {
            BufferedInputStream buffStream = new BufferedInputStream(new FileInputStream("res/wav/" + fileName));

            buffStream.skip(22); // dojdem pred numChannels(22)
            byte[] numChannelsBA = new byte[2];
            buffStream.read(numChannelsBA);

            byte[] sampleRateBA = new byte[4]; // sampleRate je na 24
            buffStream.read(sampleRateBA);

            buffStream.skip(6); // dojdem pred BitsPerSample(34);
            byte[] bitsPerSampleBA = new byte[2];
            buffStream.read(bitsPerSampleBA);

            buffStream.skip(4); // dojdem pred dataSize(40)
            byte[] dataSizeBA = new byte[4];
            buffStream.read(dataSizeBA);

            ByteBuffer convertor = ByteBuffer.wrap(numChannelsBA);
            convertor.order(ByteOrder.LITTLE_ENDIAN);
            int numChannels = convertor.getShort();

            convertor = ByteBuffer.wrap(sampleRateBA);
            convertor.order(ByteOrder.LITTLE_ENDIAN);
            int sampleRate = convertor.getInt();

            convertor = ByteBuffer.wrap(bitsPerSampleBA);
            convertor.order(ByteOrder.LITTLE_ENDIAN);
            int bitsPerSample = convertor.getShort();

            convertor = ByteBuffer.wrap(dataSizeBA);
            convertor.order(ByteOrder.LITTLE_ENDIAN);
            int dataSize = convertor.getInt();

            byte[] data = new byte[dataSize];
            buffStream.read(data);

            buffStream.close();

            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(dataSize);
            dataBuffer.order(ByteOrder.nativeOrder());
            ByteBuffer srcDataBuffer = ByteBuffer.wrap(data);
            srcDataBuffer.order(ByteOrder.LITTLE_ENDIAN);

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


            this.buffer = alGenBuffers();
            checkALError();

            alBufferData(this.buffer, format, dataBuffer, sampleRate);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getBuffer() {
        return buffer;
    }

    public void destroy() {
        alDeleteBuffers(buffer);
    }
}
