public CharacterReader(Reader input, int sz) {
        Validate.notNull(input);
        Validate.isTrue(input.markSupported());
        reader = input;
        charBuf = new char[sz > maxBufferLen ? maxBufferLen : sz];
        try {
            bufferUp();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }