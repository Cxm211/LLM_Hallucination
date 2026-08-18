public void feedInput(byte[] buf, int start, int len) throws IOException
    {
        // Must not have remaining input
        if (_inputPtr < _inputEnd) {
            _reportError("Still have %d undecoded bytes, should not call 'feedInput'", _inputEnd - _inputPtr);
        }
        if (start < 0) {
            _reportError("Input start (%d) may not be negative", start);
        }
        if (len < 0) {
            _reportError("Input length (%d) may not be negative", len);
        }
        // and shouldn't have been marked as end-of-input
        if (_endOfInput) {
            _reportError("Already closed, can not feed more input");
        }
        // Time to update pointers first
        _currInputProcessed += _origBufferLen;

        // Also need to adjust row start, to work as if it extended into the past wrt new buffer
        _currInputRowStart = start - (_inputEnd - _currInputRowStart);

        // And then update buffer settings
        _inputBuffer = buf;
        _inputPtr = start;
        _inputEnd = start + len;
        _origBufferLen = len;
    }