    public CharacterReader(Reader input, int sz) {
        Validate.notNull(input);
        Validate.isTrue(input.markSupported());
        Validate.isTrue(sz > 0, "Buffer size must be positive");
        reader = input;
        charBuf = new char[sz > maxBufferLen ? maxBufferLen : sz];
        bufferUp();

    }