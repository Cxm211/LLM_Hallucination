public final Object deserializeKey(String key, DeserializationContext ctxt)
            throws IOException
        {
            if (key == null) { // is this even legal call?
                return null;
            }
            try {
                // Use a parser over the key String, not the context parser
                TokenBuffer buf = new TokenBuffer(ctxt.getParser(), ctxt);
                buf.writeString(key);
                JsonParser p = buf.asParser();
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