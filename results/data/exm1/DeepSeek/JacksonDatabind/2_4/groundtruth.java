// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeObject(Object) [lines 781-800] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-2-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeObject(Object value) throws IOException
    {
        if (value == null) {
            writeNull();
            return;
        }
        Class<?> raw = value.getClass();
        if (raw == byte[].class) {
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
            return;
        } else if (_objectCodec == null) {
            /* 28-May-2014, tatu: Tricky choice here; if no codec, should we
             *   err out, or just embed? For now, do latter.
             */
//          throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeObject() called");
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
        } else {
            _objectCodec.writeValue(this, value);
        }
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeTree(TreeNode) [lines 803-816] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-2-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeTree(TreeNode node) throws IOException
    {
        if (node == null) {
            writeNull();
            return;
        }

        if (_objectCodec == null) {
            // as with 'writeObject()', is codec optional?
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
        } else {
            _objectCodec.writeTree(this, node);
        }
    }
