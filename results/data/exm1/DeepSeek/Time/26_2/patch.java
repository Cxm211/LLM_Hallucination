public long add(long instant, int value) {
    long localInstant = iZone.convertUTCToLocal(instant);
    localInstant = iField.add(localInstant, value);
    return iZone.convertLocalToUTC(localInstant, false);
}