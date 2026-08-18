// ===== FIXED com.fasterxml.jackson.databind.deser.impl.CreatorCollector :: verifyNonDup(AnnotatedWithParams, int, boolean) [lines 276-319] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-8-fixed/src/main/java/com/fasterxml/jackson/databind/deser/impl/CreatorCollector.java =====
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

            // one more thing: ok to override in sub-class
            if (verify && (oldOne.getClass() == newOne.getClass())) {
                // [databind#667]: avoid one particular class of bogus problems
                Class<?> oldType = oldOne.getRawParameterType(0);
                Class<?> newType = newOne.getRawParameterType(0);

                if (oldType == newType) {
                    throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                            +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
                }
                // otherwise, which one to choose?
                if (newType.isAssignableFrom(oldType)) {
                    // new type more generic, use old
                    return;
                }
                // new type more specific, use it
            }
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }
