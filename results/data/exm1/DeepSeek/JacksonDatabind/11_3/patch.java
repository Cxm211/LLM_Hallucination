protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context)
{
    final String name = type.getName();
    if (context != null) {
        JavaType actualType = context.findType(name);
        if (actualType != null) {
            return actualType;
        }
    }
    Type[] bounds = type.getBounds();
    if (context != null) {
        context._addPlaceholder(name);
    }
    return _constructType(bounds[0], context);
}