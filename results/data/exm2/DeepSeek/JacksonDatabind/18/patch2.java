public boolean hasNextValue() throws IOException
    {
        if (_parser == null) {
            return false;
        }
        if (!_hasNextChecked) {
            JsonToken t = _parser.getCurrentToken();
            if (t == null) {
                t = _parser.nextToken();
                if (t == null || t == JsonToken.END_ARRAY) {
                    JsonParser jp = _parser;
                    _parser = null;
                    if (_closeParser) {
                        jp.close();
                    }
                    return false;
                }
            }
            _hasNextChecked = true;
        }
        return true;
    }