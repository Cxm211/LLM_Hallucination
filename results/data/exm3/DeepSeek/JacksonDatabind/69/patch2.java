    protected void verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit)
    {
        final int mask = (1 << typeIndex);
        _hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = _creators[typeIndex];
        // already had an explicitly marked one?
        if (oldOne != null) {
            boolean verify;
            if ((_explicitCreators & mask) != 0) { // already had explicitly annotated, leave as-is
                // but skip, if new one not annotated
                if (!explicit) {
                    return;
                }
                // both explicit: verify
                verify = true;
            } else {
                // otherwise only verify if neither explicitly annotated.
                verify = !explicit;
            }

            if (verify) {
                Class<?> oldDecl = oldOne.getDeclaringClass();
                Class<?> newDecl = newOne.getDeclaringClass();
                if (oldDecl == newDecl) {
                    // same declaring class: check parameter types
                    Class<?> oldType = oldOne.getRawParameterType(0);
                    Class<?> newType = newOne.getRawParameterType(0);

                    if (oldType == newType) {
                        throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                                +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
                    }
                    // [databind#667]: avoid one particular class of bogus problems
                    if (newType.isAssignableFrom(oldType)) {
                        // new type more generic, use old
                        return;
                    }
                    if (oldType.isAssignableFrom(newType)) {
                        // new type more specific, use it (fall through)
                    } else {
                        // unrelated types, conflict
                        throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                                +" creators: already had "+oldOne+" with parameter type "+oldType+", encountered "+newOne+" with parameter type "+newType);
                    }
                } else {
                    // different declaring classes: allow override only if newDecl is a subclass of oldDecl
                    if (oldDecl.isAssignableFrom(newDecl)) {
                        // newOne is from a subclass (or same, but we already handled equality). Allow override.
                        // fall through to replace.
                    } else if (newDecl.isAssignableFrom(oldDecl)) {
                        // oldOne is from a subclass, so keep oldOne
                        return;
                    } else {
                        // unrelated classes, should not happen in standard hierarchy, but could with mix-ins?
                        throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                                +" creators: already had "+oldOne+" from "+oldDecl+", encountered "+newOne+" from "+newDecl);
                    }
                }
            }
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }