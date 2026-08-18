public long add(long instant, long value) {
    if (instant >= iCutover) {
        instant = iGregorianField.add(instant, value);
        if (instant < iCutover) {
            if (instant + iGapDuration < iCutover) {
                instant = gregorianToJulian(instant);
            }
        }
    } else {
        instant = iJulianField.add(instant, value);
        if (instant >= iCutover) {
            if (instant - iGapDuration >= iCutover) {
                instant = julianToGregorian(instant);
            }
        }
    }
    return instant;
}