// buggy function
    public String getValueAsString() throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            return getText();
        }
        return getValueAsString(null);
    }

    public String getValueAsString(String defaultValue) throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            return getText();
        }
        if (_currToken == null || _currToken == JsonToken.VALUE_NULL || !_currToken.isScalarValue()) {
            return defaultValue;
        }
        return getText();
    }

    public final String getValueAsString() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        return super.getValueAsString(null);
    }

    public final String getValueAsString(String defValue) throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        return super.getValueAsString(defValue);
    }

    public String getValueAsString() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        return super.getValueAsString(null);
    }

    public String getValueAsString(String defValue) throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        return super.getValueAsString(defValue);
    }

// trigger testcase
// com/fasterxml/jackson/core/json/TestJsonParser.java::testGetValueAsTextBytes
public void testGetValueAsTextBytes() throws Exception
    {
        JsonFactory f = new JsonFactory();
        _testGetValueAsText(f, true, false);
        _testGetValueAsText(f, true, true);
    }

// com/fasterxml/jackson/core/json/TestJsonParser.java::testGetValueAsTextChars
public void testGetValueAsTextChars() throws Exception
    {
        JsonFactory f = new JsonFactory();
        _testGetValueAsText(f, false, false);
        _testGetValueAsText(f, false, true);
    }
