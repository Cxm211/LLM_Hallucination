public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth,
                                  int hourOfDay, int minuteOfHour,
                                  int secondOfMinute, int millisOfSecond)
        throws IllegalArgumentException
    {
        Chronology base;
        if ((base = getBase()) != null) {
            return base.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
        }

        long instant;
        try {
            // Try Gregorian first
            instant = iGregorianChronology.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
            if (instant < iCutoverMillis) {
                // If result is before cutover, recompute using Julian
                long julianInstant = iJulianChronology.getDateTimeMillis
                    (year, monthOfYear, dayOfMonth,
                     hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
                if (julianInstant >= iCutoverMillis) {
                    throw new IllegalArgumentException("Specified date does not exist");
                }
                instant = julianInstant;
            }
        } catch (IllegalArgumentException ex) {
            // Gregorian failed (e.g., 1500-02-29). Try Julian.
            instant = iJulianChronology.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
            if (instant >= iCutoverMillis) {
                // In the illegal cutover gap.
                throw new IllegalArgumentException("Specified date does not exist");
            }
        }
        return instant;
    }