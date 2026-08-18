protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // When narrowing to a subclass, the current type should become the super-class
        // reference for the newly constructed SimpleType, to preserve proper hierarchy.
        return new SimpleType(subclass, _bindings, this, null,
                _valueHandler, _typeHandler, _asStatic);
    }