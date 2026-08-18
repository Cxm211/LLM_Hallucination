protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context)
{
    final String name = type.getName();
    if (context == null) {
        return _unknownType();
    }
    // Add placeholder first to prevent infinite recursion
    context._addPlaceholder(name);
    JavaType actualType = context.findType(name);
    if (actualType != null) {
        return actualType;
    }
    Type[] bounds = type.getBounds();
    // In case bounds are empty, which should not happen, but be safe
    if (bounds == null || bounds.length == 0) {
        return _unknownType();
    }
    return _constructType(bounds[0], context);
}