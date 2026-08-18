protected JsonSerializer<Object> _createAndCacheUntypedSerializer(JavaType type)
        throws JsonMappingException
    {        
        JsonSerializer<Object> ser;
        try {
            ser = _createUntypedSerializer(type);
        } catch (IllegalArgumentException iae) {
            
            
            ser = null;
            reportMappingProblem(iae, ClassUtil.exceptionMessage(iae));
        }
    
        if (ser != null) {
            
            _serializerCache.addAndResolveNonTypedSerializer(type, ser, this);
        }
        return ser;
    }