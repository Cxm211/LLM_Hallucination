protected void verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit)
    {
        final int mask = (1 << typeIndex);
        _hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = _creators[typeIndex];
        if (oldOne != null) {
            if ((_explicitCreators & mask) != 0) {
                if (!explicit) {
                    return;
                }
                Class<?> oldType = oldOne.getRawParameterType(0);
                Class<?> newType = newOne.getRawParameterType(0);
                if (oldType == newType) {
                    throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                            +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
                }
                if (newType.isAssignableFrom(oldType)) {
                    return;
                }
            } else {
                if (oldOne.getClass() == newOne.getClass()) {
                    Class<?> oldType = oldOne.getRawParameterType(0);
                    Class<?> newType = newOne.getRawParameterType(0);
                    if (oldType == newType) {
                        if (oldOne.getDeclaringClass() == newOne.getDeclaringClass()) {
                            throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                                    +" creators: already had implicitly discovered "+oldOne+", encountered "+newOne);
                        }
                    } else if (newType.isAssignableFrom(oldType)) {
                        return;
                    }
                }
            }
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }