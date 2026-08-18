public int parseInto(ReadWritableInstant instant, String text, int position) {
        DateTimeParser parser = requireParser();
        if (instant == null) {
            throw new IllegalArgumentException("Instant must not be null");
        }
        
        long instantMillis = instant.getMillis();
        Chronology chrono = instant.getChronology();
        long instantLocal = instantMillis + chrono.getZone().getOffset(instantMillis);
        chrono = selectChronology(chrono);
        
        DateTimeParserBucket bucket = new DateTimeParserBucket(
            instantLocal, chrono, iLocale, iPivotYear, iDefaultYear);
        int newPos = parser.parseInto(bucket, text, position);
        if (newPos >= 0) {
            long millis = bucket.computeMillis(false, text);
            // Apply parsed offset/zone to chronology if present
            if (iOffsetParsed && bucket.getOffsetInteger() != null) {
                int parsedOffset = bucket.getOffsetInteger();
                DateTimeZone parsedZone = DateTimeZone.forOffsetMillis(parsedOffset);
                chrono = chrono.withZone(parsedZone);
            } else if (bucket.getZone() != null) {
                chrono = chrono.withZone(bucket.getZone());
            }
            // Convert local millis to UTC using the current chronology's zone
            DateTimeZone zone = chrono.getZone();
            long utcMillis = millis;
            if (zone != null) {
                utcMillis = millis - zone.getOffsetFromLocal(millis);
            }
            instant.setChronology(chrono);
            instant.setMillis(utcMillis);
            if (iZone != null) {
                instant.setZone(iZone);
            }
        }
        return newPos;
    }