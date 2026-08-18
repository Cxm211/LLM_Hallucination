public long addWrapField(long instant, int value) {
    long localInstant = iZone.convertUTCToLocal(instant);
    localInstant = iField.addWrapField(localInstant, value);
    return iZone.convertLocalToUTC(localInstant, false);
}