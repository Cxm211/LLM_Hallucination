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
        }
        if (oldOne.getClass() == newOne.getClass()) {
            if ((_explicitCreators & mask) != 0 && explicit) {
                throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                        +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
            }
        } else {
            if (oldOne.getClass().isAssignableFrom(newOne.getClass())) {
                // newOne is more specific, replace old
            } else if (newOne.getClass().isAssignableFrom(oldOne.getClass())) {
                // oldOne is more specific, keep old
                return;
            } else {
                // unrelated classes, keep old
                return;
            }
        }
    }
    if (explicit) {
        _explicitCreators |= mask;
    }
    _creators[typeIndex] = _fixAccess(newOne);
}