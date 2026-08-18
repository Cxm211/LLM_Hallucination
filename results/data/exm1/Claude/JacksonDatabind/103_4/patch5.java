protected JsonSerializer<Object> _createAndCacheUntypedSerializer(Class<?> rawType)
        throws JsonMappingException
    {
        JavaType fullType = _config.constructType(rawType);
        JsonSerializer<Object> ser;
        try {
            ser = _createUntypedSerializer(fullType);
        } catch (IllegalArgumentException iae) {
            ser = null;
            String msg = iae.getMessage();
            reportMappingProblem(iae, (msg == null) ? "N/A" : msg);
        }

        if (ser != null) {
            _serializerCache.addAndResolveNonTypedSerializer(rawType, fullType, ser, this);
        }
        return ser;
    }