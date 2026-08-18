protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context)
    {
        final String name = type.getName();
        if (context != null) {
            JavaType actualType = context.findType(name);
            if (actualType != null) {
                return actualType;
            }
            // add placeholder to handle recursive bounds when context is available
            context._addPlaceholder(name);
        }

        // Without (or beyond) context, use bounds (defaulting to Object)
        Type[] bounds = type.getBounds();
        if (bounds == null || bounds.length == 0) {
            return _unknownType();
        }
        return _constructType(bounds[0], context);
    