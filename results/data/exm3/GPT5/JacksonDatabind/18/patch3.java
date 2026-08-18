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
        } catch (IOException e) {
            // Attempt recovery: skip the rest of the current value and advance to next
            try {
                if (_parser != null) {
                    if (_parser.getCurrentToken() != null) {
                        _parser.skipChildren();
                    }
                    _parser.nextToken();
                }
            } catch (Exception ignore) { }
            throw e;
        } catch (RuntimeException e) {
            try {
                if (_parser != null) {
                    if (_parser.getCurrentToken() != null) {
                        _parser.skipChildren();
                    }
                    _parser.nextToken();
                }
            } catch (Exception ignore) { }
            throw e;
        } finally {
            /* 24-Mar-2015, tatu: As per [#733], need to mark token consumed no
             *   matter what, to avoid infinite loop for certain failure cases.
             *   For 2.6 need to improve further.
             */
            if (_parser != null) {
                _parser.clearCurrentToken();
            }
        }
    }