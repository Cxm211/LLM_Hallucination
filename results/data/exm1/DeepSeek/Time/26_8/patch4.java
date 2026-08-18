public long set(long instant, String text, Locale locale) {
    long localInstant = iZone.convertUTCToLocal(instant);
    localInstant = iField.set(localInstant, text, locale);
    return iZone.convertLocalToUTC(localInstant, false, iZone.getOffset(instant));
}