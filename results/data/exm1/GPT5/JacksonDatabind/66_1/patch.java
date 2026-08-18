public final Object deserializeKey(String key, DeserializationContext ctxt)
            throws IOException
        {
            if (key == null) { // is this even legal call?
                return null;
            }
            try {
                com.fasterxml.jackson.databind.util.TokenBuffer tb = new com.fasterxml.jackson.databind.util.TokenBuffer(ctxt.getParser(), ctxt);
                tb.writeString(key);
                com.fasterxml.jackson.core.JsonParser p = tb.asParser();
                p.nextToken();
                Object result = _delegate.deserialize(p, ctxt);
                p.close();
                tb.close();
                if (result != null) {
                    return result;
                }
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
            } catch (Exception re) {
                return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation: %s", re.getMessage());
            }
        }