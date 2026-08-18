protected JsonToken _parseNumber(int ch) throws IOException
    {
        boolean negative = (ch == INT_MINUS);
        int ptr = _inputPtr;
        int startPtr = ptr-1;
        final int inputLen = _inputEnd;

        dummy_loop:
        do {
            if (negative) {
                if (ptr >= _inputEnd) {
                    break dummy_loop;
                }
                ch = _inputBuffer[ptr++];
                if (ch > INT_9 || ch < INT_0) {
                    _inputPtr = ptr;
                    return _handleInvalidNumberStart(ch, true);
                }
            }
            if (ch == INT_0) {
                break dummy_loop;
            }
            
            int intLen = 1;
            
            int_loop:
            while (true) {
                if (ptr >= _inputEnd) {
                    break dummy_loop;
                }
                ch = (int) _inputBuffer[ptr++];
                if (ch < INT_0 || ch > INT_9) {
                    break int_loop;
                }
                ++intLen;
            }

            int fractLen = 0;
            
            if (ch == '.') {
                fract_loop:
                while (true) {
                    if (ptr >= inputLen) {
                        break dummy_loop;
                    }
                    ch = (int) _inputBuffer[ptr++];
                    if (ch < INT_0 || ch > INT_9) {
                        break fract_loop;
                    }
                    ++fractLen;
                }
                if (fractLen == 0) {
                    reportUnexpectedNumberChar(ch, "Decimal point not followed by a digit");
                }
            }

            int expLen = 0;
            if (ch == 'e' || ch == 'E') {
                if (ptr >= inputLen) {
                    break dummy_loop;
                }
                ch = (int) _inputBuffer[ptr++];
                if (ch == INT_MINUS || ch == INT_PLUS) {
                    if (ptr >= inputLen) {
                        break dummy_loop;
                    }
                    ch = (int) _inputBuffer[ptr++];
                }
                while (ch <= INT_9 && ch >= INT_0) {
                    ++expLen;
                    if (ptr >= inputLen) {
                        break dummy_loop;
                    }
                    ch = (int) _inputBuffer[ptr++];
                }
                if (expLen == 0) {
                    reportUnexpectedNumberChar(ch, "Exponent indicator not followed by a digit");
                }
            }
            --ptr;
            _inputPtr = ptr;
            int len = ptr-startPtr;
            _textBuffer.resetWithShared(_inputBuffer, startPtr, len);
            return reset(negative, intLen, 0, 0);
        } while (false);

        _inputPtr = negative ? (startPtr+1) : startPtr;
        return _parseNumber2(negative);
    }