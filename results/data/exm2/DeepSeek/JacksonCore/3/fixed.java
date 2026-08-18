// ===== FIXED com.fasterxml.jackson.core.json.UTF8StreamJsonParser :: UTF8StreamJsonParser [lines 113-129] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-3-fixed/src/main/java/com/fasterxml/jackson/core/json/UTF8StreamJsonParser.java =====
    public UTF8StreamJsonParser(IOContext ctxt, int features, InputStream in,
            ObjectCodec codec, BytesToNameCanonicalizer sym,
            byte[] inputBuffer, int start, int end,
            boolean bufferRecyclable)
    {
        super(ctxt, features);
        _inputStream = in;
        _objectCodec = codec;
        _symbols = sym;
        _inputBuffer = inputBuffer;
        _inputPtr = start;
        _inputEnd = end;
        _currInputRowStart = start;
        // If we have offset, need to omit that from byte offset, so:
        _currInputProcessed = -start;
        _bufferRecyclable = bufferRecyclable;
    }
