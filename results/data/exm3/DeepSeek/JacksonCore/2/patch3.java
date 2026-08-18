    protected JsonToken _parseNumber(int c)
        throws IOException, JsonParseException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;
        boolean negative = (c == INT_MINUS);

        // Need to prepend sign?
        if (negative) {
            outBuf[outPtr++] = '-';            // Must have something after sign too
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            // Note: must be followed by a digit
            if (c < INT_0 || c > INT_9) {
                return _handleInvalidNumberStart(c, true);
            }
        }

        // One special case: if first char is 0, must not be followed by a digit
        int intLen;
        if (c == INT_0) {
            // Add the zero to buffer
            outBuf[outPtr++] = (char) c;
            intLen = 1;
            c = _verifyNoLeadingZeroes();
            // c now holds the next character after zero.
            // If c is a digit, error would have been thrown.
            // Now handle this character:
            if (c == '.' || c == 'e' || c == 'E') {
                return _parseFloat(outBuf, outPtr, c, negative, intLen);
            } else {
                // Not a digit, not '.' or exponent, so number ends with zero.
                // Need to push back the character we read.
                --_inputPtr; // because _verifyNoLeadingZeroes advanced _inputPtr
                _textBuffer.setCurrentLength(outPtr);
                return resetInt(negative, intLen);
            }
        } else {
            // Not a leading zero; add the first digit
            outBuf[outPtr++] = (char) c;
            intLen = 1;
        }

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
        
        --_inputPtr; // to push back trailing char (comma etc)
        _textBuffer.setCurrentLength(outPtr);
        // As per #105, need separating space between root values; check here

        // And there we have it!
        return resetInt(negative, intLen);
    }