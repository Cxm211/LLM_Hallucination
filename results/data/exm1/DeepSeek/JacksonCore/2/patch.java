    protected JsonToken _parseNumber(int ch) throws IOException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;
        boolean negative = (ch == INT_MINUS);

        // Need to prepend sign?
        if (negative) {
            outBuf[outPtr++] = '-';
            // Must have something after sign too
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            ch = (int) _inputBuffer[_inputPtr++] & 0xFF;
            // Note: must be followed by a digit
            if (ch < INT_0 || ch > INT_9) {
                return _handleInvalidNumberStart(ch, true);
            }
        }

        // One special case: if first char is 0, must not be followed by a digit
        if (ch == INT_0) {
            ch = _verifyNoLeadingZeroes();
        }
        
        // Ok: we can first just add digit we saw first:
        outBuf[outPtr++] = (char) ch;
        int intLen = 1;

        // And then figure out how far we can read without further checks:
        int end = _inputPtr + outBuf.length;
        if (end > _inputEnd) {
            end = _inputEnd;
        }

        // With this, we have a nice and tight loop:
        while (true) {
            if (_inputPtr >= end) {
                // Long enough to be split across boundary, so:
                return _parserNumber2(outBuf, outPtr, negative, intLen);
            }
            ch = (int) _inputBuffer[_inputPtr++] & 0xFF;
            if (ch < INT_0 || ch > INT_9) {
                break;
            }
            ++intLen;
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) ch;
        }
        if (ch == '.' || ch == 'e' || ch == 'E') {
            return _parseFloat(outBuf, outPtr, ch, negative, intLen);
        }
        
        --_inputPtr; // to push back trailing char (comma etc)
        _textBuffer.setCurrentLength(outPtr);
        // As per #105, need separating space between root values; check here

        // And there we have it!
        return resetInt(negative, intLen);
    }