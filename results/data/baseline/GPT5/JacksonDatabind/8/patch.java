protected void verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit)
    {
        final int mask = (1 << typeIndex);
        _hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = _creators[typeIndex];
        if (oldOne != null) {
            boolean verify = true;
            if ((_explicitCreators & mask) != 0) { // already had explicitly annotated, leave as-is
                if (!explicit) {
                    return;
                }
                // both explicit: verify
            } else {
                // if new is explicit, no need to verify; it'll replace
                verify = !explicit;
            }

            if (verify) {
                Class<?> oldClass = oldOne.getMember().getDeclaringClass();
                Class<?> newClass = newOne.getMember().getDeclaringClass();
                if (oldClass == newClass) {
                    throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                            +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
                }
                if (oldClass.isAssignableFrom(newClass)) {
                    // new type more specific, use it
                    if (explicit) {
                        _explicitCreators |= mask;
                    }
                    _creators[typeIndex] = _fixAccess(newOne);
                    return;
                }
                if (newClass.isAssignableFrom(oldClass)) {
                    // new type more generic, use old
                    return;
                }
            }
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }