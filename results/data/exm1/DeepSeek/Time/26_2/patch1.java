public long add(long instant, long value) {
    long localInstant = iZone.convertUTCToLocal(instant);
    localInstant = iField.add(localInstant, value);
    return iZone.convertLocalToUTC(localInstant, false);
}