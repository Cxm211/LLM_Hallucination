    protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Check that subclass is indeed a subtype of _class
        if (!_class.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException("Class "+subclass.getName()+" not sub-type of "
                    +_class.getName());
        }
        JavaType superClass = null;
        JavaType[] superInterfaces;
        if (_class.isInterface()) {
            // Determine if subclass directly implements _class
            boolean direct = false;
            for (Class<?> iface : subclass.getInterfaces()) {
                if (iface == _class) {
                    direct = true;
                    break;
                }
            }
            if (direct) {
                superInterfaces = new JavaType[] { this };
            } else {
                // Subclass implements _class through a superclass or another interface.
                // For now, we still include this as superInterface.
                superInterfaces = new JavaType[] { this };
            }
        } else {
            // _class is a class
            Class<?> parent = subclass.getSuperclass();
            if (parent == _class) {
                superClass = this;
            }
            // else superClass remains null
            superInterfaces = _superInterfaces;
        }
        return new SimpleType(subclass, _bindings, superClass, superInterfaces,
                _valueHandler, _typeHandler, _asStatic);
    }