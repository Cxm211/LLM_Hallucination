// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeBoolean(boolean) [lines 805-807] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeBoolean(boolean state) throws IOException {
        _appendValue(state ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE);
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeNull() [lines 810-812] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeNull() throws IOException {
        _appendValue(JsonToken.VALUE_NULL);
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeNumber(BigDecimal) [lines 779-785] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeNumber(BigDecimal dec) throws IOException {
        if (dec == null) {
            writeNull();
        } else {
            _appendValue(JsonToken.VALUE_NUMBER_FLOAT, dec);
        }
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeNumber(BigInteger) [lines 788-794] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeNumber(BigInteger v) throws IOException {
        if (v == null) {
            writeNull();
        } else {
            _appendValue(JsonToken.VALUE_NUMBER_INT, v);
        }
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeNumber(String) [lines 797-802] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeNumber(String encodedValue) throws IOException {
        /* 03-Dec-2010, tatu: related to [JACKSON-423], should try to keep as numeric
         *   identity as long as possible
         */
        _appendValue(JsonToken.VALUE_NUMBER_FLOAT, encodedValue);
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeNumber(double) [lines 769-771] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeNumber(double d) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_FLOAT, Double.valueOf(d));
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeNumber(float) [lines 774-776] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeNumber(float f) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_FLOAT, Float.valueOf(f));
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeNumber(int) [lines 759-761] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeNumber(int i) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_INT, Integer.valueOf(i));
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeNumber(long) [lines 764-766] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeNumber(long l) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_INT, Long.valueOf(l));
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeNumber(short) [lines 754-756] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeNumber(short i) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_INT, Short.valueOf(i));
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeObject(Object) [lines 821-841] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeObject(Object value) throws IOException
    {
        if (value == null) {
            writeNull();
            return;
        }
        Class<?> raw = value.getClass();
        if (raw == byte[].class || (value instanceof RawValue)) {
            _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, value);
            return;
        }
        if (_objectCodec == null) {
            /* 28-May-2014, tatu: Tricky choice here; if no codec, should we
             *   err out, or just embed? For now, do latter.
             */
//          throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeObject() called");
            _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, value);
        } else {
            _objectCodec.writeValue(this, value);
        }
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeRawValue(String) [lines 730-732] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeRawValue(String text) throws IOException {
        _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, new RawValue(text));
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeRawValue(String, int, int) [lines 735-740] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeRawValue(String text, int offset, int len) throws IOException {
        if (offset > 0 || len != text.length()) {
            text = text.substring(offset, offset+len);
        }
        _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, new RawValue(text));
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeRawValue(char[], int, int) [lines 743-745] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeRawValue(char[] text, int offset, int len) throws IOException {
        _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, new String(text, offset, len));
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeString(SerializableString) [lines 682-688] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeString(SerializableString text) throws IOException {
        if (text == null) {
            writeNull();
        } else {
            _appendValue(JsonToken.VALUE_STRING, text);
        }
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeString(String) [lines 668-674] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeString(String text) throws IOException {
        if (text == null) {
            writeNull();
        } else {
            _appendValue(JsonToken.VALUE_STRING, text);
        }
    }

// ===== FIXED com.fasterxml.jackson.databind.util.TokenBuffer :: writeTree(TreeNode) [lines 844-857] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-31-fixed/src/main/java/com/fasterxml/jackson/databind/util/TokenBuffer.java =====
    public void writeTree(TreeNode node) throws IOException
    {
        if (node == null) {
            writeNull();
            return;
        }

        if (_objectCodec == null) {
            // as with 'writeObject()', is codec optional?
            _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, node);
        } else {
            _objectCodec.writeTree(this, node);
        }
    }
