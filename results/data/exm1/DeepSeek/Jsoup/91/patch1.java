public CharacterReader(Reader input, int sz) {
    Validate.notNull(input);
    if (!input.markSupported()) {
        input = new BufferedReader(input);
    }
    reader = input;
    charBuf = new char[sz > maxBufferLen ? maxBufferLen : sz];
    bufferUp();
}