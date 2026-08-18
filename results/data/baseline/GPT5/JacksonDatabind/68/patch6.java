public Object deserializeFromEmbedded(JsonParser p, DeserializationContext ctxt)
        throws IOException
    {
        // First things first: id Object Id is used, most likely that's it; specifically,
        // true for UUIDs when written as binary (with Smile, other binary formats)
        if (_objectIdReader != null) {
            return deserializeFromObjectId(p, ctxt);
        }

        // TODO: maybe add support for ValueInstantiator, embedded?
        
        return p.getEmbeddedObject();
    }