// buggy function
    public void writeRaw(String text, int offset, int len) throws IOException
    {
        final char[] buf = _charBuffer;

        // minor optimization: see if we can just get and copy

        // If not, need segmented approach. For speed, let's also use input buffer
        // size that is guaranteed to fit in output buffer; each char can expand to
        // at most 3 bytes, so at most 1/3 of buffer size.

        while (len > 0) {
            int len2 = Math.min(buf.length, len);
            text.getChars(offset, offset+len2, buf, 0);
            writeRaw(buf, 0, len2);
            // If this is NOT the last segment and if the last character looks like
            // split surrogate second half, drop it
            offset += len2;
            len -= len2;
        }
    }

    private final void _writeSegmentedRaw(char[] cbuf, int offset, int len) throws IOException
    {
        final int end = _outputEnd;
        final byte[] bbuf = _outputBuffer;
        final int inputEnd = offset + len;
        
        main_loop:
        while (offset < inputEnd) {
            inner_loop:
            while (true) {
                int ch = (int) cbuf[offset];
                if (ch >= 0x80) {
                    break inner_loop;
                }
                // !!! TODO: fast(er) writes (roll input, output checks in one)
                if (_outputTail >= end) {
                    _flushBuffer();
                }
                bbuf[_outputTail++] = (byte) ch;
                if (++offset >= inputEnd) {
                    break main_loop;
                }
            }
            if ((_outputTail + 3) >= _outputEnd) {
                _flushBuffer();
            }
            char ch = cbuf[offset++];
            if (ch < 0x800) { // 2-byte?
                bbuf[_outputTail++] = (byte) (0xc0 | (ch >> 6));
                bbuf[_outputTail++] = (byte) (0x80 | (ch & 0x3f));
            } else {
                offset = _outputRawMultiByteChar(ch, cbuf, offset, inputEnd);
            }
        }
    }

    private final int _outputRawMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputEnd)
        throws IOException
    {
        // Let's handle surrogates gracefully (as 4 byte output):
        if (ch >= SURR1_FIRST) {
            if (ch <= SURR2_LAST) { // yes, outside of BMP
                // Do we have second part?
                if (inputOffset >= inputEnd || cbuf == null) { // nope... have to note down
                    _reportError("Split surrogate on writeRaw() input (last character)");
                }
                _outputSurrogates(ch, cbuf[inputOffset]);
                return inputOffset+1;
            }
        }
        final byte[] bbuf = _outputBuffer;
        bbuf[_outputTail++] = (byte) (0xe0 | (ch >> 12));
        bbuf[_outputTail++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
        bbuf[_outputTail++] = (byte) (0x80 | (ch & 0x3f));
        return inputOffset;
    }

// trigger testcase
// com/fasterxml/jackson/core/json/RawValueWithSurrogatesTest.java::testRawWithSurrogatesString
public void testRawWithSurrogatesString() throws Exception {
        _testRawWithSurrogatesString(false);
    }
