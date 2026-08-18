protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Ensure there is a valid sub-class relationship; if not, do not narrow
        if (!_class.isAssignableFrom(subclass)) {
            return this;
        }
        return new SimpleType(subclass, _bindings, this, _superInterfaces,
                _valueHandler, _typeHandler, _asStatic);
    }