// ===== FIXED com.fasterxml.jackson.core.base.ParserMinimalBase :: getValueAsString() [lines 388-396] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-9-fixed/src/main/java/com/fasterxml/jackson/core/base/ParserMinimalBase.java =====
    public String getValueAsString() throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            return getText();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return getValueAsString(null);
    }

// ===== FIXED com.fasterxml.jackson.core.base.ParserMinimalBase :: getValueAsString(String) [lines 399-410] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-9-fixed/src/main/java/com/fasterxml/jackson/core/base/ParserMinimalBase.java =====
    public String getValueAsString(String defaultValue) throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            return getText();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        if (_currToken == null || _currToken == JsonToken.VALUE_NULL || !_currToken.isScalarValue()) {
            return defaultValue;
        }
        return getText();
    }

// ===== FIXED com.fasterxml.jackson.core.json.ReaderBasedJsonParser :: getValueAsString() [lines 244-257] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-9-fixed/src/main/java/com/fasterxml/jackson/core/json/ReaderBasedJsonParser.java =====
    public final String getValueAsString() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return super.getValueAsString(null);
    }

// ===== FIXED com.fasterxml.jackson.core.json.ReaderBasedJsonParser :: getValueAsString(String) [lines 261-273] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-9-fixed/src/main/java/com/fasterxml/jackson/core/json/ReaderBasedJsonParser.java =====
    public final String getValueAsString(String defValue) throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return super.getValueAsString(defValue);
    }

// ===== FIXED com.fasterxml.jackson.core.json.UTF8StreamJsonParser :: getValueAsString() [lines 296-309] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-9-fixed/src/main/java/com/fasterxml/jackson/core/json/UTF8StreamJsonParser.java =====
    public String getValueAsString() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return super.getValueAsString(null);
    }

// ===== FIXED com.fasterxml.jackson.core.json.UTF8StreamJsonParser :: getValueAsString(String) [lines 313-326] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-9-fixed/src/main/java/com/fasterxml/jackson/core/json/UTF8StreamJsonParser.java =====
    public String getValueAsString(String defValue) throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return super.getValueAsString(defValue);
    }
