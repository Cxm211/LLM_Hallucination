private JsonToken _parserNumber2(char[] outBuf, int outPtr, boolean negative,
            int intPartLength)
        throws IOException, JsonParseException
    {
        while (true) {
            if (_inputPtr >= _inputEnd && !loadMore()) {
                _textBuffer.setCurrentLength(outPtr);
                return resetInt(negative, intPartLength);
            }
            int c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            if (c > INT_9 || c < INT_0) {
                if (c == '.' || c == 'e' || c == 'E') {
                    return _parseFloat(outBuf, outPtr, c, negative, intPartLength);
                }
                break;
            }
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
            ++intPartLength;
        }
        --_inputPtr;
        _textBuffer.setCurrentLength(outPtr);
        return resetInt(negative, intPartLength);
        
    }