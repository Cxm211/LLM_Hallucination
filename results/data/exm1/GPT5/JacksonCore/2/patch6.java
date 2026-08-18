private int _skipWSOrEnd() throws IOException
    {
        final int[] codes = _icWS;
        while ((_inputPtr < _inputEnd) || loadMore()) {
            final int i = _inputBuffer[_inputPtr++] & 0xFF;
            final int code = codes[i];
            if (code == 0) {
                return i;
            }
            if (code == 1) {
                if (i == INT_LF) {
                    ++_currInputRow;
                    _currInputRowStart = _inputPtr;
                } else if (i == INT_CR) {
                    _skipCR();
                }
                continue;
            }
            if (code == 2) { _skipUtf8_2(i); continue; }
            if (code == 3) { _skipUtf8_3(i); continue; }
            if (code == 4) { _skipUtf8_4(i); continue; }
            switch (i) {
            case '/':
                _skipComment();
                break;
            case '#':
                if (!_skipYAMLComment()) {
                    return i;
                }
                break;
            default:
                if (i < 32) {
                    _throwInvalidSpace(i);
                }
                _reportInvalidChar(i);
            }
        }
        _handleEOF();
        return -1;
    }