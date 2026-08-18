private int _findOffsetForAdd(int hash)
    {
        // first, check the primary:
        int offset = _calcOffset(hash);
        final int[] hashArea = _hashArea;
        if (hashArea[offset+3] == 0) {
            return offset;
        }
        // then secondary
        int offset2 = _secondaryStart + ((offset >> 3) << 2);
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
        
        // one caveat: in the unlikely event if spill-over filling up,
        // check if that could be considered a DoS attack; handle appropriately
        // (NOTE: approximate for now; we could verify details if that becomes necessary)
        /* 31-Jul-2015, tatu: Note that spillover area does NOT end at end of array,
         *   since "long names" area follows. Instead, need to calculate from hash size.
         */
        // Ensure spillover does not intrude into long-names area
        if (_spilloverEnd >= _longNameOffset) {
            if (_failOnDoS) {
                _reportTooManyCollisions();
            }
            // and if we didn't fail, we'll simply force rehash for next add
            // (which, in turn, may double up or nuke contents, depending on size etc)
            _needRehash = true;
        }
        return offset;
    }