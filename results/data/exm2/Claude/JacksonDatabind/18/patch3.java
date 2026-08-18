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
            _parser.clearCurrentToken();
            return value;
        } catch (Exception e) {
            /* Need to mark token consumed and skip to recovery point */
            _hasNextChecked = false;
            _parser.skipChildren();
            _parser.clearCurrentToken();
            throw e;
        }
    }