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
        if ((_outputTail + 4) >= _outputEnd) {
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