protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Re-resolve hierarchy for the new subclass; do not reuse old super-types
        return new SimpleType(subclass, _bindings, null, null,
                _valueHandler, _typeHandler, _asStatic);
    }