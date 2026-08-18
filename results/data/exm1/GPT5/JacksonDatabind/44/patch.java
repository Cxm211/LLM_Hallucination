protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Create specialized simple type for the subclass; do not inherit superInterfaces
        // from the current type since they correspond to current raw type, not the subclass.
        return new SimpleType(subclass, _bindings, this, null,
                _valueHandler, _typeHandler, _asStatic);
    }