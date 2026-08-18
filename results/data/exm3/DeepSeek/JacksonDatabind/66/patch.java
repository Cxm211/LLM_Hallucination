        public final Object deserializeKey(String key, DeserializationContext ctxt)
            throws IOException
        {
            if (key == null) { // is this even legal call?
                return null;
            }
            try {
                // Ugh... should not have to give parser which may or may not be correct one...
                // Use TokenBuffer to feed the key string as a JSON string value.
                TokenBuffer tb = new TokenBuffer(ctxt.getParser(), ctxt);
                tb.writeString(key);
                JsonParser p = tb.asParser();
                p.nextToken();
                Object result = _delegate.deserialize(p, ctxt);
                p.close();
                if (result != null) {
                    return result;
                }
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
            } catch (Exception re) {
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
            }
        }