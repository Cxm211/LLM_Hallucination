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
            if (explicit && (_explicitCreators & mask) != 0) {
                if (oldOne.equals(newOne)) {
                    return;
                }
                throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                        +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
            }
        }
    }
    if (explicit) {
        _explicitCreators |= mask;
    }
    _creators[typeIndex] = _fixAccess(newOne);
}