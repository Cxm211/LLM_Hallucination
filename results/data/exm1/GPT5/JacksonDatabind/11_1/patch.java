protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context)
    {
        final String name = type.getName();
        // First, try to resolve from context if available
        if (context != null) {
            JavaType actualType = context.findType(name);
            if (actualType != null) {
                return actualType;
            }
        }

        // Fallback to bounds
        Type[] bounds = type.getBounds();
        if (bounds == null || bounds.length == 0) {
            return _unknownType();
        }

        // Handle recursive types with placeholder when context exists
        if (context != null) {
            context._addPlaceholder(name);
            return _constructType(bounds[0], context);
        }
        // Without context, still use the first bound instead of unknown type
        return _constructType(bounds[0], null);
    }