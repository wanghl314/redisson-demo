package com.whl.redisson.ssl.pem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DerEncoder {
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    void objectIdentifier(EncodedOid encodedOid) throws IOException {
        int code = (encodedOid != null) ? 0x06 : 0x05;
        codeLengthBytes(code, (encodedOid != null) ? encodedOid.toByteArray() : null);
    }

    void integer(int... encodedInteger) throws IOException {
        codeLengthBytes(0x02, bytes(encodedInteger));
    }

    void octetString(byte[] bytes) throws IOException {
        codeLengthBytes(0x04, bytes);
    }

    void sequence(byte[] bytes) throws IOException {
        codeLengthBytes(0x30, bytes);
    }

    void codeLengthBytes(int code, byte[] bytes) throws IOException {
        this.stream.write(code);
        int length = (bytes != null) ? bytes.length : 0;
        if (length <= 127) {
            this.stream.write(length & 0xFF);
        }
        else {
            ByteArrayOutputStream lengthStream = new ByteArrayOutputStream();
            while (length != 0) {
                lengthStream.write(length & 0xFF);
                length = length >> 8;
            }
            byte[] lengthBytes = lengthStream.toByteArray();
            this.stream.write(0x80 | lengthBytes.length);
            for (int i = lengthBytes.length - 1; i >= 0; i--) {
                this.stream.write(lengthBytes[i]);
            }
        }
        if (bytes != null) {
            this.stream.write(bytes);
        }
    }

    private static byte[] bytes(int... elements) {
        if (elements == null) {
            return null;
        }
        byte[] result = new byte[elements.length];
        for (int i = 0; i < elements.length; i++) {
            result[i] = (byte) elements[i];
        }
        return result;
    }

    byte[] toSequence() throws IOException {
        DerEncoder sequenceEncoder = new DerEncoder();
        sequenceEncoder.sequence(toByteArray());
        return sequenceEncoder.toByteArray();
    }

    byte[] toByteArray() {
        return this.stream.toByteArray();
    }

}
