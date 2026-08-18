public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        String text = p.getValueAsString();
        if (text != null) { // has String representation
            if (text.length() == 0 || (text = text.trim()).length() == 0) {
                return _deserializeFromEmptyString();
            }
            Exception cause = null;
            T result = null;
            try {
                result = _deserialize(text, ctxt);
            } catch (IllegalArgumentException iae) {
                cause = iae;
            } catch (MalformedURLException me) {
                cause = me;
            }
            if (result != null) {
                return result;
            }
            String msg = "not a valid textual representation";
            if (cause != null) {
                String m2 = cause.getMessage();
                if (m2 != null) {
                    msg = msg + ", problem: "+m2;
                }
            }
            return (T) ctxt.handleWeirdStringValue(_valueClass, text, msg);
        }
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.START_ARRAY) {
            return _deserializeFromArray(p, ctxt);
        }
        if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = p.getEmbeddedObject();
            if (ob == null) {
                return null;
            }
            if (_valueClass.isAssignableFrom(ob.getClass())) {
                return (T) ob;
            }
            return _deserializeEmbedded(ob, ctxt);
        }
        return (T) ctxt.handleUnexpectedToken(_valueClass, p);
    }