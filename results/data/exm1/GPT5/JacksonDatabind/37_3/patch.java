protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        /*
        if (!_class.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException("Class "+subclass.getName()+" not sub-type of "
                    +_class.getName());
        }
        */
        // Ensure proper super-type linkage: current type becomes super-type of the narrowed type
        return new SimpleType(subclass, _bindings, this, _superInterfaces,
                _valueHandler, _typeHandler, _asStatic);
    }