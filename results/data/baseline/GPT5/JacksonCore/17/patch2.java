private final int _outputRawMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputEnd)
        throws IOException
    {
        // Handle surrogates (as 4 byte output)
        if (ch >= SURR1_FIRST && ch <= SURR2_LAST) {
            // If it's a low surrogate without preceding high surrogate
            if (ch > SURR1_LAST) {
                _reportError("Unmatched surrogate on writeRaw() input (first character)");
            }
            // Need second part
            if (inputOffset >= inputEnd || cbuf == null) {
                _reportError("Split surrogate on writeRaw() input (last character)");
            }
            _outputSurrogates(ch, cbuf[inputOffset]);
            return inputOffset + 1;
        }
        final byte[] bbuf = _outputBuffer;
        bbuf[_outputTail++] = (byte) (0xe0 | (ch >> 12));
        bbuf[_outputTail++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
        bbuf[_outputTail++] = (byte) (0x80 | (ch & 0x3f));
        return inputOffset;
    }