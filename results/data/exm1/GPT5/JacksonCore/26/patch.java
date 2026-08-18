public void feedInput(byte[] buf, int start, int end) throws IOException
    {
        if (_inputPtr < _inputEnd) {
            _reportError("Still have %d undecoded bytes, should not call 'feedInput'", _inputEnd - _inputPtr);
        }
        if (buf == null) {
            _reportError("Null input buffer");
        }
        if (start < 0 || end < start || end > buf.length) {
            _reportError("Input end (%d) may not be before start (%d)", end, start);
        }
        if (_endOfInput) {
            _reportError("Already closed, can not feed more input");
        }
        _currInputProcessed += _origBufferLen;
        _currInputRowStart = start - (_inputEnd - _currInputRowStart);
        _inputBuffer = buf;
        _inputPtr = start;
        _inputEnd = end;
        _origBufferLen = end - start;
    }