protected JsonSerializer<Object> _createAndCacheUntypedSerializer(JavaType type)
        throws JsonMappingException
    {        
        JsonSerializer<Object> ser;
        try {
            ser = _createUntypedSerializer(type);
        } catch (IllegalArgumentException iae) {
            // We better only expose checked exceptions, since those
            // are what caller is expected to handle
            ser = null;
            String msg = iae.getMessage();
            reportMappingProblem(iae, msg == null ? "N/A" : msg);
        }
    
        if (ser != null) {
            // 21-Dec-2015, tatu: Should we also cache using raw key?
            _serializerCache.addAndResolveNonTypedSerializer(type, ser, this);
        }
        return ser;
    }