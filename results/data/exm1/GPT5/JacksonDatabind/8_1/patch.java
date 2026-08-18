protected void verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit)
    {
        final int mask = (1 << typeIndex);
        _hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = _creators[typeIndex];
        if (oldOne != null) {
            if ((_explicitCreators & mask) != 0) { // already had explicitly annotated, leave as-is
                if (!explicit) {
                    return;
                }
            }
            // ok to override in sub-class: compare declaring classes, not implementation classes
            Class<?> oldDecl = oldOne.getDeclaringClass();
            Class<?> newDecl = newOne.getDeclaringClass();
            if (oldDecl == newDecl) {
                throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                        +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
            }
            if (oldDecl.isAssignableFrom(newDecl)) {
                // new one from a more specific (sub-)class: use it, fall through to set
            } else if (newDecl.isAssignableFrom(oldDecl)) {
                // existing one is from a more specific class: keep old, ignore new
                return;
            }
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }