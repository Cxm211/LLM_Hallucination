public long roundCeiling(long instant) {
    if (iTimeField) {
        int offset = getOffsetToAdd(instant);
        instant = iField.roundCeiling(instant + offset);
        return iZone.convertLocalToUTC(instant, false);
    } else {
        long localInstant = iZone.convertUTCToLocal(instant);
        localInstant = iField.roundCeiling(localInstant);
        return iZone.convertLocalToUTC(localInstant, false);
    }
}