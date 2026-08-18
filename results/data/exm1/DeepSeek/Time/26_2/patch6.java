public long roundCeiling(long instant) {
    long localInstant = iZone.convertUTCToLocal(instant);
    localInstant = iField.roundCeiling(localInstant);
    return iZone.convertLocalToUTC(localInstant, false);
}