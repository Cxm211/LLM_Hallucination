protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Ensure there is a valid sub-class relationship
        if (!_class.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException("Class "+subclass.getName()+" not a super-type of "
                    +_class.getName());
        }
        // 15-Jan-2015, tatu: Not correct; should really re-resolve...
        return new SimpleType(subclass, _bindings, _superClass, _superInterfaces,
                _valueHandler, _typeHandler, _asStatic);
    }