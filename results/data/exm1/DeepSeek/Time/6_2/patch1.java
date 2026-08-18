public long add(long instant, int value) {
    if (instant >= iCutover) {
        instant = iGregorianField.add(instant, value);
        if (instant < iCutover) {
            if ((value > 0 && instant + iGapDuration < iCutover) ||
                (value < 0 && instant + iGapDuration > iCutover)) {
                instant = gregorianToJulian(instant);
            }
        }
    } else {
        instant = iJulianField.add(instant, value);
        if (instant >= iCutover) {
            if ((value > 0 && instant - iGapDuration >= iCutover) ||
                (value < 0 && instant - iGapDuration <= iCutover)) {
                instant = julianToGregorian(instant);
            }
        }
    }
    return instant;
}