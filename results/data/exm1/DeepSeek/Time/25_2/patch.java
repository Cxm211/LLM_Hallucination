public int getOffsetFromLocal(long instantLocal) {
    final int offsetLocal = getOffset(instantLocal);
    final long instantAdjusted = instantLocal - offsetLocal;
    final int offsetAdjusted = getOffset(instantAdjusted);
    if (offsetLocal != offsetAdjusted) {
        if ((offsetLocal - offsetAdjusted) < 0) {
            long nextLocal = nextTransition(instantAdjusted);
            if (nextLocal != Long.MIN_VALUE) {
                if (instantLocal - offsetLocal < nextLocal) {
                    return offsetLocal;
                }
                return offsetAdjusted;
            }
        } else {
            long prevLocal = previousTransition(instantAdjusted);
            if (prevLocal != Long.MIN_VALUE) {
                if (instantLocal - offsetLocal > prevLocal) {
                    return offsetLocal;
                }
                return offsetAdjusted;
            }
        }
    }
    return offsetAdjusted;
}