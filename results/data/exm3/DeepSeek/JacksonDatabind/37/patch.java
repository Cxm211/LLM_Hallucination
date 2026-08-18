    protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Should we check that there is a sub-class relationship?
        // 15-Jan-2016, tatu: Almost yes, but there are some complications with
        //    placeholder values, so no.
        /*
        if (!_class.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException("Class "+subclass.getName()+" not sub-type of "
                    +_class.getName());
        }
        */
        // 15-Jan-2015, tatu: Not correct; should really re-resolve...
        // Re-resolve bindings if the number of type parameters differs.
        JavaType[] newBindings = _bindings;
        if (_bindings != null) {
            int origParamCount = _class.getTypeParameters().length;
            int newParamCount = subclass.getTypeParameters().length;
            if (origParamCount != newParamCount) {
                newBindings = null;
            }
        }
        // Re-resolve handlers by setting them to null; they will be resolved as needed.
        return new SimpleType(subclass, newBindings, _superClass, _superInterfaces,
                null, null, _asStatic);
    }