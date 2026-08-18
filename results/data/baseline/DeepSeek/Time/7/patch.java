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
        
        long newLocalMillis = bucket.computeMillis(false, text);
        
        DateTimeZone newZone = chrono.getZone();
        if (iOffsetParsed && bucket.getOffsetInteger() != null) {
            int parsedOffset = bucket.getOffsetInteger();
            newZone = DateTimeZone.forOffsetMillis(parsedOffset);
        } else if (bucket.getZone() != null) {
            newZone = bucket.getZone();
        }
        if (iZone != null) {
            newZone = iZone;
        }
        
        long utcMillis = newLocalMillis - newZone.getOffset(newLocalMillis);
        instant.setMillis(utcMillis);
        instant.setChronology(chrono.withZone(newZone));
        
        return newPos;
    }