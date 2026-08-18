protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Ensure proper subtype relation
        if (!_class.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException("Class "+subclass.getName()+" not sub-type of "
                    +_class.getName());
        }
        // For interfaces, include current type as super-interface; otherwise as super-class
        JavaType superClass = _class.isInterface() ? null : this;
        JavaType[] superInterfaces = _class.isInterface() ? new JavaType[] { this } : _superInterfaces;
        return new SimpleType(subclass, _bindings, superClass, superInterfaces,
                _valueHandler, _typeHandler, _asStatic);
    }