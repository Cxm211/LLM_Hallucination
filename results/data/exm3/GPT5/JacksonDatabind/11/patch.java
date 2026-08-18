protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context)
    {
        final String name = type.getName();
        // 19-Mar-2015: Without context, we should try to use bounds if available.
        Type[] bounds = type.getBounds();
        if (context == null) {
            // Use first bound if available; avoid potential recursion by erasing parameterized bounds
            if (bounds != null && bounds.length > 0) {
                Type b = bounds[0];
                if (b instanceof java.lang.reflect.ParameterizedType) {
                    b = ((java.lang.reflect.ParameterizedType) b).getRawType();
                }
                return _constructType(b, null);
            }
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
        }

        /* 29-Jan-2010, tatu: We used to throw exception here, if type was
         *   bound: but the problem is that this can occur for generic "base"
         *   method, overridden by sub-class. If so, we will want to ignore
         *   current type (for method) since it will be masked.
         */

        // With type variables we must use bound information.
        // Theoretically this gets tricky, as there may be multiple
        // bounds ("... extends A & B"); and optimally we might
        // want to choose the best match. Also, bounds are optional;
        // but here we are lucky in that implicit "Object" is
        // added as bounds if so.
        // Either way let's just use the first bound, for now, and
        // worry about better match later on if there is need.

        /* 29-Jan-2010, tatu: One more problem are recursive types
         *   (T extends Comparable<T>). Need to add "placeholder"
         *   for resolution to catch those.
         */
        context._addPlaceholder(name);
        return _constructType(bounds[0], context);
    }