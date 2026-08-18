public CharacterReader(Reader input, int sz) {
        Validate.notNull(input);
        if (!input.markSupported()) {
            input = new BufferedReader(input, Math.max(1, Math.min(sz, maxBufferLen)));
        }
        reader = input;
        int bufferSize = sz > maxBufferLen ? maxBufferLen : sz;
        if (bufferSize < 1) bufferSize = 1;
        charBuf = new char[bufferSize];
        bufferUp();

    }