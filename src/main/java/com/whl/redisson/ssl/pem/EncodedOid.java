package com.whl.redisson.ssl.pem;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HexFormat;

public class EncodedOid {
    static final EncodedOid OID_1_2_840_10040_4_1 = EncodedOid.of("2a8648ce380401");
    static final EncodedOid OID_1_2_840_113549_1_1_1 = EncodedOid.of("2A864886F70D010101");
    static final EncodedOid OID_1_2_840_113549_1_1_10 = EncodedOid.of("2a864886f70d01010a");
    static final EncodedOid OID_1_3_101_110 = EncodedOid.of("2b656e");
    static final EncodedOid OID_1_3_101_111 = EncodedOid.of("2b656f");
    static final EncodedOid OID_1_3_101_112 = EncodedOid.of("2b6570");
    static final EncodedOid OID_1_3_101_113 = EncodedOid.of("2b6571");
    static final EncodedOid OID_1_2_840_10045_2_1 = EncodedOid.of("2a8648ce3d0201");
    static final EncodedOid OID_1_3_132_0_34 = EncodedOid.of("2b81040022");

    private final byte[] value;

    private EncodedOid(byte[] value) {
        this.value = value;
    }

    byte[] toByteArray() {
        return this.value.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Arrays.equals(this.value, ((EncodedOid) obj).value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    static EncodedOid of(String hexString) {
        return of(HexFormat.of().parseHex(hexString));
    }

    static EncodedOid of(DerElement derElement) {
        return of(derElement.getContents());
    }

    static EncodedOid of(ByteBuffer byteBuffer) {
        return of(byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.remaining());
    }

    static EncodedOid of(byte[] bytes) {
        return of(bytes, 0, bytes.length);
    }

    static EncodedOid of(byte[] bytes, int off, int len) {
        byte[] value = new byte[len];
        System.arraycopy(bytes, off, value, 0, len);
        return new EncodedOid(value);
    }

}
