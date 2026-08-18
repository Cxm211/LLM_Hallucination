protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context)
    {
        final String name = type.getName();
        if (context == null) {
            return _unknownType();
        } else {
            JavaType actualType = context.findType(name);
            if (actualType != null) {
                return actualType;
            }
        }

        context._addPlaceholder(name);
        Type[] bounds = type.getBounds();
        if (bounds != null && bounds.length > 0) {
            return _constructType(bounds[0], context);
        }
        return _unknownType();
    }