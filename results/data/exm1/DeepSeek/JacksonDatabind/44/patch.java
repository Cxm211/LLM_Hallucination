protected JavaType _narrow(Class<?> subclass)
{
    if (_class == subclass) {
        return this;
    }
    // Should we check that there is a sub-class relationship?
    // 15-Jan-2016, tatu: Almost yes, but there are some complications with
    //    placeholder values (`Void`, `NoClass`), so can not quite do yet.
    // TODO: fix in 2.8
    // First, check if superclass can be narrowed
    Class<?> superClass = subclass.getSuperclass();
    if (superClass != null && _class.isAssignableFrom(superClass)) {
        JavaType superType = _narrow(superClass);
        return new SimpleType(subclass, _bindings, superType, _superInterfaces,
                _valueHandler, _typeHandler, _asStatic);
    }
    // Then, try interfaces
    for (Class<?> iface : subclass.getInterfaces()) {
        if (_class.isAssignableFrom(iface)) {
            JavaType superType = _narrow(iface);
            return new SimpleType(subclass, _bindings, superType, _superInterfaces,
                    _valueHandler, _typeHandler, _asStatic);
        }
    }
    // Otherwise, just return a simple type (fallback)
    return new SimpleType(subclass, _bindings, this, _superInterfaces,
            _valueHandler, _typeHandler, _asStatic);
}