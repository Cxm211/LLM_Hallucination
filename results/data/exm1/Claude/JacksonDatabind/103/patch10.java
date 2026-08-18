protected JsonDeserializer<Object> _createAndCache2(DeserializationContext ctxt,
            DeserializerFactory factory, JavaType type)
        throws JsonMappingException
    {
        JsonDeserializer<Object> deser;
        try {
            deser = _createDeserializer(ctxt, factory, type);
        } catch (IllegalArgumentException iae) {
            
            
            throw JsonMappingException.from(ctxt, ClassUtil.exceptionMessage(iae), iae);
        }
        if (deser == null) {
            return null;
        }
        
        
        
        
        boolean addToCache = !_hasCustomHandlers(type) && deser.isCachable();

        
        
        
        
        
        
        
        
        
        
        
        if (deser instanceof ResolvableDeserializer) {
            _incompleteDeserializers.put(type, deser);
            ((ResolvableDeserializer)deser).resolve(ctxt);
            _incompleteDeserializers.remove(type);
        }
        if (addToCache) {
            _cachedDeserializers.put(type, deser);
        }
        return deser;
    }