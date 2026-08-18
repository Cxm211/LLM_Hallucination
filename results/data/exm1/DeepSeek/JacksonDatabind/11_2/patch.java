protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context)
{
    final String name = type.getName();
    if (context == null) {
        return _unknownType();
    } else {
        context._addPlaceholder(name);
        JavaType actualType = context.findType(name);
        if (actualType != null && !context.hasPlaceholder(name)) {
            return actualType;
        }
    }

    Type[] bounds = type.getBounds();
    return _constructType(bounds[0], context);
}