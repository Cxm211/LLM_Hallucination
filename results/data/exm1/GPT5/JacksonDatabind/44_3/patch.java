protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        JavaType superClass = _class.isInterface() ? null : this;
        JavaType[] superInts = _class.isInterface() ? new JavaType[] { this } : _superInterfaces;
        return new SimpleType(subclass, _bindings, superClass, superInts,
                _valueHandler, _typeHandler, _asStatic);
    }