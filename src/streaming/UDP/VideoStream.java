package streaming.UDP;

import java.io.FileInputStream;

public class VideoStream {
    private FileInputStream fis;
    //private int frame_nb;

    public VideoStream(String filename) throws Exception{
        fis = new FileInputStream(filename);
    //    frame_nb = 0;
    }

    public int getNextFrame(byte[] frame) throws Exception{
        int length = 0;
        String lengthString;
        byte[] frameLength = new byte[5];

        fis.read(frameLength, 0, 5);

        lengthString = new String(frameLength);
        length = Integer.parseInt(lengthString);

        return fis.read(frame, 0, length);
    }
}
