    public void removeNonVisible(boolean inferMutators) {
        JsonProperty.Access acc = findAccess();
        if (acc == null) {
            acc = JsonProperty.Access.AUTO;
        }
        switch (acc) {
        case READ_ONLY:
            _setters = null;
            _ctorParameters = null;
            if (!_forSerialization) {
                _fields = null;
            }
            break;
        case READ_WRITE:
            break;
        case WRITE_ONLY:
            _getters = null;
            if (_forSerialization) {
                _fields = null;
            }
            break;
        default:
        case AUTO:
            _getters = _removeNonVisible(_getters);
            _ctorParameters = _removeNonVisible(_ctorParameters);
            _fields = _removeNonVisible(_fields);
            _setters = _removeNonVisible(_setters);
            break;
        }
    }