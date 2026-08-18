public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        String text = p.getValueAsString();
        if (text != null) { // has String representation
            if (text.length() == 0 || (text = text.trim()).length() == 0) {
                return _deserializeFromEmptyString();
            }
            try {
                return _deserialize(text, ctxt);
            } catch (IllegalArgumentException iae) {
                String msg = "not a valid textual representation";
                String m2 = iae.getMessage();
                if (m2 != null) {
                    msg = msg + ", problem: " + m2;
                }
                JsonMappingException e = ctxt.weirdStringException(text, _valueClass, msg);
                e.initCause(iae);
                throw e;
            } catch (MalformedURLException me) {
                String msg = "not a valid textual representation";
                String m2 = me.getMessage();
                if (m2 != null) {
                    msg = msg + ", problem: " + m2;
                }
                JsonMappingException e = ctxt.weirdStringException(text, _valueClass, msg);
                e.initCause(me);
                throw e;
            }
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