private JsonToken _parseNumber2(boolean negative) throws IOException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;

        if (negative) {
            outBuf[outPtr++] = '-';
        }

        int intLen = 0;
        char c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : getNextChar("No digit following minus sign");
        if (c == '0') {
            c = _verifyNoLeadingZeroes();
        }
        boolean eof = false;

        int_loop:
        while (c >= '0' && c <= '9') {
            ++intLen;
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            if (_inputPtr >= _inputEnd && !loadMore()) {
                c = CHAR_NULL;
                eof = true;
                break int_loop;
            }
            c = _inputBuffer[_inputPtr++];
        }
        if (intLen == 0) {
            reportInvalidNumber("Missing integer part (next char "+_getCharDesc(c)+")");
        }

        int fractLen = 0;
        if (c == '.') {
            outBuf[outPtr++] = c;

            fract_loop:
            while (true) {
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break fract_loop;
                }
                c = _inputBuffer[_inputPtr++];
                if (c < INT_0 || c > INT_9) {
                    break fract_loop;
                }
                ++fractLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
            }
            if (fractLen == 0) {
                reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
            }
        }

        int expLen = 0;
        if (c == 'e' || c == 'E') {
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++]
                : getNextChar("expected a digit for number exponent");
            if (c == '-' || c == '+') {
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++]
                    : getNextChar("expected a digit for number exponent");
            }

            exp_loop:
            while (c <= INT_9 && c >= INT_0) {
                ++expLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break exp_loop;
                }
                c = _inputBuffer[_inputPtr++];
            }
            if (expLen == 0) {
                reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
            }
        }

        if (!eof) {
            --_inputPtr;
        }
        _textBuffer.setCurrentLength(outPtr);
        return reset(negative, intLen, fractLen, expLen);
    }