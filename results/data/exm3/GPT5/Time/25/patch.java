public int getOffsetFromLocal(long instantLocal) {
        // get the offset at instantLocal (first estimate)
        final int offsetLocal = getOffset(instantLocal);
        // adjust instantLocal using the estimate and recalc the offset
        final long instantAdjusted = instantLocal - offsetLocal;
        final int offsetAdjusted = getOffset(instantAdjusted);
        // if the offsets differ, we must be near a DST boundary
        if (offsetLocal != offsetAdjusted) {
            // If this is an overlap (fall-back), prefer the earlier offset (larger value)
            if (offsetLocal > offsetAdjusted) {
                return offsetLocal;
            }
            // Otherwise, for gaps (spring-forward), ensure the time is after the gap
            if ((offsetLocal - offsetAdjusted) < 0) {
                long nextLocal = nextTransition(instantAdjusted);
                long nextAdjusted = nextTransition(instantLocal - offsetAdjusted);
                if (nextLocal != nextAdjusted) {
                    return offsetLocal;
                }
            }
        }
        return offsetAdjusted;
    }