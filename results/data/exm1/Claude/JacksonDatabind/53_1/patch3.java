protected JavaType _fromClass(ClassStack context, Class<?> rawType, TypeBindings bindings)
{
    JavaType result = _findWellKnownSimple(rawType);
    if (result != null) {
        return result;
    }
    boolean cachable = (bindings == null) || bindings.isEmpty();
    if (cachable) {
        result = _typeCache.get(rawType);
        if (result != null) {
            return result;
        }
    }

    if (context == null) {
        context = new ClassStack(rawType);
    } else {
        ClassStack prev = context.find(rawType);
        if (prev != null) {
            ResolvedRecursiveType selfRef = new ResolvedRecursiveType(rawType, EMPTY_BINDINGS);
            prev.addSelfReference(selfRef);
            return selfRef;
        }
        context = context.child(rawType);
    }

    if (rawType.isArray()) {
        result = ArrayType.construct(_fromAny(context, rawType.getComponentType(), bindings),
                bindings);
    } else {
        JavaType superClass;
        JavaType[] superInterfaces;

        if (rawType.isInterface()) {
            superClass = null;
            superInterfaces = _resolveSuperInterfaces(context, rawType, bindings);
        } else {
            superClass = _resolveSuperClass(context, rawType, bindings);
            superInterfaces = _resolveSuperInterfaces(context, rawType, bindings);
        }

        if (rawType == Properties.class) {
            result = MapType.construct(rawType, bindings, superClass, superInterfaces,
                    CORE_TYPE_STRING, CORE_TYPE_STRING);
        } else if (superClass != null) {
            result = superClass.refine(rawType, bindings, superClass, superInterfaces);
        }
        if (result == null) {
            result = _fromWellKnownClass(context, rawType, bindings, superClass, superInterfaces);
            if (result == null) {
                result = _fromWellKnownInterface(context, rawType, bindings, superClass, superInterfaces);
                if (result == null) {
                    result = _newSimpleType(rawType, bindings, superClass, superInterfaces);
                }
            }
        }
    }
    context.resolveSelfReferences(result);
    if (cachable) {
        _typeCache.putIfAbsent(rawType, result);
    }
    return result;
}