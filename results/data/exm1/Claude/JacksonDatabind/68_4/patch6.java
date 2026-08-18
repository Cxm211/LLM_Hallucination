public Object deserializeFromEmbedded(JsonParser p, DeserializationContext ctxt)
        throws IOException
    {
        if (_objectIdReader != null) {
            return deserializeFromObjectId(p, ctxt);
        }
        return p.getEmbeddedObject();
    }