public final Object deserializeKey(String key, DeserializationContext ctxt)
            throws IOException
        {
            if (key == null) {
                return null;
            }
            JsonParser p = null;
            try {
                TokenBuffer buf = new TokenBuffer(ctxt.getParser(), ctxt);
                buf.writeString(key);
                p = buf.asParser();
                p.nextToken();
                Object result = _delegate.deserialize(p, ctxt);
                if (result != null) {
                    return result;
                }
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
            } catch (Exception re) {
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
            } finally {
                if (p != null) {
                    p.close();
                }
            }
        }