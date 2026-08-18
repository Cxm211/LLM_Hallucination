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

        // Compute using Julian chronology.
        long julianMillis = iJulianChronology.getDateTimeMillis
            (year, monthOfYear, dayOfMonth,
             hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);

        // Compute the millis for the start of the last valid Julian date (1582-10-04).
        long lastJulianStart = iJulianChronology.getDateTimeMillis(1582, 10, 4, 0, 0, 0, 0);
        long lastJulianEnd = lastJulianStart + DateTimeConstants.MILLIS_PER_DAY;

        if (julianMillis < lastJulianEnd) {
            // Valid Julian date (including the entire day of 1582-10-04).
            return julianMillis;
        }

        if (julianMillis < iCutoverMillis) {
            // The date falls within the cutover gap.
            throw new IllegalArgumentException("Specified date does not exist");
        }

        // Julian millis >= cutover, so compute using Gregorian.
        long gregMillis = iGregorianChronology.getDateTimeMillis
            (year, monthOfYear, dayOfMonth,
             hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
        if (gregMillis >= iCutoverMillis) {
            return gregMillis;
        }
        // This should not happen, but just in case.
        throw new IllegalArgumentException("Specified date does not exist");
    }