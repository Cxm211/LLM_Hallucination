public long set(long instant, int value) {
    long localInstant = iBase.getZone().convertUTCToLocal(instant);
    long difference = FieldUtils.safeSubtract(value, get(instant));
    localInstant = getType().getField(iBase.withUTC()).add(localInstant, difference);
    return iBase.getZone().convertLocalToUTC(localInstant, true);
}