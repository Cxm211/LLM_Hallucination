protected JsonToken _parseNumber(int c)
        throws IOException, JsonParseException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;
        boolean negative = (c == INT_MINUS);

        if (negative) {
            outBuf[outPtr++] = '-';
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            if (c < INT_0 || c > INT_9) {
                return _handleInvalidNumberStart(c, true);
            }
        }

        if (c == INT_0) {
            c = _verifyNoLeadingZeroes();
        }
        
        outBuf[outPtr++] = (char) c;
        int intLen = 1;

        int end = _inputPtr + outBuf.length;
        if (end > _inputEnd) {
            end = _inputEnd;
        }

        while (true) {
            if (_inputPtr >= end) {
                return _parserNumber2(outBuf, outPtr, negative, intLen);
            }
            c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            if (c < INT_0 || c > INT_9) {
                break;
            }
            ++intLen;
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
        }
        if (c == '.' || c == 'e' || c == 'E') {
            return _parseFloat(outBuf, outPtr, c, negative, intLen);
        }
        
        --_inputPtr;
        _textBuffer.setCurrentLength(outPtr);
        return resetInt(negative, intLen);
    }