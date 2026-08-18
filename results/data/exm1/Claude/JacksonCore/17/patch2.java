private final int _outputRawMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputEnd)
    throws IOException
{
    if (ch >= SURR1_FIRST) {
        if (ch <= SURR2_LAST) {
            if (inputOffset >= inputEnd || cbuf == null) {
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