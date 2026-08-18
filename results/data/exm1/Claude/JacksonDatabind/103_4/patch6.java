protected JsonSerializer<Object> _createAndCacheUntypedSerializer(JavaType type)
        throws JsonMappingException
    {        
        JsonSerializer<Object> ser;
        try {
            ser = _createUntypedSerializer(type);
        } catch (IllegalArgumentException iae) {
            ser = null;
            String msg = iae.getMessage();
            reportMappingProblem(iae, (msg == null) ? "N/A" : msg);
        }
    
        if (ser != null) {
            _serializerCache.addAndResolveNonTypedSerializer(type, ser, this);
        }
        return ser;
    }