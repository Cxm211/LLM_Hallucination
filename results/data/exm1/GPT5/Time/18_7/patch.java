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
            // Try as Gregorian first.
            instant = iGregorianChronology.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
        } catch (IllegalArgumentException ex) {
            // If Gregorian fields are invalid, fall back to Julian.
            long jInstant = iJulianChronology.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
            if (jInstant >= iCutoverMillis) {
                throw new IllegalArgumentException("Specified date does not exist");
            }
            return jInstant;
        }

        if (instant < iCutoverMillis) {
            // Maybe it's Julian.
            long jInstant = iJulianChronology.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
            if (jInstant >= iCutoverMillis) {
                // Okay, it's in the illegal cutover gap.
                throw new IllegalArgumentException("Specified date does not exist");
            }
            return jInstant;
        }
        return instant;
    }