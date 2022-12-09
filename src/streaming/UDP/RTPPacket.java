package streaming.UDP;

public class RTPPacket {
    private int HEADER_SIZE = 12;

    private int version;
    private int padding;
    private int extension;
    private int cc;
    private int marker;
    private int payloadType;
    private int sequenceNr;
    private int timestamp;
    private int ssrc;

    private byte[] header;
    private int payload_size;
    private byte[] payload;


    public RTPPacket(int pType, int frameNb, int time, byte[] data, int data_length){
        // fill by default header fields
        version = 2;
        padding = 0;
        extension = 0;
        cc = 0;
        marker = 0;
        ssrc = 0;
    
        // fill changing header fields
        sequenceNr = frameNb;
        timestamp = time;
        payloadType = pType;
    
        header = new byte[HEADER_SIZE];
    
        header[0] = (byte)(version << 6 | padding << 5 | extension << 4 | cc);
        header[1] = (byte)(marker << 7 | payloadType & 0x000000FF);
        header[2] = (byte)(sequenceNr >> 8);
        header[3] = (byte)(sequenceNr & 0xFF);
        header[4] = (byte)(timestamp >> 24);
        header[5] = (byte)(timestamp >> 16);
        header[6] = (byte)(timestamp >> 8);
        header[7] = (byte)(timestamp & 0xFF);
        header[8] = (byte)(ssrc >> 24);
        header[9] = (byte)(ssrc >> 16);
        header[10] = (byte)(ssrc >> 8);
        header[11] = (byte)(ssrc & 0xFF);

        payload_size = data_length;
        payload = new byte[data_length];

        for (int i=0; i < data_length; i++)
            payload[i] = data[i];
    }

    public RTPPacket(byte[] packet, int packet_size){
        // fill default fields
        version = 2;
        padding = 0;
        extension = 0;
        cc = 0;
        marker = 0;
        ssrc = 0;

        if (packet_size >= HEADER_SIZE){
            header = new byte[HEADER_SIZE];
            for (int i=0; i < HEADER_SIZE; i++)
                header[i] = packet[i];
                
            payload_size = packet_size - HEADER_SIZE;
            payload = new byte[payload_size];
            for (int i=HEADER_SIZE; i < packet_size; i++)
                payload[i-HEADER_SIZE] = packet[i];

            payloadType = header[1] & 127;
            sequenceNr = unsigned_int(header[3]) + 256*unsigned_int(header[2]);
            timestamp = unsigned_int(header[7]) + 256*unsigned_int(header[6]) + 65536*unsigned_int(header[5]) + 16777216*unsigned_int(header[4]);
        }
    }


    public int getpacket(byte[] packet){
        for (int i=0; i < HEADER_SIZE; i++)
            packet[i] = header[i];
        for (int i=0; i < payload_size; i++)
            packet[i+HEADER_SIZE] = payload[i];
    
        return payload_size + HEADER_SIZE;
    }


    public int getpayload(byte[] data) {
        for (int i=0; i < payload_size; i++)
            data[i] = payload[i];
        
        return payload_size;
    }

    public int getpayload_length() {
        return payload_size;
    }

    public int getlength() {
        return payload_size + HEADER_SIZE;
    }

    public int gettimestamp() {
        return timestamp;
    }

    public int getsequencenumber() {
        return sequenceNr;
    }

    public int getpayloadtype() {
        return payloadType;
    }

    public void printheader(){
        System.out.print("[RTP-Header] ");
        System.out.println("Version: " + version
                           + ", Padding: " + padding
                           + ", Extension: " + extension
                           + ", CC: " + cc
                           + ", Marker: " + marker
                           + ", PayloadType: " + payloadType
                           + ", SequenceNumber: " + sequenceNr
                           + ", TimeStamp: " + timestamp);
    }

    public int unsigned_int(int nb) {
        if (nb >= 0)
            return(nb);
        else
            return(256+nb);
    }
}
