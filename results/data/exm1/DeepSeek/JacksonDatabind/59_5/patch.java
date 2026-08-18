    protected JavaType(JavaType base) 
    {
        _class = base._class;
        _hash = base._hash;
        _valueHandler = base._valueHandler;
        _typeHandler = base._typeHandler;
        _asStatic = base._asStatic;
        _bindings = base._bindings;
        _superClass = base._superClass;
        _superInterfaces = base._superInterfaces;
    }