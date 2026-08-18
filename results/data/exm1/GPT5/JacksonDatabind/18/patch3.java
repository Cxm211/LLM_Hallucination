public T nextValue() throws IOException
    {
        if (!_hasNextChecked) {
            if (!hasNextValue()) {
                return _throwNoSuchElement();
            }
        }
        if (_parser == null) {
            return _throwNoSuchElement();
        }
        _hasNextChecked = false;

        try {
            T value;
            if (_updatedValue == null) {
                value = _deserializer.deserialize(_parser, _context);
            } else{
                _deserializer.deserialize(_parser, _context, _updatedValue);
                value = _updatedValue;
            }
            return value;
        } catch (IOException | RuntimeException e) {
            try {
                if (_parser != null) {
                    JsonToken t = _parser.getCurrentToken();
                    if (t == null) {
                        t = _parser.nextToken();
                    }
                    if (t == JsonToken.FIELD_NAME) {
                        t = _parser.nextToken();
                    }
                    if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
                        _parser.skipChildren();
                    }
                    while (true) {
                        JsonToken tt = _parser.getCurrentToken();
                        if (tt == JsonToken.END_OBJECT || tt == JsonToken.END_ARRAY) {
                            break;
                        }
                        tt = _parser.nextToken();
                        if (tt == null) {
                            break;
                        }
                        if (tt == JsonToken.END_OBJECT || tt == JsonToken.END_ARRAY) {
                            break;
                        }
                    }
                }
            } catch (Exception ignore) {
            } finally {
                try { if (_parser != null) { _parser.clearCurrentToken(); } } catch (Exception ignore2) {}
            }
            throw e;
        } finally {
            try { if (_parser != null) { _parser.clearCurrentToken(); } } catch (Exception ignore3) {}
        }
    }