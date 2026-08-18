private int _findOffsetForAdd(int hash)
{
    // first, check the primary:
    int offset = _calcOffset(hash);
    final int[] hashArea = _hashArea;
    if (hashArea[offset+3] == 0) {
        return offset;
    }
    // then secondary
    int offset2 = _secondaryStart + ((hash >> 3) << 2);
    if (hashArea[offset2+3] == 0) {
        return offset2;
    }
    // if not, tertiary?

    offset2 = _tertiaryStart + ((offset >> (_tertiaryShift + 2)) << _tertiaryShift);
    final int bucketSize = (1 << _tertiaryShift);
    for (int end = offset2 + bucketSize; offset2 < end; offset2 += 4) {
        if (hashArea[offset2+3] == 0) {
            return offset2;
        }
    }

    // and if even tertiary full, append at the end of spill area
    offset = _spilloverEnd;
    _spilloverEnd += 4;

    if (_spilloverEnd >= hashArea.length) {
        if (_failOnDoS) {
            _reportTooManyCollisions();
        }
        _needRehash = true;
    }
    return offset;
}