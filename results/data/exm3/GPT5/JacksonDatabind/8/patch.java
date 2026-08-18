protected void verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit)
    {
        final int mask = (1 << typeIndex);
        _hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = _creators[typeIndex];
        if (oldOne != null) {
            // If we already had an explicitly annotated one
            if ((_explicitCreators & mask) != 0) {
                // keep the existing explicit one if new is not explicit
                if (!explicit) {
                    return;
                }
                // both explicit: conflict
                throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                        +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
            } else {
                // Existing one not explicit
                if (!explicit) {
                    // neither is explicit: keep the existing one to avoid unnecessary overrides
                    return;
                }
            }
            // ok to override in sub-class: no exception simply for same runtime class
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }