protected void verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit)
    {
        final int mask = (1 << typeIndex);
        _hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = _creators[typeIndex];
        if (oldOne != null) {
            if ((_explicitCreators & mask) != 0) { // already had explicitly annotated
                if (!explicit) {
                    return;
                }
                throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                            +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
            }
            // ok to override in sub-class
            Class<?> oldDecl = oldOne.getDeclaringClass();
            Class<?> newDecl = newOne.getDeclaringClass();
            if (oldDecl == newDecl) {
                // same declaring class: keep existing, no exception
            } else if (oldDecl.isAssignableFrom(newDecl)) {
                // new type more specific, use it
                if (explicit) {
                    _explicitCreators |= mask;
                }
                _creators[typeIndex] = _fixAccess(newOne);
                return;
            } else if (newDecl.isAssignableFrom(oldDecl)) {
                // new type more generic, use old
                return;
            }
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }