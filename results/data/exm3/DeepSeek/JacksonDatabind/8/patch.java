    protected void verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit)
    {
        final int mask = (1 << typeIndex);
        _hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = _creators[typeIndex];
        // already had an explicitly marked one?
        if (oldOne != null) {

            if ((_explicitCreators & mask) != 0) { // already had explicitly annotated, leave as-is
                // but skip, if new one not annotated
                if (!explicit) {
                    return;
                }
                // both explicit: verify
                // otherwise only verify if neither explicitly annotated.
            }

            // one more thing: ok to override in sub-class
            if (oldOne.getDeclaringClass() == newOne.getDeclaringClass()) {
                // [databind#667]: avoid one particular class of bogus problems

                    throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                            +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
                // otherwise, which one to choose?
                    // new type more generic, use old
                // new type more specific, use it
            }
            // otherwise, which one to choose?
            // new type more generic, use old
            // new type more specific, use it
            Class<?> oldType = oldOne.getDeclaringClass();
            Class<?> newType = newOne.getDeclaringClass();
            if (oldType.isAssignableFrom(newType)) {
                // new type more specific, use it
                return;
            }
            if (newType.isAssignableFrom(oldType)) {
                // new type more generic, use old
                _creators[typeIndex] = _fixAccess(newOne);
                return;
            }
            // unrelated; throw
            throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                    +" creators: already had "+oldOne+", encountered "+newOne);
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }