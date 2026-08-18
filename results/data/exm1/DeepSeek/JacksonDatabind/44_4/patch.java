    protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Recursively narrow superclass chain
        Class<?> superClass = subclass.getSuperclass();
        if (superClass != null) {
            JavaType superType = _narrow(superClass);
            return new SimpleType(subclass, _bindings, superType, _superInterfaces,
                    _valueHandler, _typeHandler, _asStatic);
        }
        // Fallback: use current type as superclass (should not normally occur)
        return new SimpleType(subclass, _bindings, this, _superInterfaces,
                _valueHandler, _typeHandler, _asStatic);
    }