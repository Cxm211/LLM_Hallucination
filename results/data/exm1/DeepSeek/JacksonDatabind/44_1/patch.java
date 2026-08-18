protected JavaType _narrow(Class<?> subclass)
{
    if (_class == subclass) {
        return this;
    }
    // Should we check that there is a sub-class relationship?
    if (!_class.isAssignableFrom(subclass)) {
        throw new IllegalArgumentException("Class "+subclass.getName()+" not sub-type of "
                +_class.getName());
    }
    return new SimpleType(subclass, _bindings, this, _superInterfaces,
            _valueHandler, _typeHandler, _asStatic);
    // Otherwise, stitch together the hierarchy. First, super-class
    // if not found, try a super-interface
    // should not get here but...
}