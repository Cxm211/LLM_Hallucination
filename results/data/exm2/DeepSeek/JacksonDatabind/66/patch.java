        public final Object deserializeKey(String key, DeserializationContext ctxt)
            throws IOException
        {
            if (key == null) { // is this even legal call?
                return null;
            }
            TokenBuffer tb = new TokenBuffer(ctxt.getParser(), ctxt);
            tb.writeString(key);
            JsonParser p = tb.asParser();
            try {
                Object result = _delegate.deserialize(p, ctxt);
                if (result != null) {
                    return result;
                }
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
            } catch (Exception re) {
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
            } finally {
                p.close();
            }
        }