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
        // Adjust processed byte/row offsets to account for initial buffer offset
        _currInputProcessed = -start;
        _currInputRowStart = start;
        _bufferRecyclable = bufferRecyclable;
    }