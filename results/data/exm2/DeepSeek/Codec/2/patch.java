void encode(byte[] in, int inPos, int inAvail) {
    if (eof) {
        return;
    }
    // inAvail < 0 is how we're informed of EOF in the underlying data we're
    // encoding.
    if (inAvail < 0) {
        eof = true;
        if (buf == null || buf.length - pos < encodeSize) {
            resizeBuf();
        }
        int charsWritten = 0;
        switch (modulus) {
            case 1:
                buf[pos++] = encodeTable[(x >> 2) & MASK_6BITS];
                buf[pos++] = encodeTable[(x << 4) & MASK_6BITS];
                charsWritten = 2;
                // URL-SAFE skips the padding to further reduce size.
                if (encodeTable == STANDARD_ENCODE_TABLE) {
                    buf[pos++] = PAD;
                    buf[pos++] = PAD;
                    charsWritten = 4;
                }
                break;

            case 2:
                buf[pos++] = encodeTable[(x >> 10) & MASK_6BITS];
                buf[pos++] = encodeTable[(x >> 4) & MASK_6BITS];
                buf[pos++] = encodeTable[(x << 2) & MASK_6BITS];
                charsWritten = 3;
                // URL-SAFE skips the padding to further reduce size.
                if (encodeTable == STANDARD_ENCODE_TABLE) {
                    buf[pos++] = PAD;
                    charsWritten = 4;
                }
                break;
        }
        if (charsWritten > 0) {
            currentLinePos += charsWritten;
        }
        if (lineLength > 0 && lineLength <= currentLinePos) {
            System.arraycopy(lineSeparator, 0, buf, pos, lineSeparator.length);
            pos += lineSeparator.length;
            currentLinePos = 0;
        }
    } else {
        for (int i = 0; i < inAvail; i++) {
            if (buf == null || buf.length - pos < encodeSize) {
                resizeBuf();
            }
            modulus = (++modulus) % 3;
            int b = in[inPos++];
            if (b < 0) { b += 256; }
            x = (x << 8) + b;
            if (0 == modulus) {
                buf[pos++] = encodeTable[(x >> 18) & MASK_6BITS];
                buf[pos++] = encodeTable[(x >> 12) & MASK_6BITS];
                buf[pos++] = encodeTable[(x >> 6) & MASK_6BITS];
                buf[pos++] = encodeTable[x & MASK_6BITS];
                currentLinePos += 4;
                if (lineLength > 0 && lineLength <= currentLinePos) {
                    System.arraycopy(lineSeparator, 0, buf, pos, lineSeparator.length);
                    pos += lineSeparator.length;
                    currentLinePos = 0;
                }
            }
        }
    }
}