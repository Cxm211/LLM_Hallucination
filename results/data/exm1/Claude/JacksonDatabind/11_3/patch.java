protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context)
{
    final String name = type.getName();
    if (context == null) {
        return _unknownType();
    }
    JavaType actualType = context.findType(name);
    if (actualType != null) {
        return actualType;
    }
    if (context._addPlaceholder(name)) {
        Type[] bounds = type.getBounds();
        return _constructType(bounds[0], context);
    }
    return _unknownType();
}