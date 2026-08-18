// buggy function
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
        instant.setMillis(bucket.computeMillis(false, text));
        if (iOffsetParsed && bucket.getOffsetInteger() != null) {
            int parsedOffset = bucket.getOffsetInteger();
            DateTimeZone parsedZone = DateTimeZone.forOffsetMillis(parsedOffset);
            chrono = chrono.withZone(parsedZone);
        } else if (bucket.getZone() != null) {
            chrono = chrono.withZone(bucket.getZone());
        }
        instant.setChronology(chrono);
        if (iZone != null) {
            instant.setZone(iZone);
        }
        return newPos;
    }

// trigger testcase
// org/joda/time/format/TestDateTimeFormatter.java::testParseInto_monthDay_feb29_newYork_startOfYear
public void testParseInto_monthDay_feb29_newYork_startOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 1, 0, 0, 0, 0, NEWYORK);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 0, 0, 0, 0, NEWYORK), result);
    }

// org/joda/time/format/TestDateTimeFormatter.java::testParseInto_monthDay_feb29_tokyo_endOfYear
public void testParseInto_monthDay_feb29_tokyo_endOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 12, 31, 23, 59, 59, 999, TOKYO);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 23, 59, 59, 999, TOKYO), result);
    }
