public long roundFloor(long instant) {
    if (iTimeField) {
        int offset = getOffsetToAdd(instant);
        instant = iField.roundFloor(instant + offset);
        return instant - offset;
    } else {
        int offset = iZone.getOffset(instant);
        long localInstant = iZone.convertUTCToLocal(instant);
        localInstant = iField.roundFloor(localInstant);
        return iZone.convertLocalToUTC(localInstant, false, offset);
    }
}