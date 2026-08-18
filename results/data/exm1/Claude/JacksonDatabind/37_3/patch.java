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
    // FIXED: Do not copy _superClass and _superInterfaces directly.
    // When narrowing to a subclass, we must recompute supertype information for the narrowed type.
    return new SimpleType(subclass, _bindings, null, null,
            _valueHandler, _typeHandler, _asStatic);
}