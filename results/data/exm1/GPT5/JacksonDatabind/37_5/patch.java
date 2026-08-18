protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Re-resolve supertypes for the new subclass instead of carrying over old ones
        return new SimpleType(subclass, _bindings, null, null,
                _valueHandler, _typeHandler, _asStatic);
    }