protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // If current type is an interface, treat it as a super-interface of the subclass
        if (_class.isInterface()) {
            return new SimpleType(subclass, _bindings, null, new JavaType[] { this },
                    _valueHandler, _typeHandler, _asStatic);
        }
        // Otherwise, current type is considered a super-class of the subclass
        return new SimpleType(subclass, _bindings, this, null,
                _valueHandler, _typeHandler, _asStatic);
    }