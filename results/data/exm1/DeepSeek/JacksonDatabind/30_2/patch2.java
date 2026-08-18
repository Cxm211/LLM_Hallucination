    public TokenBuffer(ObjectCodec codec, boolean parsedAsString)
    {
        _objectCodec = codec;
        _generatorFeatures = DEFAULT_GENERATOR_FEATURES;
        _writeContext = JsonWriteContext.createRootContext(null);
        _first = _last = new Segment();
        _appendAt = 0;
        _hasNativeTypeIds = false;
        _hasNativeObjectIds = false;
        _mayHaveNativeIds = false;
    }