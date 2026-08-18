protected void verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit)
    {
        final int mask = (1 << typeIndex);
        _hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = _creators[typeIndex];
        if (oldOne != null) {
            if ((_explicitCreators & mask) != 0) { // already had explicitly annotated, leave as-is
                if (!explicit) {
                    // if new one is a subclass, allow override
                    if (!newOne.getClass().isAssignableFrom(oldOne.getClass())) {
                        return;
                    }
                } else {
                    // both explicit: verify
                    if (oldOne.getClass() == newOne.getClass()) {
                        throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                                +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
                    }
                    // new more specific? replace old
                    if (!oldOne.getClass().isAssignableFrom(newOne.getClass())) {
                        // old is more specific or unrelated? keep old
                        return;
                    }
                }
            } else {
                // old not explicit, new is explicit? will replace later
                // if both non-explicit, check subclass
                if (!explicit) {
                    if (oldOne.getClass() != newOne.getClass()) {
                        // if new is superclass of old, keep old; else replace
                        if (!newOne.getClass().isAssignableFrom(oldOne.getClass())) {
                            // new is not a superclass, check if old is superclass of new? If old is superclass, keep old
                            if (oldOne.getClass().isAssignableFrom(newOne.getClass())) {
                                // old is superclass, keep old? Actually, we want more specific, so if old is superclass, we want new? Wait.
                                // The rule: if new is subclass of old, replace; else keep old.
                                // So here: if old is superclass of new, then new is subclass, so replace? But we are in non-explicit branch, let's decide: we always want the most specific creator. Since both non-explicit, we should pick the more specific one.
                                // So if new is subclass of old, we will replace; otherwise keep old.
                                // But the code below will always assign newOne at the end. So we need to conditionally assign.
                            }
                        }
                    }
                }
            }
            // one more thing: ok to override in sub-class
            if (oldOne.getClass() == newOne.getClass()) {
                // [databind#667]: avoid one particular class of bogus problems
                    throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                            +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
                // otherwise, which one to choose?
                    // new type more generic, use old
                // new type more specific, use it
            }
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }