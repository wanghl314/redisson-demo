package com.whl.redisson.ssl.pem;

import java.nio.ByteBuffer;

public class DerElement {
    private final ValueType valueType;

    private final long tagType;

    private final ByteBuffer contents;

    private DerElement(ByteBuffer bytes) {
        byte b = bytes.get();
        this.valueType = ((b & 0x20) == 0) ? ValueType.PRIMITIVE : ValueType.ENCODED;
        this.tagType = decodeTagType(b, bytes);
        int length = decodeLength(bytes);
        bytes.limit(bytes.position() + length);
        this.contents = bytes.slice();
        bytes.limit(bytes.capacity());
        bytes.position(bytes.position() + length);
    }

    private long decodeTagType(byte b, ByteBuffer bytes) {
        long tagType = (b & 0x1F);
        if (tagType != 0x1F) {
            return tagType;
        }
        tagType = 0;
        b = bytes.get();
        while ((b & 0x80) != 0) {
            tagType <<= 7;
            tagType = tagType | (b & 0x7F);
            b = bytes.get();
        }
        return tagType;
    }

    private int decodeLength(ByteBuffer bytes) {
        byte b = bytes.get();
        if ((b & 0x80) == 0) {
            return b & 0x7F;
        }
        int numberOfLengthBytes = (b & 0x7F);
        assert numberOfLengthBytes != 0 : "Infinite length encoding is not supported";
        assert numberOfLengthBytes != 0x7F : "Reserved length encoding is not supported";
        assert numberOfLengthBytes <= 4 : "Length overflow";
        int length = 0;
        for (int i = 0; i < numberOfLengthBytes; i++) {
            length <<= 8;
            length |= (bytes.get() & 0xFF);
        }
        return length;
    }

    boolean isType(ValueType valueType) {
        return this.valueType == valueType;
    }

    boolean isType(ValueType valueType, TagType tagType) {
        return this.valueType == valueType && this.tagType == tagType.getNumber();
    }

    ByteBuffer getContents() {
        return this.contents;
    }

    static DerElement of(byte[] bytes) {
        return of(ByteBuffer.wrap(bytes));
    }

    static DerElement of(ByteBuffer bytes) {
        return (bytes.remaining() > 0) ? new DerElement(bytes) : null;
    }

    enum ValueType {

        PRIMITIVE, ENCODED

    }

    enum TagType {

        INTEGER(0x02), OCTET_STRING(0x04), OBJECT_IDENTIFIER(0x06), SEQUENCE(0x10);

        private final int number;

        TagType(int number) {
            this.number = number;
        }

        int getNumber() {
            return this.number;
        }

    }

}
