public int parseInto(ReadWritableInstant instant, String text, int position) {
        DateTimeParser parser = requireParser();
        if (instant == null) {
            throw new IllegalArgumentException("Instant must not be null");
        }
        
        long instantMillis = instant.getMillis();
        Chronology chrono = instant.getChronology();
        long instantLocal = instantMillis + chrono.getZone().getOffset(instantMillis);
        chrono = selectChronology(chrono);
        int defaultYear = chrono.year().get(instantLocal);
        
        DateTimeParserBucket bucket = new DateTimeParserBucket(
            instantLocal, chrono, iLocale, iPivotYear, defaultYear);
        int newPos = parser.parseInto(bucket, text, position);
        
        // Determine parsed zone/offset and update chronology accordingly before setting millis
        if (iOffsetParsed && bucket.getOffsetInteger() != null) {
            int parsedOffset = bucket.getOffsetInteger();
            DateTimeZone parsedZone = DateTimeZone.forOffsetMillis(parsedOffset);
            chrono = chrono.withZone(parsedZone);
        } else if (bucket.getZone() != null) {
            chrono = chrono.withZone(bucket.getZone());
        }
        
        long parsedMillis = bucket.computeMillis(false, text);
        instant.setChronology(chrono);
        instant.setMillis(parsedMillis);
        if (iZone != null) {
            instant.setZone(iZone);
        }
        return newPos;
    }