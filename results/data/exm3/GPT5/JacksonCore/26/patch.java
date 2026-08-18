public void feedInput(byte[] buf, int start, int end) throws IOException
    {
        // Must not have remaining input
        if (_inputPtr < _inputEnd) {
            _reportError("Still have %d undecoded bytes, should not call 'feedInput'", _inputEnd - _inputPtr);
        }
        if (end < start) {
            _reportError("Input end (%d) may not be before start (%d)", end, start);
        }
        // and shouldn't have been marked as end-of-input
        if (_endOfInput) {
            _reportError("Already closed, can not feed more input");
        }
        // Time to update pointers first
        _currInputProcessed += _origBufferLen;

        // Adjust row start as if input continued from previous buffer end
        _currInputRowStart = _currInputRowStart - _inputEnd;

        // And then update buffer settings; use indices relative to the provided slice
        _inputBuffer = buf;
        _inputPtr = 0;
        _inputEnd = end - start;
        _origBufferLen = _inputEnd;
    }