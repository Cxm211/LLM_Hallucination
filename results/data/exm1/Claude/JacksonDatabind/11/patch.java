protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context)
    {
        final String name = type.getName();
        // 19-Mar-2015: Without context, all we can check are bounds.
        if (context == null) {
            // And to prevent infinite loops, now need this:
            return _unknownType();
        } else {
            // Ok: here's where context might come in handy!
            /* 19-Mar-2015, tatu: As per [databind#609], may need to allow
             *   unresolved type variables to handle some cases where bounds
             *   are enough. Let's hope it does not hide real fail cases.
             */
            JavaType actualType = context.findType(name);
            if (actualType != null) {
                return actualType;
            }
            // If not found in context, return unknown type to avoid NPE
            return _unknownType();
        }

        /* 29-Jan-2010, tatu: We used to throw exception here, if type was
         *   bound: but the problem is that this can occur for generic "base"
         *   method, overridden by sub-class. If so, we will want to ignore
         *   current type (for method) since it will be masked.
         */
        // Note: This code is now unreachable due to early returns above
        // Type[] bounds = type.getBounds();
        // context._addPlaceholder(name);
        // return _constructType(bounds[0], context);
    }