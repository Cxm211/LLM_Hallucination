public long roundFloor(long instant) {
    long localInstant = iZone.convertUTCToLocal(instant);
    localInstant = iField.roundFloor(localInstant);
    return iZone.convertLocalToUTC(localInstant, false);
}