// buggy code
    public void add(DurationFieldType type, int amount) {
        if (type == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
            setMillis(type.getField(getChronology()).add(getMillis(), amount));
    }

    public void addYears(final int years) {
            setMillis(getChronology().years().add(getMillis(), years));
    }

    public void addWeekyears(final int weekyears) {
            setMillis(getChronology().weekyears().add(getMillis(), weekyears));
    }

    public void addMonths(final int months) {
            setMillis(getChronology().months().add(getMillis(), months));
    }

    public void addWeeks(final int weeks) {
            setMillis(getChronology().weeks().add(getMillis(), weeks));
    }

    public void addDays(final int days) {
            setMillis(getChronology().days().add(getMillis(), days));
    }

    public void addHours(final int hours) {
            setMillis(getChronology().hours().add(getMillis(), hours));
    }

    public void addMinutes(final int minutes) {
            setMillis(getChronology().minutes().add(getMillis(), minutes));
    }

    public void addSeconds(final int seconds) {
            setMillis(getChronology().seconds().add(getMillis(), seconds));
    }

    public void addMillis(final int millis) {
            setMillis(getChronology().millis().add(getMillis(), millis));
    }

// relevant test
// org.joda.time.TestInterval_Basics::testContains_long
    public void testContains_long() {
        assertEquals(false, interval37.contains(2));  
        assertEquals(true,  interval37.contains(3));
        assertEquals(true,  interval37.contains(4));
        assertEquals(true,  interval37.contains(5));
        assertEquals(true,  interval37.contains(6));
        assertEquals(false, interval37.contains(7));  
        assertEquals(false, interval37.contains(8));  
    }

// org.joda.time.TestInterval_Basics::testContains_long_zeroDuration
    public void testContains_long_zeroDuration() {
        assertEquals(false, interval33.contains(2));  
        assertEquals(false, interval33.contains(3));  
        assertEquals(false, interval33.contains(4));  
    }

// org.joda.time.TestInterval_Basics::testContainsNow
    public void testContainsNow() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.containsNow());  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true,  interval37.containsNow());
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(true,  interval37.containsNow());
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(true,  interval37.containsNow());
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.containsNow());  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.containsNow());  
        
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval33.containsNow());  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval33.containsNow());  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval33.containsNow());  
    }

// org.joda.time.TestInterval_Basics::testContains_RI
    public void testContains_RI() {
        assertEquals(false, interval37.contains(new Instant(2)));  
        assertEquals(true,  interval37.contains(new Instant(3)));
        assertEquals(true,  interval37.contains(new Instant(4)));
        assertEquals(true,  interval37.contains(new Instant(5)));
        assertEquals(true,  interval37.contains(new Instant(6)));
        assertEquals(false, interval37.contains(new Instant(7)));  
        assertEquals(false, interval37.contains(new Instant(8)));  
    }

// org.joda.time.TestInterval_Basics::testContains_RI_null
    public void testContains_RI_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.contains((ReadableInstant) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true,  interval37.contains((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(true,  interval37.contains((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(true,  interval37.contains((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.contains((ReadableInstant) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.contains((ReadableInstant) null));  
    }

// org.joda.time.TestInterval_Basics::testContains_RI_zeroDuration
    public void testContains_RI_zeroDuration() {
        assertEquals(false, interval33.contains(new Instant(2)));  
        assertEquals(false, interval33.contains(new Instant(3)));  
        assertEquals(false, interval33.contains(new Instant(4)));  
    }

// org.joda.time.TestInterval_Basics::testContains_RInterval
    public void testContains_RInterval() {
        assertEquals(false, interval37.contains(new Interval(1, 2)));  
        assertEquals(false, interval37.contains(new Interval(2, 2)));  
        
        assertEquals(false, interval37.contains(new Interval(2, 3)));  
        assertEquals(true,  interval37.contains(new Interval(3, 3)));
        
        assertEquals(false, interval37.contains(new Interval(2, 4)));  
        assertEquals(true,  interval37.contains(new Interval(3, 4)));
        assertEquals(true,  interval37.contains(new Interval(4, 4)));
        
        assertEquals(false, interval37.contains(new Interval(2, 6)));  
        assertEquals(true,  interval37.contains(new Interval(3, 6)));
        assertEquals(true,  interval37.contains(new Interval(4, 6)));
        assertEquals(true,  interval37.contains(new Interval(5, 6)));
        assertEquals(true,  interval37.contains(new Interval(6, 6)));
        
        assertEquals(false, interval37.contains(new Interval(2, 7)));  
        assertEquals(true,  interval37.contains(new Interval(3, 7)));
        assertEquals(true,  interval37.contains(new Interval(4, 7)));
        assertEquals(true,  interval37.contains(new Interval(5, 7)));
        assertEquals(true,  interval37.contains(new Interval(6, 7)));
        assertEquals(false, interval37.contains(new Interval(7, 7)));  
        
        assertEquals(false, interval37.contains(new Interval(2, 8)));  
        assertEquals(false, interval37.contains(new Interval(3, 8)));  
        assertEquals(false, interval37.contains(new Interval(4, 8)));  
        assertEquals(false, interval37.contains(new Interval(5, 8)));  
        assertEquals(false, interval37.contains(new Interval(6, 8)));  
        assertEquals(false, interval37.contains(new Interval(7, 8)));  
        assertEquals(false, interval37.contains(new Interval(8, 8)));  
        
        assertEquals(false, interval37.contains(new Interval(8, 9)));  
        assertEquals(false, interval37.contains(new Interval(9, 9)));  
    }

// org.joda.time.TestInterval_Basics::testContains_RInterval_null
    public void testContains_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.contains((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true,  interval37.contains((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(true,  interval37.contains((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(true,  interval37.contains((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.contains((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.contains((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testContains_RInterval_zeroDuration
    public void testContains_RInterval_zeroDuration() {
        assertEquals(false, interval33.contains(interval33));  
        assertEquals(false, interval33.contains(interval37));  
        assertEquals(true,  interval37.contains(interval33));
        assertEquals(false, interval33.contains(new Interval(1, 2)));  
        assertEquals(false, interval33.contains(new Interval(8, 9)));  
        assertEquals(false, interval33.contains(new Interval(1, 9)));  
        
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval33.contains((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval33.contains((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval33.contains((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testOverlaps_RInterval
    public void testOverlaps_RInterval() {
        assertEquals(false, interval37.overlaps(new Interval(1, 2)));  
        assertEquals(false, interval37.overlaps(new Interval(2, 2)));  
        
        assertEquals(false, interval37.overlaps(new Interval(2, 3)));  
        assertEquals(false, interval37.overlaps(new Interval(3, 3)));  
        
        assertEquals(true,  interval37.overlaps(new Interval(2, 4)));
        assertEquals(true,  interval37.overlaps(new Interval(3, 4)));
        assertEquals(true,  interval37.overlaps(new Interval(4, 4)));
        
        assertEquals(true,  interval37.overlaps(new Interval(2, 6)));
        assertEquals(true,  interval37.overlaps(new Interval(3, 6)));
        assertEquals(true,  interval37.overlaps(new Interval(4, 6)));
        assertEquals(true,  interval37.overlaps(new Interval(5, 6)));
        assertEquals(true,  interval37.overlaps(new Interval(6, 6)));
        
        assertEquals(true,  interval37.overlaps(new Interval(2, 7)));
        assertEquals(true,  interval37.overlaps(new Interval(3, 7)));
        assertEquals(true,  interval37.overlaps(new Interval(4, 7)));
        assertEquals(true,  interval37.overlaps(new Interval(5, 7)));
        assertEquals(true,  interval37.overlaps(new Interval(6, 7)));
        assertEquals(false, interval37.overlaps(new Interval(7, 7)));  
        
        assertEquals(true,  interval37.overlaps(new Interval(2, 8)));
        assertEquals(true,  interval37.overlaps(new Interval(3, 8)));
        assertEquals(true,  interval37.overlaps(new Interval(4, 8)));
        assertEquals(true,  interval37.overlaps(new Interval(5, 8)));
        assertEquals(true,  interval37.overlaps(new Interval(6, 8)));
        assertEquals(false, interval37.overlaps(new Interval(7, 8)));  
        assertEquals(false, interval37.overlaps(new Interval(8, 8)));  
        
        assertEquals(false, interval37.overlaps(new Interval(8, 9)));  
        assertEquals(false, interval37.overlaps(new Interval(9, 9)));  
    }

// org.joda.time.TestInterval_Basics::testOverlaps_RInterval_null
    public void testOverlaps_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.overlaps((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.overlaps((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(true,  interval37.overlaps((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(true,  interval37.overlaps((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.overlaps((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.overlaps((ReadableInterval) null));  
        
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval33.overlaps((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testOverlaps_RInterval_zeroDuration
    public void testOverlaps_RInterval_zeroDuration() {
        assertEquals(false, interval33.overlaps(interval33));  
        assertEquals(false, interval33.overlaps(interval37));  
        assertEquals(false, interval37.overlaps(interval33));  
        assertEquals(false, interval33.overlaps(new Interval(1, 2)));
        assertEquals(false, interval33.overlaps(new Interval(8, 9)));
        assertEquals(true,  interval33.overlaps(new Interval(1, 9)));
    }

// org.joda.time.TestInterval_Basics::testOverlap_RInterval
    public void testOverlap_RInterval() {
        assertEquals(null, interval37.overlap(new Interval(1, 2)));  
        assertEquals(null, interval37.overlap(new Interval(2, 2)));  
        
        assertEquals(null, interval37.overlap(new Interval(2, 3)));  
        assertEquals(null, interval37.overlap(new Interval(3, 3)));  
        
        assertEquals(new Interval(3, 4), interval37.overlap(new Interval(2, 4)));  
        assertEquals(new Interval(3, 4), interval37.overlap(new Interval(3, 4)));
        assertEquals(new Interval(4, 4), interval37.overlap(new Interval(4, 4)));
        
        assertEquals(new Interval(3, 7), interval37.overlap(new Interval(2, 7)));  
        assertEquals(new Interval(3, 7), interval37.overlap(new Interval(3, 7)));
        assertEquals(new Interval(4, 7), interval37.overlap(new Interval(4, 7)));
        assertEquals(new Interval(5, 7), interval37.overlap(new Interval(5, 7)));
        assertEquals(new Interval(6, 7), interval37.overlap(new Interval(6, 7)));
        assertEquals(null, interval37.overlap(new Interval(7, 7)));  
        
        assertEquals(new Interval(3, 7), interval37.overlap(new Interval(2, 8)));  
        assertEquals(new Interval(3, 7), interval37.overlap(new Interval(3, 8)));  
        assertEquals(new Interval(4, 7), interval37.overlap(new Interval(4, 8)));  
        assertEquals(new Interval(5, 7), interval37.overlap(new Interval(5, 8)));  
        assertEquals(new Interval(6, 7), interval37.overlap(new Interval(6, 8)));  
        assertEquals(null, interval37.overlap(new Interval(7, 8)));  
        assertEquals(null, interval37.overlap(new Interval(8, 8)));  
    }

// org.joda.time.TestInterval_Basics::testOverlap_RInterval_null
    public void testOverlap_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(null, interval37.overlap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(null, interval37.overlap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(new Interval(4, 4), interval37.overlap((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(new Interval(6, 6), interval37.overlap((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(null, interval37.overlap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(null, interval37.overlap((ReadableInterval) null));  
        
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(null, interval33.overlap((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testOverlap_RInterval_zone
    public void testOverlap_RInterval_zone() {
        Interval testA = new Interval(new DateTime(3, LONDON), new DateTime(7, LONDON));
        assertEquals(ISOChronology.getInstance(LONDON), testA.getChronology());
        
        Interval testB = new Interval(new DateTime(4, MOSCOW), new DateTime(8, MOSCOW));
        assertEquals(ISOChronology.getInstance(MOSCOW), testB.getChronology());
        
        Interval resultAB = testA.overlap(testB);
        assertEquals(ISOChronology.getInstance(LONDON), resultAB.getChronology());
        
        Interval resultBA = testB.overlap(testA);
        assertEquals(ISOChronology.getInstance(MOSCOW), resultBA.getChronology());
    }

// org.joda.time.TestInterval_Basics::testOverlap_RInterval_zoneUTC
    public void testOverlap_RInterval_zoneUTC() {
        Interval testA = new Interval(new Instant(3), new Instant(7));
        assertEquals(ISOChronology.getInstanceUTC(), testA.getChronology());
        
        Interval testB = new Interval(new Instant(4), new Instant(8));
        assertEquals(ISOChronology.getInstanceUTC(), testB.getChronology());
        
        Interval result = testA.overlap(testB);
        assertEquals(ISOChronology.getInstanceUTC(), result.getChronology());
    }

// org.joda.time.TestInterval_Basics::testGap_RInterval
    public void testGap_RInterval() {
        assertEquals(new Interval(1, 3), interval37.gap(new Interval(0, 1)));
        assertEquals(new Interval(1, 3), interval37.gap(new Interval(1, 1)));
        
        assertEquals(null, interval37.gap(new Interval(2, 3)));  
        assertEquals(null, interval37.gap(new Interval(3, 3)));  
        
        assertEquals(null, interval37.gap(new Interval(4, 6)));  
        
        assertEquals(null, interval37.gap(new Interval(3, 7)));  
        assertEquals(null, interval37.gap(new Interval(6, 7)));  
        assertEquals(null, interval37.gap(new Interval(7, 7)));  
        
        assertEquals(null, interval37.gap(new Interval(6, 8)));  
        assertEquals(null, interval37.gap(new Interval(7, 8)));  
        assertEquals(new Interval(7, 8), interval37.gap(new Interval(8, 8)));
        
        assertEquals(null, interval37.gap(new Interval(6, 9)));  
        assertEquals(null, interval37.gap(new Interval(7, 9)));  
        assertEquals(new Interval(7, 8), interval37.gap(new Interval(8, 9)));
        assertEquals(new Interval(7, 9), interval37.gap(new Interval(9, 9)));
    }

// org.joda.time.TestInterval_Basics::testGap_RInterval_null
    public void testGap_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(new Interval(2, 3),  interval37.gap((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(null,  interval37.gap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(null,  interval37.gap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(null,  interval37.gap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(null,  interval37.gap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(new Interval(7, 8),  interval37.gap((ReadableInterval) null));
    }

// org.joda.time.TestInterval_Basics::testGap_RInterval_zone
    public void testGap_RInterval_zone() {
        Interval testA = new Interval(new DateTime(3, LONDON), new DateTime(7, LONDON));
        assertEquals(ISOChronology.getInstance(LONDON), testA.getChronology());
        
        Interval testB = new Interval(new DateTime(1, MOSCOW), new DateTime(2, MOSCOW));
        assertEquals(ISOChronology.getInstance(MOSCOW), testB.getChronology());
        
        Interval resultAB = testA.gap(testB);
        assertEquals(ISOChronology.getInstance(LONDON), resultAB.getChronology());
        
        Interval resultBA = testB.gap(testA);
        assertEquals(ISOChronology.getInstance(MOSCOW), resultBA.getChronology());
    }

// org.joda.time.TestInterval_Basics::testGap_RInterval_zoneUTC
    public void testGap_RInterval_zoneUTC() {
        Interval testA = new Interval(new Instant(3), new Instant(7));
        assertEquals(ISOChronology.getInstanceUTC(), testA.getChronology());
        
        Interval testB = new Interval(new Instant(1), new Instant(2));
        assertEquals(ISOChronology.getInstanceUTC(), testB.getChronology());
        
        Interval result = testA.gap(testB);
        assertEquals(ISOChronology.getInstanceUTC(), result.getChronology());
    }

// org.joda.time.TestInterval_Basics::testAbuts_RInterval
    public void testAbuts_RInterval() {
        assertEquals(false, interval37.abuts(new Interval(1, 2)));  
        assertEquals(false, interval37.abuts(new Interval(2, 2)));  
        
        assertEquals(true,  interval37.abuts(new Interval(2, 3)));
        assertEquals(true,  interval37.abuts(new Interval(3, 3)));
        
        assertEquals(false, interval37.abuts(new Interval(2, 4)));  
        assertEquals(false, interval37.abuts(new Interval(3, 4)));  
        assertEquals(false, interval37.abuts(new Interval(4, 4)));  
        
        assertEquals(false, interval37.abuts(new Interval(2, 6)));  
        assertEquals(false, interval37.abuts(new Interval(3, 6)));  
        assertEquals(false, interval37.abuts(new Interval(4, 6)));  
        assertEquals(false, interval37.abuts(new Interval(5, 6)));  
        assertEquals(false, interval37.abuts(new Interval(6, 6)));  
        
        assertEquals(false, interval37.abuts(new Interval(2, 7)));  
        assertEquals(false, interval37.abuts(new Interval(3, 7)));  
        assertEquals(false, interval37.abuts(new Interval(4, 7)));  
        assertEquals(false, interval37.abuts(new Interval(5, 7)));  
        assertEquals(false, interval37.abuts(new Interval(6, 7)));  
        assertEquals(true,  interval37.abuts(new Interval(7, 7)));
        
        assertEquals(false, interval37.abuts(new Interval(2, 8)));  
        assertEquals(false, interval37.abuts(new Interval(3, 8)));  
        assertEquals(false, interval37.abuts(new Interval(4, 8)));  
        assertEquals(false, interval37.abuts(new Interval(5, 8)));  
        assertEquals(false, interval37.abuts(new Interval(6, 8)));  
        assertEquals(true,  interval37.abuts(new Interval(7, 8)));
        assertEquals(false, interval37.abuts(new Interval(8, 8)));  
        
        assertEquals(false, interval37.abuts(new Interval(8, 9)));  
        assertEquals(false, interval37.abuts(new Interval(9, 9)));  
    }

// org.joda.time.TestInterval_Basics::testAbuts_RInterval_null
    public void testAbuts_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false,  interval37.abuts((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true,  interval37.abuts((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false,  interval37.abuts((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false,  interval37.abuts((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(true,  interval37.abuts((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false,  interval37.abuts((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testIsEqual_RI
    public void testIsEqual_RI() {
        assertEquals(false, interval37.isEqual(interval33));
        assertEquals(true, interval37.isEqual(interval37));
    }

// org.joda.time.TestInterval_Basics::testIsBefore_long
    public void testIsBefore_long() {
        assertEquals(false, interval37.isBefore(2));
        assertEquals(false, interval37.isBefore(3));
        assertEquals(false, interval37.isBefore(4));
        assertEquals(false, interval37.isBefore(5));
        assertEquals(false, interval37.isBefore(6));
        assertEquals(true,  interval37.isBefore(7));
        assertEquals(true,  interval37.isBefore(8));
    }

// org.joda.time.TestInterval_Basics::testIsBeforeNow
    public void testIsBeforeNow() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(true, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(true, interval37.isBeforeNow());
    }

// org.joda.time.TestInterval_Basics::testIsBefore_RI
    public void testIsBefore_RI() {
        assertEquals(false, interval37.isBefore(new Instant(2)));
        assertEquals(false, interval37.isBefore(new Instant(3)));
        assertEquals(false, interval37.isBefore(new Instant(4)));
        assertEquals(false, interval37.isBefore(new Instant(5)));
        assertEquals(false, interval37.isBefore(new Instant(6)));
        assertEquals(true,  interval37.isBefore(new Instant(7)));
        assertEquals(true,  interval37.isBefore(new Instant(8)));
    }

// org.joda.time.TestInterval_Basics::testIsBefore_RI_null
    public void testIsBefore_RI_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(true, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(true, interval37.isBefore((ReadableInstant) null));
    }

// org.joda.time.TestInterval_Basics::testIsBefore_RInterval
    public void testIsBefore_RInterval() {
        assertEquals(false, interval37.isBefore(new Interval(Long.MIN_VALUE, 2)));
        assertEquals(false, interval37.isBefore(new Interval(Long.MIN_VALUE, 3)));
        assertEquals(false, interval37.isBefore(new Interval(Long.MIN_VALUE, 4)));
        
        assertEquals(false, interval37.isBefore(new Interval(6, Long.MAX_VALUE)));
        assertEquals(true, interval37.isBefore(new Interval(7, Long.MAX_VALUE)));
        assertEquals(true, interval37.isBefore(new Interval(8, Long.MAX_VALUE)));
    }

// org.joda.time.TestInterval_Basics::testIsBefore_RInterval_null
    public void testIsBefore_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(true, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(true, interval37.isBefore((ReadableInterval) null));
    }

// org.joda.time.TestInterval_Basics::testIsAfter_long
    public void testIsAfter_long() {
        assertEquals(true,  interval37.isAfter(2));
        assertEquals(false, interval37.isAfter(3));
        assertEquals(false, interval37.isAfter(4));
        assertEquals(false, interval37.isAfter(5));
        assertEquals(false, interval37.isAfter(6));
        assertEquals(false, interval37.isAfter(7));
        assertEquals(false, interval37.isAfter(8));
    }

// org.joda.time.TestInterval_Basics::testIsAfterNow
    public void testIsAfterNow() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(true, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.isAfterNow());
    }

// org.joda.time.TestInterval_Basics::testIsAfter_RI
    public void testIsAfter_RI() {
        assertEquals(true,  interval37.isAfter(new Instant(2)));
        assertEquals(false, interval37.isAfter(new Instant(3)));
        assertEquals(false, interval37.isAfter(new Instant(4)));
        assertEquals(false, interval37.isAfter(new Instant(5)));
        assertEquals(false, interval37.isAfter(new Instant(6)));
        assertEquals(false, interval37.isAfter(new Instant(7)));
        assertEquals(false, interval37.isAfter(new Instant(8)));
    }

// org.joda.time.TestInterval_Basics::testIsAfter_RI_null
    public void testIsAfter_RI_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(true, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
    }

// org.joda.time.TestInterval_Basics::testIsAfter_RInterval
    public void testIsAfter_RInterval() {
        assertEquals(true, interval37.isAfter(new Interval(Long.MIN_VALUE, 2)));
        assertEquals(true, interval37.isAfter(new Interval(Long.MIN_VALUE, 3)));
        assertEquals(false, interval37.isAfter(new Interval(Long.MIN_VALUE, 4)));
        
        assertEquals(false, interval37.isAfter(new Interval(6, Long.MAX_VALUE)));
        assertEquals(false, interval37.isAfter(new Interval(7, Long.MAX_VALUE)));
        assertEquals(false, interval37.isAfter(new Interval(8, Long.MAX_VALUE)));
    }

// org.joda.time.TestInterval_Basics::testIsAfter_RInterval_null
    public void testIsAfter_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(true, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.isAfter((ReadableInterval) null));
    }

// org.joda.time.TestInterval_Basics::testToInterval1
    public void testToInterval1() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval result = test.toInterval();
        assertSame(test, result);
    }

// org.joda.time.TestInterval_Basics::testToMutableInterval1
    public void testToMutableInterval1() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        MutableInterval result = test.toMutableInterval();
        assertEquals(test, result);
    }

// org.joda.time.TestInterval_Basics::testToPeriod
    public void testToPeriod() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, COPTIC_PARIS);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, COPTIC_PARIS);
        Interval base = new Interval(dt1, dt2);
        
        Period test = base.toPeriod();
        Period expected = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(expected, test);
    }

// org.joda.time.TestInterval_Basics::testToPeriod_PeriodType1
    public void testToPeriod_PeriodType1() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, COPTIC_PARIS);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, COPTIC_PARIS);
        Interval base = new Interval(dt1, dt2);
        
        Period test = base.toPeriod(null);
        Period expected = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(expected, test);
    }

// org.joda.time.TestInterval_Basics::testToPeriod_PeriodType2
    public void testToPeriod_PeriodType2() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18);
        Interval base = new Interval(dt1, dt2);
        
        Period test = base.toPeriod(PeriodType.yearWeekDayTime());
        Period expected = new Period(dt1, dt2, PeriodType.yearWeekDayTime());
        assertEquals(expected, test);
    }

// org.joda.time.TestInterval_Basics::testSerialization
    public void testSerialization() throws Exception {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Interval result = (Interval) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestInterval_Basics::testToString
    public void testToString() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, DateTimeZone.UTC);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, DateTimeZone.UTC);
        Interval test = new Interval(dt1, dt2);
        assertEquals("2004-06-09T07:08:09.010Z/2005-08-13T12:14:16.018Z", test.toString());
    }

// org.joda.time.TestInterval_Basics::testToString_reparse
    public void testToString_reparse() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, DateTimeZone.getDefault());
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, DateTimeZone.getDefault());
        Interval test = new Interval(dt1, dt2);
        assertEquals(test, new Interval(test.toString()));
    }

// org.joda.time.TestInterval_Basics::testWithChronology1
    public void testWithChronology1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withChronology(BuddhistChronology.getInstance());
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2, BuddhistChronology.getInstance()), test);
    }

// org.joda.time.TestInterval_Basics::testWithChronology2
    public void testWithChronology2() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withChronology(null);
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2, ISOChronology.getInstance()), test);
    }

// org.joda.time.TestInterval_Basics::testWithChronology3
    public void testWithChronology3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withChronology(COPTIC_PARIS);
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithStartMillis_long1
    public void testWithStartMillis_long1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withStartMillis(TEST_TIME1 - 1);
        assertEquals(new Interval(TEST_TIME1 - 1, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithStartMillis_long2
    public void testWithStartMillis_long2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withStartMillis(TEST_TIME2 + 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithStartMillis_long3
    public void testWithStartMillis_long3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withStartMillis(TEST_TIME1);
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithStartInstant_RI1
    public void testWithStartInstant_RI1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withStart(new Instant(TEST_TIME1 - 1));
        assertEquals(new Interval(TEST_TIME1 - 1, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithStartInstant_RI2
    public void testWithStartInstant_RI2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withStart(new Instant(TEST_TIME2 + 1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithStartInstant_RI3
    public void testWithStartInstant_RI3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withStart(null);
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithEndMillis_long1
    public void testWithEndMillis_long1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEndMillis(TEST_TIME2 - 1);
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2 - 1, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithEndMillis_long2
    public void testWithEndMillis_long2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withEndMillis(TEST_TIME1 - 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithEndMillis_long3
    public void testWithEndMillis_long3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEndMillis(TEST_TIME2);
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithEndInstant_RI1
    public void testWithEndInstant_RI1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEnd(new Instant(TEST_TIME2 - 1));
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2 - 1, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithEndInstant_RI2
    public void testWithEndInstant_RI2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withEnd(new Instant(TEST_TIME1 - 1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithEndInstant_RI3
    public void testWithEndInstant_RI3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEnd(null);
        assertEquals(new Interval(TEST_TIME1, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart1
    public void testWithDurationAfterStart1() throws Throwable {
        Duration dur = new Duration(TEST_TIME2 - TEST_TIME_NOW);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS);
        Interval test = base.withDurationAfterStart(dur);
        
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart2
    public void testWithDurationAfterStart2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationAfterStart(null);
        
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart3
    public void testWithDurationAfterStart3() throws Throwable {
        Duration dur = new Duration(-1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withDurationAfterStart(dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart4
    public void testWithDurationAfterStart4() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationAfterStart(base.toDuration());
        
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd1
    public void testWithDurationBeforeEnd1() throws Throwable {
        Duration dur = new Duration(TEST_TIME_NOW - TEST_TIME1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS);
        Interval test = base.withDurationBeforeEnd(dur);
        
        assertEquals(new Interval(TEST_TIME1, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd2
    public void testWithDurationBeforeEnd2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationBeforeEnd(null);
        
        assertEquals(new Interval(TEST_TIME2, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd3
    public void testWithDurationBeforeEnd3() throws Throwable {
        Duration dur = new Duration(-1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withDurationBeforeEnd(dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd4
    public void testWithDurationBeforeEnd4() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationBeforeEnd(base.toDuration());
        
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodAfterStart1
    public void testWithPeriodAfterStart1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, COPTIC_PARIS);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        
        Interval base = new Interval(dt, dt);
        Interval test = base.withPeriodAfterStart(dur);
        assertEquals(new Interval(dt, dur), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodAfterStart2
    public void testWithPeriodAfterStart2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withPeriodAfterStart(null);
        
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodAfterStart3
    public void testWithPeriodAfterStart3() throws Throwable {
        Period per = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withPeriodAfterStart(per);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithPeriodBeforeEnd1
    public void testWithPeriodBeforeEnd1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, COPTIC_PARIS);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        
        Interval base = new Interval(dt, dt);
        Interval test = base.withPeriodBeforeEnd(dur);
        assertEquals(new Interval(dur, dt), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodBeforeEnd2
    public void testWithPeriodBeforeEnd2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withPeriodBeforeEnd(null);
        
        assertEquals(new Interval(TEST_TIME2, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodBeforeEnd3
    public void testWithPeriodBeforeEnd3() throws Throwable {
        Period per = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withPeriodBeforeEnd(per);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40);
        assertEquals(1970, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(2, test.get(DateTimeFieldType.dayOfWeek()));
        assertEquals(160, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(24, test.get(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(1970, test.get(DateTimeFieldType.weekyear()));
        assertEquals(10, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(30, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(40, test.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(MILLIS_OF_DAY_UTC / 60000 , test.get(DateTimeFieldType.minuteOfDay()));
        assertEquals(MILLIS_OF_DAY_UTC / 1000 , test.get(DateTimeFieldType.secondOfDay()));
        assertEquals(MILLIS_OF_DAY_UTC , test.get(DateTimeFieldType.millisOfDay()));
        assertEquals(10, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        
        test = new LocalDateTime(1970, 6, 9, 12, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalDateTime(1970, 6, 9, 14, 30);
        assertEquals(2, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(2, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(14, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalDateTime(1970, 6, 9, 0, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(24, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testSize
    public void testSize() {
        LocalDateTime test = new LocalDateTime();
        assertEquals(4, test.size());
    }

// org.joda.time.TestLocalDateTime_Basics::testGetFieldType_int
    public void testGetFieldType_int() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        assertSame(DateTimeFieldType.year(), test.getFieldType(0));
        assertSame(DateTimeFieldType.monthOfYear(), test.getFieldType(1));
        assertSame(DateTimeFieldType.dayOfMonth(), test.getFieldType(2));
        assertSame(DateTimeFieldType.millisOfDay(), test.getFieldType(3));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.year(), fields[0]);
        assertSame(DateTimeFieldType.monthOfYear(), fields[1]);
        assertSame(DateTimeFieldType.dayOfMonth(), fields[2]);
        assertSame(DateTimeFieldType.millisOfDay(), fields[3]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestLocalDateTime_Basics::testGetField_int
    public void testGetField_int() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        assertSame(COPTIC_UTC.year(), test.getField(0));
        assertSame(COPTIC_UTC.monthOfYear(), test.getField(1));
        assertSame(COPTIC_UTC.dayOfMonth(), test.getField(2));
        assertSame(COPTIC_UTC.millisOfDay(), test.getField(3));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGetFields
    public void testGetFields() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertSame(COPTIC_UTC.year(), fields[0]);
        assertSame(COPTIC_UTC.monthOfYear(), fields[1]);
        assertSame(COPTIC_UTC.dayOfMonth(), fields[2]);
        assertSame(COPTIC_UTC.millisOfDay(), fields[3]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestLocalDateTime_Basics::testGetValue_int
    public void testGetValue_int() {
        LocalDateTime test = new LocalDateTime(ISO_UTC);
        assertEquals(1970, test.getValue(0));
        assertEquals(6, test.getValue(1));
        assertEquals(9, test.getValue(2));
        assertEquals(MILLIS_OF_DAY_UTC, test.getValue(3));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGetValues
    public void testGetValues() {
        LocalDateTime test = new LocalDateTime(ISO_UTC);
        int[] values = test.getValues();
        assertEquals(1970, values[0]);
        assertEquals(6, values[1]);
        assertEquals(9, values[2]);
        assertEquals(MILLIS_OF_DAY_UTC, values[3]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestLocalDateTime_Basics::testIsSupported_DateTimeFieldType
    public void testIsSupported_DateTimeFieldType() {
        LocalDateTime test = new LocalDateTime();
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfWeek()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.centuryOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.era()));
        
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfSecond()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.halfdayOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfDay()));
        
        assertEquals(false, test.isSupported((DateTimeFieldType) null));
    }

// org.joda.time.TestLocalDateTime_Basics::testIsSupported_DurationFieldType
    public void testIsSupported_DurationFieldType() {
        LocalDateTime test = new LocalDateTime();
        assertEquals(false, test.isSupported(DurationFieldType.eras()));
        assertEquals(true, test.isSupported(DurationFieldType.centuries()));
        assertEquals(true, test.isSupported(DurationFieldType.years()));
        assertEquals(true, test.isSupported(DurationFieldType.months()));
        assertEquals(true, test.isSupported(DurationFieldType.weekyears()));
        assertEquals(true, test.isSupported(DurationFieldType.weeks()));
        assertEquals(true, test.isSupported(DurationFieldType.days()));
        
        assertEquals(true, test.isSupported(DurationFieldType.hours()));
        assertEquals(true, test.isSupported(DurationFieldType.minutes()));
        assertEquals(true, test.isSupported(DurationFieldType.seconds()));
        assertEquals(true, test.isSupported(DurationFieldType.millis()));
        assertEquals(true, test.isSupported(DurationFieldType.halfdays()));
        
        assertEquals(false, test.isSupported((DurationFieldType) null));
    }

// org.joda.time.TestLocalDateTime_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        LocalDateTime test1 = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        LocalDateTime test2 = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        LocalDateTime test3 = new LocalDateTime(1971, 6, 9, 10, 20, 30, 40);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        Partial partial = new Partial(
                new DateTimeFieldType[] {
                        DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(),
                        DateTimeFieldType.dayOfMonth(), DateTimeFieldType.millisOfDay()},
                new int[] {1970, 6, 9, MILLIS_OF_DAY_UTC}, COPTIC_PARIS);
        assertEquals(true, test1.equals(partial));
        assertEquals(true, test1.hashCode() == partial.hashCode());
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestLocalDateTime_Basics::testCompareTo
    public void testCompareTo() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.millisOfDay(),
        };
        int[] values = new int[] {2005, 6, 2, MILLIS_OF_DAY_UTC};
        Partial p = new Partial(types, values);
        assertEquals(0, test1.compareTo(p));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

        try {
            @SuppressWarnings("deprecation")
            YearMonthDay ymd = new YearMonthDay();
            test1.compareTo(ymd);
            fail();
        } catch (ClassCastException ex) {}
        try {
            @SuppressWarnings("deprecation")
            TimeOfDay tod = new TimeOfDay();
            test1.compareTo(tod);
            fail();
        } catch (ClassCastException ex) {}
        Partial partial = new Partial()
            .with(DateTimeFieldType.centuryOfEra(), 1)
            .with(DateTimeFieldType.halfdayOfDay(), 0)
            .with(DateTimeFieldType.dayOfMonth(), 9);
        try {
            new LocalDateTime(1970, 6, 9, 10, 20, 30, 40).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testIsEqual_LocalDateTime
    public void testIsEqual_LocalDateTime() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new LocalDateTime(2005, 7, 2, 10, 20, 30, 40).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testIsBefore_LocalDateTime
    public void testIsBefore_LocalDateTime() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new LocalDateTime(2005, 7, 2, 10, 20, 30, 40).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testIsAfter_LocalDateTime
    public void testIsAfter_LocalDateTime() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new LocalDateTime(2005, 7, 2, 10, 20, 30, 40).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithDate
    public void testWithDate() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withDate(2006, 2, 1);
        
        check(test, 2004, 6, 9, 10, 20, 30, 40);
        check(result, 2006, 2, 1, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithTime
    public void testWithTime() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withTime(9, 8, 7, 6);
        
        check(test, 2004, 6, 9, 10, 20, 30, 40);
        check(result, 2004, 6, 9, 9, 8, 7, 6);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithField_DateTimeFieldType_int_1
    public void testWithField_DateTimeFieldType_int_1() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 40), test);
        assertEquals(new LocalDateTime(2006, 6, 9, 10, 20, 30, 40), result);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithField_DateTimeFieldType_int_2
    public void testWithField_DateTimeFieldType_int_2() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithField_DateTimeFieldType_int_3
    public void testWithField_DateTimeFieldType_int_3() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withField(DateTimeFieldType.year(), 2004);
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 40), test);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_1
    public void testWithFieldAdded_DurationFieldType_int_1() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 40), test);
        assertEquals(new LocalDateTime(2010, 6, 9, 10, 20, 30, 40), result);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_2
    public void testWithFieldAdded_DurationFieldType_int_2() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_3
    public void testWithFieldAdded_DurationFieldType_int_3() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_4
    public void testWithFieldAdded_DurationFieldType_int_4() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlus_RP
    public void testPlus_RP() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plus(new Period(1, 2, 3, 4, 29, 6, 7, 8));
        LocalDateTime expected = new LocalDateTime(2003, 7, 29, 15, 26, 37, 48, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusYears_int
    public void testPlusYears_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusYears(1);
        LocalDateTime expected = new LocalDateTime(2003, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusMonths(1);
        LocalDateTime expected = new LocalDateTime(2002, 6, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusWeeks_int
    public void testPlusWeeks_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusWeeks(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 10, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusDays_int
    public void testPlusDays_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusDays(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 4, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusHours_int
    public void testPlusHours_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusHours(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 11, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusMinutes_int
    public void testPlusMinutes_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusMinutes(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 21, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusSeconds_int
    public void testPlusSeconds_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusSeconds(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 31, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusMillis_int
    public void testPlusMillis_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusMillis(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 30, 41, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinus_RP
    public void testMinus_RP() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        
        LocalDateTime expected = new LocalDateTime(2001, 3, 26, 9, 19, 29, 39, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusYears_int
    public void testMinusYears_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusYears(1);
        LocalDateTime expected = new LocalDateTime(2001, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusMonths(1);
        LocalDateTime expected = new LocalDateTime(2002, 4, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusWeeks_int
    public void testMinusWeeks_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusWeeks(1);
        LocalDateTime expected = new LocalDateTime(2002, 4, 26, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusDays_int
    public void testMinusDays_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusDays(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 2, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusHours_int
    public void testMinusHours_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusHours(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 9, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusMinutes_int
    public void testMinusMinutes_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusMinutes(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 19, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusSeconds_int
    public void testMinusSeconds_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusSeconds(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 29, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusMillis_int
    public void testMinusMillis_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusMillis(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 30, 39, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testGetters
    public void testGetters() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, GJ_UTC);
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(160, test.getDayOfYear());
        assertEquals(2, test.getDayOfWeek());
        assertEquals(24, test.getWeekOfWeekyear());
        assertEquals(1970, test.getWeekyear());
        assertEquals(70, test.getYearOfCentury());
        assertEquals(20, test.getCenturyOfEra());
        assertEquals(1970, test.getYearOfEra());
        assertEquals(DateTimeConstants.AD, test.getEra());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(MILLIS_OF_DAY_UTC, test.getMillisOfDay());
    }

// org.joda.time.TestLocalDateTime_Basics::testWithers
    public void testWithers() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, GJ_UTC);
        check(test.withYear(2000), 2000, 6, 9, 10, 20, 30, 40);
        check(test.withMonthOfYear(2), 1970, 2, 9, 10, 20, 30, 40);
        check(test.withDayOfMonth(2), 1970, 6, 2, 10, 20, 30, 40);
        check(test.withDayOfYear(6), 1970, 1, 6, 10, 20, 30, 40);
        check(test.withDayOfWeek(6), 1970, 6, 13, 10, 20, 30, 40);
        check(test.withWeekOfWeekyear(6), 1970, 2, 3, 10, 20, 30, 40);
        check(test.withWeekyear(1971), 1971, 6, 15, 10, 20, 30, 40);
        check(test.withYearOfCentury(60), 1960, 6, 9, 10, 20, 30, 40);
        check(test.withCenturyOfEra(21), 2070, 6, 9, 10, 20, 30, 40);
        check(test.withYearOfEra(1066), 1066, 6, 9, 10, 20, 30, 40);
        check(test.withEra(DateTimeConstants.BC), -1970, 6, 9, 10, 20, 30, 40);
        check(test.withHourOfDay(6), 1970, 6, 9, 6, 20, 30, 40);
        check(test.withMinuteOfHour(6), 1970, 6, 9, 10, 6, 30, 40);
        check(test.withSecondOfMinute(6), 1970, 6, 9, 10, 20, 6, 40);
        check(test.withMillisOfSecond(6), 1970, 6, 9, 10, 20, 30, 6);
        check(test.withMillisOfDay(61234), 1970, 6, 9, 0, 1, 1, 234);
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime
    public void testToDateTime() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        
        DateTime test = base.toDateTime();
        check(base, 2005, 6, 9, 6, 7, 8, 9);
        DateTime expected = new DateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_Zone
    public void testToDateTime_Zone() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        
        DateTime test = base.toDateTime(TOKYO);
        check(base, 2005, 6, 9, 6, 7, 8, 9);
        DateTime expected = new DateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_nullZone
    public void testToDateTime_nullZone() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        
        DateTime test = base.toDateTime((DateTimeZone) null);
        check(base, 2005, 6, 9, 6, 7, 8, 9);
        DateTime expected = new DateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToLocalDate
    public void testToLocalDate() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        LocalDate expected = new LocalDate(2005, 6, 9, COPTIC_LONDON);
        assertEquals(expected,base.toLocalDate());
    }

// org.joda.time.TestLocalDateTime_Basics::testToLocalTime
    public void testToLocalTime() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        LocalTime expected = new LocalTime(6, 7, 8, 9, COPTIC_LONDON);
        assertEquals(expected,base.toLocalTime());
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7, BUDDHIST_TOKYO);
        
        DateTime test = base.toDateTime(dt);
        check(base, 2005, 6, 9, 10, 20, 30, 40);
        DateTime expected = new DateTime(2005, 6, 9, 10, 20, 30, 40, BUDDHIST_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 2005, 6, 9, 10, 20, 30, 40);
        DateTime expected = new DateTime(2005, 6, 9, 10, 20, 30, 40, ISO_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_summer
    public void testToDate_summer() {
        LocalDateTime base = new LocalDateTime(2005, 7, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 7, 9, 10, 20, 30, 40);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JULY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        gcal.set(Calendar.HOUR_OF_DAY, 10);
        gcal.set(Calendar.MINUTE, 20);
        gcal.set(Calendar.SECOND, 30);
        gcal.set(Calendar.MILLISECOND, 40);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_winter
    public void testToDate_winter() {
        LocalDateTime base = new LocalDateTime(2005, 1, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 1, 9, 10, 20, 30, 40);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JANUARY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        gcal.set(Calendar.HOUR_OF_DAY, 10);
        gcal.set(Calendar.MINUTE, 20);
        gcal.set(Calendar.SECOND, 30);
        gcal.set(Calendar.MILLISECOND, 40);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_springDST
    public void testToDate_springDST() {
        LocalDateTime base = new LocalDateTime(2007, 4, 2, 0, 20, 0, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2, 0, 20, 0, 0);
            assertEquals("Mon Apr 02 01:00:00 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_springDST_2Hour40Savings
    public void testToDate_springDST_2Hour40Savings() {
        LocalDateTime base = new LocalDateTime(2007, 4, 2, 0, 20, 0, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000, (3600000 / 6) * 16);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2, 0, 20, 0, 0);
            assertEquals("Mon Apr 02 02:40:00 GMT+03:40 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_autumnDST
    public void testToDate_autumnDST() {
        LocalDateTime base = new LocalDateTime(2007, 10, 2, 0, 20, 30, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 10, 2, 0, 20, 30, 0);
            assertEquals("Tue Oct 02 00:20:30 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_summer_Zone
    public void testToDate_summer_Zone() {
        LocalDateTime base = new LocalDateTime(2005, 7, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        Date test = base.toDate(TimeZone.getDefault());
        check(base, 2005, 7, 9, 10, 20, 30, 40);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JULY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        gcal.set(Calendar.HOUR_OF_DAY, 10);
        gcal.set(Calendar.MINUTE, 20);
        gcal.set(Calendar.SECOND, 30);
        gcal.set(Calendar.MILLISECOND, 40);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_winter_Zone
    public void testToDate_winter_Zone() {
        LocalDateTime base = new LocalDateTime(2005, 1, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        Date test = base.toDate(TimeZone.getDefault());
        check(base, 2005, 1, 9, 10, 20, 30, 40);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JANUARY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        gcal.set(Calendar.HOUR_OF_DAY, 10);
        gcal.set(Calendar.MINUTE, 20);
        gcal.set(Calendar.SECOND, 30);
        gcal.set(Calendar.MILLISECOND, 40);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_springDST_Zone
    public void testToDate_springDST_Zone() {
        LocalDateTime base = new LocalDateTime(2007, 4, 2, 0, 20, 0, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate(TimeZone.getDefault());
            check(base, 2007, 4, 2, 0, 20, 0, 0);
            assertEquals("Mon Apr 02 01:00:00 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_springDST_2Hour40Savings_Zone
    public void testToDate_springDST_2Hour40Savings_Zone() {
        LocalDateTime base = new LocalDateTime(2007, 4, 2, 0, 20, 0, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000, (3600000 / 6) * 16);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate(TimeZone.getDefault());
            check(base, 2007, 4, 2, 0, 20, 0, 0);
            assertEquals("Mon Apr 02 02:40:00 GMT+03:40 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_autumnDST_Zone
    public void testToDate_autumnDST_Zone() {
        LocalDateTime base = new LocalDateTime(2007, 10, 2, 0, 20, 30, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate(TimeZone.getDefault());
            check(base, 2007, 10, 2, 0, 20, 30, 0);
            assertEquals("Tue Oct 02 00:20:30 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testProperty
    public void testProperty() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, GJ_UTC);
        assertEquals(test.year(), test.property(DateTimeFieldType.year()));
        assertEquals(test.monthOfYear(), test.property(DateTimeFieldType.monthOfYear()));
        assertEquals(test.dayOfMonth(), test.property(DateTimeFieldType.dayOfMonth()));
        assertEquals(test.dayOfWeek(), test.property(DateTimeFieldType.dayOfWeek()));
        assertEquals(test.dayOfYear(), test.property(DateTimeFieldType.dayOfYear()));
        assertEquals(test.weekOfWeekyear(), test.property(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(test.weekyear(), test.property(DateTimeFieldType.weekyear()));
        assertEquals(test.yearOfCentury(), test.property(DateTimeFieldType.yearOfCentury()));
        assertEquals(test.yearOfEra(), test.property(DateTimeFieldType.yearOfEra()));
        assertEquals(test.centuryOfEra(), test.property(DateTimeFieldType.centuryOfEra()));
        assertEquals(test.era(), test.property(DateTimeFieldType.era()));
        assertEquals(test.hourOfDay(), test.property(DateTimeFieldType.hourOfDay()));
        assertEquals(test.minuteOfHour(), test.property(DateTimeFieldType.minuteOfHour()));
        assertEquals(test.secondOfMinute(), test.property(DateTimeFieldType.secondOfMinute()));
        assertEquals(test.millisOfSecond(), test.property(DateTimeFieldType.millisOfSecond()));
        assertEquals(test.millisOfDay(), test.property(DateTimeFieldType.millisOfDay()));
        
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(test, test.property(DateTimeFieldType.minuteOfDay()).getLocalDateTime());
    }

// org.joda.time.TestLocalDateTime_Basics::testSerialization
    public void testSerialization() throws Exception {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDateTime result = (LocalDateTime) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
        assertTrue(result.isSupported(DateTimeFieldType.dayOfMonth()));  
    }

// org.joda.time.TestLocalDateTime_Basics::testToString
    public void testToString() {
        LocalDateTime test = new LocalDateTime(2002, 6, 9, 10, 20, 30, 40);
        assertEquals("2002-06-09T10:20:30.040", test.toString());
    }

// org.joda.time.TestLocalDateTime_Basics::testToString_String
    public void testToString_String() {
        LocalDateTime test = new LocalDateTime(2002, 6, 9, 10, 20, 30, 40);
        assertEquals("2002 10", test.toString("yyyy HH"));
        assertEquals("2002-06-09T10:20:30.040", test.toString((String) null));
    }

// org.joda.time.TestLocalDateTime_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40);
        assertEquals("Tue 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("mar. 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("1970-06-09T10:20:30.040", test.toString(null, Locale.ENGLISH));
        assertEquals("Tue 9/6", test.toString("EEE d/M", null));
        assertEquals("1970-06-09T10:20:30.040", test.toString(null, null));
    }

// org.joda.time.TestLocalDateTime_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        LocalDateTime test = new LocalDateTime(2002, 6, 9, 10, 20, 30, 40);
        assertEquals("2002 10", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09T10:20:30.040", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestLocalDate_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        LocalDate test = new LocalDate();
        assertEquals(1970, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(2, test.get(DateTimeFieldType.dayOfWeek()));
        assertEquals(160, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(24, test.get(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(1970, test.get(DateTimeFieldType.weekyear()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.hourOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testSize
    public void testSize() {
        LocalDate test = new LocalDate();
        assertEquals(3, test.size());
    }

// org.joda.time.TestLocalDate_Basics::testGetFieldType_int
    public void testGetFieldType_int() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
        assertSame(DateTimeFieldType.year(), test.getFieldType(0));
        assertSame(DateTimeFieldType.monthOfYear(), test.getFieldType(1));
        assertSame(DateTimeFieldType.dayOfMonth(), test.getFieldType(2));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.year(), fields[0]);
        assertSame(DateTimeFieldType.monthOfYear(), fields[1]);
        assertSame(DateTimeFieldType.dayOfMonth(), fields[2]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestLocalDate_Basics::testGetField_int
    public void testGetField_int() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
        assertSame(COPTIC_UTC.year(), test.getField(0));
        assertSame(COPTIC_UTC.monthOfYear(), test.getField(1));
        assertSame(COPTIC_UTC.dayOfMonth(), test.getField(2));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testGetFields
    public void testGetFields() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertSame(COPTIC_UTC.year(), fields[0]);
        assertSame(COPTIC_UTC.monthOfYear(), fields[1]);
        assertSame(COPTIC_UTC.dayOfMonth(), fields[2]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestLocalDate_Basics::testGetValue_int
    public void testGetValue_int() {
        LocalDate test = new LocalDate();
        assertEquals(1970, test.getValue(0));
        assertEquals(6, test.getValue(1));
        assertEquals(9, test.getValue(2));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testGetValues
    public void testGetValues() {
        LocalDate test = new LocalDate();
        int[] values = test.getValues();
        assertEquals(1970, values[0]);
        assertEquals(6, values[1]);
        assertEquals(9, values[2]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestLocalDate_Basics::testIsSupported_DateTimeFieldType
    public void testIsSupported_DateTimeFieldType() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfWeek()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.centuryOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.era()));
        assertEquals(false, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(false, test.isSupported((DateTimeFieldType) null));
    }

// org.joda.time.TestLocalDate_Basics::testIsSupported_DurationFieldType
    public void testIsSupported_DurationFieldType() {
        LocalDate test = new LocalDate(1970, 6, 9);
        assertEquals(false, test.isSupported(DurationFieldType.eras()));
        assertEquals(true, test.isSupported(DurationFieldType.centuries()));
        assertEquals(true, test.isSupported(DurationFieldType.years()));
        assertEquals(true, test.isSupported(DurationFieldType.months()));
        assertEquals(true, test.isSupported(DurationFieldType.weekyears()));
        assertEquals(true, test.isSupported(DurationFieldType.weeks()));
        assertEquals(true, test.isSupported(DurationFieldType.days()));
        
        assertEquals(false, test.isSupported(DurationFieldType.hours()));
        assertEquals(false, test.isSupported((DurationFieldType) null));
    }

// org.joda.time.TestLocalDate_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        LocalDate test1 = new LocalDate(1970, 6, 9, COPTIC_PARIS);
        LocalDate test2 = new LocalDate(1970, 6, 9, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        LocalDate test3 = new LocalDate(1971, 6, 9);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(true, test1.equals(new YearMonthDay(1970, 6, 9, COPTIC_PARIS)));
        assertEquals(true, test1.hashCode() == new YearMonthDay(1970, 6, 9, COPTIC_PARIS).hashCode());
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestLocalDate_Basics::testEqualsHashCodeLenient
    public void testEqualsHashCodeLenient() {
        LocalDate test1 = new LocalDate(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        LocalDate test2 = new LocalDate(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
    }

// org.joda.time.TestLocalDate_Basics::testEqualsHashCodeStrict
    public void testEqualsHashCodeStrict() {
        LocalDate test1 = new LocalDate(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        LocalDate test2 = new LocalDate(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
    }

// org.joda.time.TestLocalDate_Basics::testEqualsHashCodeAPI
    public void testEqualsHashCodeAPI() {
        LocalDate test = new LocalDate(1970, 6, 9, COPTIC_PARIS);
        int expected = 157;
        expected = 23 * expected + 1970;
        expected = 23 * expected + COPTIC_UTC.year().getType().hashCode();
        expected = 23 * expected + 6;
        expected = 23 * expected + COPTIC_UTC.monthOfYear().getType().hashCode();
        expected = 23 * expected + 9;
        expected = 23 * expected + COPTIC_UTC.dayOfMonth().getType().hashCode();
        expected += COPTIC_UTC.hashCode();
        assertEquals(expected, test.hashCode());
    }

// org.joda.time.TestLocalDate_Basics::testCompareTo
    public void testCompareTo() {
        LocalDate test1 = new LocalDate(2005, 6, 2);
        LocalDate test1a = new LocalDate(2005, 6, 2);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        LocalDate test2 = new LocalDate(2005, 7, 2);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        LocalDate test3 = new LocalDate(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth(),
        };
        int[] values = new int[] {2005, 6, 2};
        Partial p = new Partial(types, values);
        assertEquals(0, test1.compareTo(p));
        assertEquals(0, test1.compareTo(new YearMonthDay(2005, 6, 2)));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

        try {
            test1.compareTo(new TimeOfDay());
            fail();
        } catch (ClassCastException ex) {}
        Partial partial = new Partial()
            .with(DateTimeFieldType.centuryOfEra(), 1)
            .with(DateTimeFieldType.halfdayOfDay(), 0)
            .with(DateTimeFieldType.dayOfMonth(), 9);
        try {
            new LocalDate(1970, 6, 9).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testIsEqual_LocalDate
    public void testIsEqual_LocalDate() {
        LocalDate test1 = new LocalDate(2005, 6, 2);
        LocalDate test1a = new LocalDate(2005, 6, 2);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        LocalDate test2 = new LocalDate(2005, 7, 2);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        LocalDate test3 = new LocalDate(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new LocalDate(2005, 7, 2).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testIsBefore_LocalDate
    public void testIsBefore_LocalDate() {
        LocalDate test1 = new LocalDate(2005, 6, 2);
        LocalDate test1a = new LocalDate(2005, 6, 2);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        LocalDate test2 = new LocalDate(2005, 7, 2);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        LocalDate test3 = new LocalDate(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new LocalDate(2005, 7, 2).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testIsAfter_LocalDate
    public void testIsAfter_LocalDate() {
        LocalDate test1 = new LocalDate(2005, 6, 2);
        LocalDate test1a = new LocalDate(2005, 6, 2);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        LocalDate test2 = new LocalDate(2005, 7, 2);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        LocalDate test3 = new LocalDate(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new LocalDate(2005, 7, 2).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithField_DateTimeFieldType_int_1
    public void testWithField_DateTimeFieldType_int_1() {
        LocalDate test = new LocalDate(2004, 6, 9);
        LocalDate result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new LocalDate(2004, 6, 9), test);
        assertEquals(new LocalDate(2006, 6, 9), result);
    }

// org.joda.time.TestLocalDate_Basics::testWithField_DateTimeFieldType_int_2
    public void testWithField_DateTimeFieldType_int_2() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithField_DateTimeFieldType_int_3
    public void testWithField_DateTimeFieldType_int_3() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withField(DateTimeFieldType.hourOfDay(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithField_DateTimeFieldType_int_4
    public void testWithField_DateTimeFieldType_int_4() {
        LocalDate test = new LocalDate(2004, 6, 9);
        LocalDate result = test.withField(DateTimeFieldType.year(), 2004);
        assertEquals(new LocalDate(2004, 6, 9), test);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_1
    public void testWithFieldAdded_DurationFieldType_int_1() {
        LocalDate test = new LocalDate(2004, 6, 9);
        LocalDate result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new LocalDate(2004, 6, 9), test);
        assertEquals(new LocalDate(2010, 6, 9), result);
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_2
    public void testWithFieldAdded_DurationFieldType_int_2() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_3
    public void testWithFieldAdded_DurationFieldType_int_3() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_4
    public void testWithFieldAdded_DurationFieldType_int_4() {
        LocalDate test = new LocalDate(2004, 6, 9);
        LocalDate result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_5
    public void testWithFieldAdded_DurationFieldType_int_5() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withFieldAdded(DurationFieldType.hours(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testPlus_RP
    public void testPlus_RP() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plus(new Period(1, 2, 3, 4, 29, 6, 7, 8));
        LocalDate expected = new LocalDate(2003, 7, 28, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testPlusYears_int
    public void testPlusYears_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plusYears(1);
        LocalDate expected = new LocalDate(2003, 5, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plusMonths(1);
        LocalDate expected = new LocalDate(2002, 6, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testPlusWeeks_int
    public void testPlusWeeks_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plusWeeks(1);
        LocalDate expected = new LocalDate(2002, 5, 10, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testPlusDays_int
    public void testPlusDays_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plusDays(1);
        LocalDate expected = new LocalDate(2002, 5, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinus_RP
    public void testMinus_RP() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        
        
        
        
        LocalDate expected = new LocalDate(2001, 3, 26, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinusYears_int
    public void testMinusYears_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minusYears(1);
        LocalDate expected = new LocalDate(2001, 5, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minusMonths(1);
        LocalDate expected = new LocalDate(2002, 4, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinusWeeks_int
    public void testMinusWeeks_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minusWeeks(1);
        LocalDate expected = new LocalDate(2002, 4, 26, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinusDays_int
    public void testMinusDays_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minusDays(1);
        LocalDate expected = new LocalDate(2002, 5, 2, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testGetters
    public void testGetters() {
        LocalDate test = new LocalDate(1970, 6, 9, GJ_UTC);
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(160, test.getDayOfYear());
        assertEquals(2, test.getDayOfWeek());
        assertEquals(24, test.getWeekOfWeekyear());
        assertEquals(1970, test.getWeekyear());
        assertEquals(70, test.getYearOfCentury());
        assertEquals(20, test.getCenturyOfEra());
        assertEquals(1970, test.getYearOfEra());
        assertEquals(DateTimeConstants.AD, test.getEra());
    }

// org.joda.time.TestLocalDate_Basics::testWithers
    public void testWithers() {
        LocalDate test = new LocalDate(1970, 6, 9, GJ_UTC);
        check(test.withYear(2000), 2000, 6, 9);
        check(test.withMonthOfYear(2), 1970, 2, 9);
        check(test.withDayOfMonth(2), 1970, 6, 2);
        check(test.withDayOfYear(6), 1970, 1, 6);
        check(test.withDayOfWeek(6), 1970, 6, 13);
        check(test.withWeekOfWeekyear(6), 1970, 2, 3);
        check(test.withWeekyear(1971), 1971, 6, 15);
        check(test.withYearOfCentury(60), 1960, 6, 9);
        check(test.withCenturyOfEra(21), 2070, 6, 9);
        check(test.withYearOfEra(1066), 1066, 6, 9);
        check(test.withEra(DateTimeConstants.BC), -1970, 6, 9);
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay
    public void testToDateTimeAtStartOfDay() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtStartOfDay();
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay_avoidDST
    public void testToDateTimeAtStartOfDay_avoidDST() {
        LocalDate base = new LocalDate(2007, 4, 1);
        
        DateTimeZone.setDefault(MOCK_GAZA);
        DateTime test = base.toDateTimeAtStartOfDay();
        check(base, 2007, 4, 1);
        assertEquals(new DateTime(2007, 4, 1, 1, 0, 0, 0, MOCK_GAZA), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay_Zone
    public void testToDateTimeAtStartOfDay_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtStartOfDay(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_TOKYO), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay_Zone_avoidDST
    public void testToDateTimeAtStartOfDay_Zone_avoidDST() {
        LocalDate base = new LocalDate(2007, 4, 1);
        
        DateTime test = base.toDateTimeAtStartOfDay(MOCK_GAZA);
        check(base, 2007, 4, 1);
        assertEquals(new DateTime(2007, 4, 1, 1, 0, 0, 0, MOCK_GAZA), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay_nullZone
    public void testToDateTimeAtStartOfDay_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtStartOfDay((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtMidnight
    public void testToDateTimeAtMidnight() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight();
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtMidnight_Zone
    public void testToDateTimeAtMidnight_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_TOKYO), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtMidnight_nullZone
    public void testToDateTimeAtMidnight_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtCurrentTime
    public void testToDateTimeAtCurrentTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeAtCurrentTime();
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_LONDON);
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtCurrentTime_Zone
    public void testToDateTimeAtCurrentTime_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeAtCurrentTime(TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_TOKYO);
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtCurrentTime_nullZone
    public void testToDateTimeAtCurrentTime_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeAtCurrentTime((DateTimeZone) null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_LONDON);
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToLocalDateTime_LocalTime
    public void testToLocalDateTime_LocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, COPTIC_TOKYO);
        
        LocalDateTime test = base.toLocalDateTime(tod);
        check(base, 2005, 6, 9);
        LocalDateTime expected = new LocalDateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_UTC);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToLocalDateTime_nullLocalTime
    public void testToLocalDateTime_nullLocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        
        try {
            base.toLocalDateTime((LocalTime) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestLocalDate_Basics::testToLocalDateTime_wrongChronologyLocalTime
    public void testToLocalDateTime_wrongChronologyLocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, BUDDHIST_PARIS); 
        
        try {
            base.toLocalDateTime(tod);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_LocalTime
    public void testToDateTime_LocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, COPTIC_TOKYO);
        
        DateTime test = base.toDateTime(tod);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_nullLocalTime
    public void testToDateTime_nullLocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        long now = new DateTime(2004, 5, 8, 12, 13, 14, 15, COPTIC_LONDON).getMillis();
        DateTimeUtils.setCurrentMillisFixed(now);
        
        DateTime test = base.toDateTime((LocalTime) null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_LocalTime_Zone
    public void testToDateTime_LocalTime_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, COPTIC_TOKYO);
        
        DateTime test = base.toDateTime(tod, TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_LocalTime_nullZone
    public void testToDateTime_LocalTime_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, COPTIC_TOKYO);
        
        DateTime test = base.toDateTime(tod, null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_nullLocalTime_Zone
    public void testToDateTime_nullLocalTime_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        long now = new DateTime(2004, 5, 8, 12, 13, 14, 15, COPTIC_TOKYO).getMillis();
        DateTimeUtils.setCurrentMillisFixed(now);
        
        DateTime test = base.toDateTime((LocalTime) null, TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_wrongChronoLocalTime_Zone
    public void testToDateTime_wrongChronoLocalTime_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, BUDDHIST_TOKYO);
        
        try {
            base.toDateTime(tod, LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testToDateMidnight
    public void testToDateMidnight() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight();
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateMidnight_Zone
    public void testToDateMidnight_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_TOKYO), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateMidnight_nullZone
    public void testToDateMidnight_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        
        DateTime test = base.toDateTime(dt);
        check(base, 2005, 6, 9);
        DateTime expected = dt;
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        LocalDate base = new LocalDate(2005, 6, 9);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 2005, 6, 9);
        DateTime expected = dt;
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToInterval
    public void testToInterval() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval();
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTimeAtStartOfDay();
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToInterval_Zone
    public void testToInterval_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval(TOKYO);
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTimeAtStartOfDay(TOKYO);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToInterval_Zone_noMidnight
    public void testToInterval_Zone_noMidnight() {
        LocalDate base = new LocalDate(2006, 4, 1, ISO_LONDON);  
        DateTimeZone gaza = DateTimeZone.forID("Asia/Gaza");
        Interval test = base.toInterval(gaza);
        check(base, 2006, 4, 1);
        DateTime start = new DateTime(2006, 4, 1, 1, 0, 0, 0, gaza);
        DateTime end = new DateTime(2006, 4, 2, 0, 0, 0, 0, gaza);
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToInterval_nullZone
    public void testToInterval_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval(null);
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTimeAtStartOfDay(LONDON);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDate_summer
    public void testToDate_summer() {
        LocalDate base = new LocalDate(2005, 7, 9, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 7, 9);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JULY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDate_winter
    public void testToDate_winter() {
        LocalDate base = new LocalDate(2005, 1, 9, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 1, 9);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JANUARY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDate_springDST
    public void testToDate_springDST() {
        LocalDate base = new LocalDate(2007, 4, 2);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2);
            assertEquals("Mon Apr 02 01:00:00 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDate_Basics::testToDate_springDST_2Hour40Savings
    public void testToDate_springDST_2Hour40Savings() {
        LocalDate base = new LocalDate(2007, 4, 2);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000, (3600000 / 6) * 16);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2);
            assertEquals("Mon Apr 02 02:40:00 GMT+03:40 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDate_Basics::testToDate_autumnDST
    public void testToDate_autumnDST() {
        LocalDate base = new LocalDate(2007, 10, 2);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 10, 2);
            assertEquals("Tue Oct 02 00:00:00 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDate_Basics::testProperty
    public void testProperty() {
        LocalDate test = new LocalDate(2005, 6, 9, GJ_UTC);
        assertEquals(test.year(), test.property(DateTimeFieldType.year()));
        assertEquals(test.monthOfYear(), test.property(DateTimeFieldType.monthOfYear()));
        assertEquals(test.dayOfMonth(), test.property(DateTimeFieldType.dayOfMonth()));
        assertEquals(test.dayOfWeek(), test.property(DateTimeFieldType.dayOfWeek()));
        assertEquals(test.dayOfYear(), test.property(DateTimeFieldType.dayOfYear()));
        assertEquals(test.weekOfWeekyear(), test.property(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(test.weekyear(), test.property(DateTimeFieldType.weekyear()));
        assertEquals(test.yearOfCentury(), test.property(DateTimeFieldType.yearOfCentury()));
        assertEquals(test.yearOfEra(), test.property(DateTimeFieldType.yearOfEra()));
        assertEquals(test.centuryOfEra(), test.property(DateTimeFieldType.centuryOfEra()));
        assertEquals(test.era(), test.property(DateTimeFieldType.era()));
        try {
            test.property(DateTimeFieldType.millisOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testSerialization
    public void testSerialization() throws Exception {
        LocalDate test = new LocalDate(1972, 6, 9, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDate result = (LocalDate) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestLocalDate_Basics::testToString
    public void testToString() {
        LocalDate test = new LocalDate(2002, 6, 9);
        assertEquals("2002-06-09", test.toString());
    }

// org.joda.time.TestLocalDate_Basics::testToString_String
    public void testToString_String() {
        LocalDate test = new LocalDate(2002, 6, 9);
        assertEquals("2002 \ufffd\ufffd", test.toString("yyyy HH"));
        assertEquals("2002-06-09", test.toString((String) null));
    }

// org.joda.time.TestLocalDate_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        LocalDate test = new LocalDate(1970, 6, 9);
        assertEquals("Tue 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("mar. 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("1970-06-09", test.toString(null, Locale.ENGLISH));
        assertEquals("Tue 9/6", test.toString("EEE d/M", null));
        assertEquals("1970-06-09", test.toString(null, null));
    }

// org.joda.time.TestLocalDate_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        LocalDate test = new LocalDate(2002, 6, 9);
        assertEquals("2002 \ufffd\ufffd", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestLocalTime_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(10, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(30, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(40, test.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(TEST_TIME_NOW / 60000 , test.get(DateTimeFieldType.minuteOfDay()));
        assertEquals(TEST_TIME_NOW / 1000 , test.get(DateTimeFieldType.secondOfDay()));
        assertEquals(TEST_TIME_NOW , test.get(DateTimeFieldType.millisOfDay()));
        assertEquals(10, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalTime(12, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalTime(14, 30);
        assertEquals(2, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(2, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(14, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalTime(0, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(24, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.dayOfMonth());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testSize
    public void testSize() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(4, test.size());
    }

// org.joda.time.TestLocalTime_Basics::testGetFieldType_int
    public void testGetFieldType_int() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertSame(DateTimeFieldType.hourOfDay(), test.getFieldType(0));
        assertSame(DateTimeFieldType.minuteOfHour(), test.getFieldType(1));
        assertSame(DateTimeFieldType.secondOfMinute(), test.getFieldType(2));
        assertSame(DateTimeFieldType.millisOfSecond(), test.getFieldType(3));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.hourOfDay(), fields[0]);
        assertSame(DateTimeFieldType.minuteOfHour(), fields[1]);
        assertSame(DateTimeFieldType.secondOfMinute(), fields[2]);
        assertSame(DateTimeFieldType.millisOfSecond(), fields[3]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestLocalTime_Basics::testGetField_int
    public void testGetField_int() {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_UTC);
        assertSame(COPTIC_UTC.hourOfDay(), test.getField(0));
        assertSame(COPTIC_UTC.minuteOfHour(), test.getField(1));
        assertSame(COPTIC_UTC.secondOfMinute(), test.getField(2));
        assertSame(COPTIC_UTC.millisOfSecond(), test.getField(3));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testGetFields
    public void testGetFields() {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_UTC);
        DateTimeField[] fields = test.getFields();
        assertSame(COPTIC_UTC.hourOfDay(), fields[0]);
        assertSame(COPTIC_UTC.minuteOfHour(), fields[1]);
        assertSame(COPTIC_UTC.secondOfMinute(), fields[2]);
        assertSame(COPTIC_UTC.millisOfSecond(), fields[3]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestLocalTime_Basics::testGetValue_int
    public void testGetValue_int() {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
        assertEquals(10, test.getValue(0));
        assertEquals(20, test.getValue(1));
        assertEquals(30, test.getValue(2));
        assertEquals(40, test.getValue(3));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testGetValues
    public void testGetValues() {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_UTC);
        int[] values = test.getValues();
        assertEquals(10, values[0]);
        assertEquals(20, values[1]);
        assertEquals(30, values[2]);
        assertEquals(40, values[3]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestLocalTime_Basics::testIsSupported_DateTimeFieldType
    public void testIsSupported_DateTimeFieldType() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfSecond()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfDay()));
        
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.halfdayOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfDay()));
        
        assertEquals(false, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(false, test.isSupported((DateTimeFieldType) null));
        
        DateTimeFieldType d = new DateTimeFieldType("hours") {
            private static final long serialVersionUID = 1L;
            public DurationFieldType getDurationType() {
                return DurationFieldType.hours();
            }
            public DurationFieldType getRangeDurationType() {
                return null;
            }
            public DateTimeField getField(Chronology chronology) {
                return chronology.hourOfDay();
            }
        };
        assertEquals(false, test.isSupported(d));
        
        d = new DateTimeFieldType("hourOfYear") {
            private static final long serialVersionUID = 1L;
            public DurationFieldType getDurationType() {
                return DurationFieldType.hours();
            }
            public DurationFieldType getRangeDurationType() {
                return DurationFieldType.years();
            }
            public DateTimeField getField(Chronology chronology) {
                return chronology.hourOfDay();
            }
        };
        assertEquals(false, test.isSupported(d));
    }

// org.joda.time.TestLocalTime_Basics::testIsSupported_DurationFieldType
    public void testIsSupported_DurationFieldType() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(true, test.isSupported(DurationFieldType.hours()));
        assertEquals(true, test.isSupported(DurationFieldType.minutes()));
        assertEquals(true, test.isSupported(DurationFieldType.seconds()));
        assertEquals(true, test.isSupported(DurationFieldType.millis()));
        assertEquals(true, test.isSupported(DurationFieldType.halfdays()));
        
        assertEquals(false, test.isSupported(DurationFieldType.days()));
        assertEquals(false, test.isSupported((DurationFieldType) null));
    }

// org.joda.time.TestLocalTime_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
        LocalTime test2 = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        LocalTime test3 = new LocalTime(15, 20, 30, 40);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new TimeOfDay(10, 20, 30, 40, COPTIC_UTC)));
        assertEquals(true, test1.hashCode() == new TimeOfDay(10, 20, 30, 40, COPTIC_UTC).hashCode());
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestLocalTime_Basics::testCompareTo
    public void testCompareTo() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40);
        LocalTime test1a = new LocalTime(10, 20, 30, 40);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        LocalTime test2 = new LocalTime(10, 20, 35, 40);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        LocalTime test3 = new LocalTime(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.minuteOfHour(),
            DateTimeFieldType.secondOfMinute(),
            DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {10, 20, 30, 40};
        Partial p = new Partial(types, values);
        assertEquals(0, test1.compareTo(p));
        assertEquals(0, test1.compareTo(new TimeOfDay(10, 20, 30, 40)));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

    }

// org.joda.time.TestLocalTime_Basics::testIsEqual_LocalTime
    public void testIsEqual_LocalTime() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40);
        LocalTime test1a = new LocalTime(10, 20, 30, 40);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        LocalTime test2 = new LocalTime(10, 20, 35, 40);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        LocalTime test3 = new LocalTime(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new LocalTime(10, 20, 35, 40).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testIsBefore_LocalTime
    public void testIsBefore_LocalTime() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40);
        LocalTime test1a = new LocalTime(10, 20, 30, 40);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        LocalTime test2 = new LocalTime(10, 20, 35, 40);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        LocalTime test3 = new LocalTime(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new LocalTime(10, 20, 35, 40).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testIsAfter_LocalTime
    public void testIsAfter_LocalTime() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40);
        LocalTime test1a = new LocalTime(10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        LocalTime test2 = new LocalTime(10, 20, 35, 40);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        LocalTime test3 = new LocalTime(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new LocalTime(10, 20, 35, 40).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithField_DateTimeFieldType_int_1
    public void testWithField_DateTimeFieldType_int_1() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withField(DateTimeFieldType.hourOfDay(), 15);
        
        assertEquals(new LocalTime(10, 20, 30, 40), test);
        assertEquals(new LocalTime(15, 20, 30, 40), result);
    }

// org.joda.time.TestLocalTime_Basics::testWithField_DateTimeFieldType_int_2
    public void testWithField_DateTimeFieldType_int_2() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithField_DateTimeFieldType_int_3
    public void testWithField_DateTimeFieldType_int_3() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withField(DateTimeFieldType.dayOfMonth(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithField_DateTimeFieldType_int_4
    public void testWithField_DateTimeFieldType_int_4() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withField(DateTimeFieldType.hourOfDay(), 10);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_1
    public void testWithFieldAdded_DurationFieldType_int_1() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withFieldAdded(DurationFieldType.hours(), 6);
        
        assertEquals(new LocalTime(10, 20, 30, 40), test);
        assertEquals(new LocalTime(16, 20, 30, 40), result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_2
    public void testWithFieldAdded_DurationFieldType_int_2() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_3
    public void testWithFieldAdded_DurationFieldType_int_3() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_4
    public void testWithFieldAdded_DurationFieldType_int_4() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withFieldAdded(DurationFieldType.hours(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_5
    public void testWithFieldAdded_DurationFieldType_int_5() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withFieldAdded(DurationFieldType.days(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_6
    public void testWithFieldAdded_DurationFieldType_int_6() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withFieldAdded(DurationFieldType.hours(), 16);
        
        assertEquals(new LocalTime(10, 20, 30, 40), test);
        assertEquals(new LocalTime(2, 20, 30, 40), result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_7
    public void testWithFieldAdded_DurationFieldType_int_7() {
        LocalTime test = new LocalTime(23, 59, 59, 999);
        LocalTime result = test.withFieldAdded(DurationFieldType.millis(), 1);
        assertEquals(new LocalTime(0, 0, 0, 0), result);
        
        test = new LocalTime(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.seconds(), 1);
        assertEquals(new LocalTime(0, 0, 0, 999), result);
        
        test = new LocalTime(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.minutes(), 1);
        assertEquals(new LocalTime(0, 0, 59, 999), result);
        
        test = new LocalTime(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.hours(), 1);
        assertEquals(new LocalTime(0, 59, 59, 999), result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_8
    public void testWithFieldAdded_DurationFieldType_int_8() {
        LocalTime test = new LocalTime(0, 0, 0, 0);
        LocalTime result = test.withFieldAdded(DurationFieldType.millis(), -1);
        assertEquals(new LocalTime(23, 59, 59, 999), result);
        
        test = new LocalTime(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.seconds(), -1);
        assertEquals(new LocalTime(23, 59, 59, 0), result);
        
        test = new LocalTime(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.minutes(), -1);
        assertEquals(new LocalTime(23, 59, 0, 0), result);
        
        test = new LocalTime(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.hours(), -1);
        assertEquals(new LocalTime(23, 0, 0, 0), result);
    }

// org.joda.time.TestLocalTime_Basics::testPlus_RP
    public void testPlus_RP() {
        LocalTime test = new LocalTime(10, 20, 30, 40, BUDDHIST_LONDON);
        LocalTime result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        LocalTime expected = new LocalTime(15, 26, 37, 48, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testPlusHours_int
    public void testPlusHours_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.plusHours(1);
        LocalTime expected = new LocalTime(2, 2, 3, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testPlusMinutes_int
    public void testPlusMinutes_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.plusMinutes(1);
        LocalTime expected = new LocalTime(1, 3, 3, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testPlusSeconds_int
    public void testPlusSeconds_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.plusSeconds(1);
        LocalTime expected = new LocalTime(1, 2, 4, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testPlusMillis_int
    public void testPlusMillis_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.plusMillis(1);
        LocalTime expected = new LocalTime(1, 2, 3, 5, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinus_RP
    public void testMinus_RP() {
        LocalTime test = new LocalTime(10, 20, 30, 40, BUDDHIST_LONDON);
        LocalTime result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        LocalTime expected = new LocalTime(9, 19, 29, 39, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinusHours_int
    public void testMinusHours_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.minusHours(1);
        LocalTime expected = new LocalTime(0, 2, 3, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinusMinutes_int
    public void testMinusMinutes_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.minusMinutes(1);
        LocalTime expected = new LocalTime(1, 1, 3, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinusSeconds_int
    public void testMinusSeconds_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.minusSeconds(1);
        LocalTime expected = new LocalTime(1, 2, 2, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinusMillis_int
    public void testMinusMillis_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.minusMillis(1);
        LocalTime expected = new LocalTime(1, 2, 3, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testGetters
    public void testGetters() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(TEST_TIME_NOW, test.getMillisOfDay());
    }

// org.joda.time.TestLocalTime_Basics::testWithers
    public void testWithers() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        check(test.withHourOfDay(6), 6, 20, 30, 40);
        check(test.withMinuteOfHour(6), 10, 6, 30, 40);
        check(test.withSecondOfMinute(6), 10, 20, 6, 40);
        check(test.withMillisOfSecond(6), 10, 20, 30, 6);
        check(test.withMillisOfDay(61234), 0, 1, 1, 234);
        try {
            test.withHourOfDay(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withHourOfDay(24);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testToDateTimeTodayDefaultZone
    public void testToDateTimeTodayDefaultZone() {
        LocalTime base = new LocalTime(10, 20, 30, 40, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeToday();
        check(base, 10, 20, 30, 40);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_LONDON);
        expected = expected.hourOfDay().setCopy(10);
        expected = expected.minuteOfHour().setCopy(20);
        expected = expected.secondOfMinute().setCopy(30);
        expected = expected.millisOfSecond().setCopy(40);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalTime_Basics::testToDateTimeToday_Zone
    public void testToDateTimeToday_Zone() {
        LocalTime base = new LocalTime(10, 20, 30, 40, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeToday(TOKYO);
        check(base, 10, 20, 30, 40);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_TOKYO);
        expected = expected.hourOfDay().setCopy(10);
        expected = expected.minuteOfHour().setCopy(20);
        expected = expected.secondOfMinute().setCopy(30);
        expected = expected.millisOfSecond().setCopy(40);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalTime_Basics::testToDateTimeToday_nullZone
    public void testToDateTimeToday_nullZone() {
        LocalTime base = new LocalTime(10, 20, 30, 40, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeToday((DateTimeZone) null);
        check(base, 10, 20, 30, 40);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_LONDON);
        expected = expected.hourOfDay().setCopy(10);
        expected = expected.minuteOfHour().setCopy(20);
        expected = expected.secondOfMinute().setCopy(30);
        expected = expected.millisOfSecond().setCopy(40);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalTime_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        LocalTime base = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
        DateTime dt = new DateTime(0L); 
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        
        DateTime test = base.toDateTime(dt);
        check(base, 10, 20, 30, 40);
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        assertEquals("1970-01-01T10:20:30.040+01:00", test.toString());
    }

// org.joda.time.TestLocalTime_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        LocalTime base = new LocalTime(1, 2, 3, 4);
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2);
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 1, 2, 3, 4);
        assertEquals("1970-01-02T01:02:03.004+01:00", test.toString());
    }

// org.joda.time.TestLocalTime_Basics::testProperty
    public void testProperty() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(test.hourOfDay(), test.property(DateTimeFieldType.hourOfDay()));
        assertEquals(test.minuteOfHour(), test.property(DateTimeFieldType.minuteOfHour()));
        assertEquals(test.secondOfMinute(), test.property(DateTimeFieldType.secondOfMinute()));
        assertEquals(test.millisOfSecond(), test.property(DateTimeFieldType.millisOfSecond()));
        assertEquals(test.millisOfDay(), test.property(DateTimeFieldType.millisOfDay()));
        
        assertEquals(test, test.property(DateTimeFieldType.minuteOfDay()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.secondOfDay()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.millisOfDay()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.hourOfHalfday()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.halfdayOfDay()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.clockhourOfHalfday()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.clockhourOfDay()).getLocalTime());
        
        try {
            test.property(DateTimeFieldType.dayOfWeek());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testSerialization
    public void testSerialization() throws Exception {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalTime result = (LocalTime) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestLocalTime_Basics::testToString
    public void testToString() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals("10:20:30.040", test.toString());
    }

// org.joda.time.TestLocalTime_Basics::testToString_String
    public void testToString_String() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString("yyyy HH"));
        assertEquals("10:20:30.040", test.toString((String) null));
    }

// org.joda.time.TestLocalTime_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals("10 20", test.toString("H m", Locale.ENGLISH));
        assertEquals("10:20:30.040", test.toString(null, Locale.ENGLISH));
        assertEquals("10 20", test.toString("H m", null));
        assertEquals("10:20:30.040", test.toString(null, null));
    }

// org.joda.time.TestLocalTime_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("10:20:30.040", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestMonthDay_Basics::testGet
    public void testGet() {
        MonthDay test = new MonthDay();
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.year());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testSize
    public void testSize() {
        MonthDay test = new MonthDay();
        assertEquals(2, test.size());
    }

// org.joda.time.TestMonthDay_Basics::testGetFieldType
    public void testGetFieldType() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        assertSame(DateTimeFieldType.monthOfYear(), test.getFieldType(0));
        assertSame(DateTimeFieldType.dayOfMonth(), test.getFieldType(1));

        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertEquals(2, fields.length);
        assertSame(DateTimeFieldType.monthOfYear(), fields[0]);
        assertSame(DateTimeFieldType.dayOfMonth(), fields[1]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestMonthDay_Basics::testGetField
    public void testGetField() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        assertSame(COPTIC_UTC.monthOfYear(), test.getField(0));
        assertSame(COPTIC_UTC.dayOfMonth(), test.getField(1));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testGetFields
    public void testGetFields() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertEquals(2, fields.length);
        assertSame(COPTIC_UTC.monthOfYear(), fields[0]);
        assertSame(COPTIC_UTC.dayOfMonth(), fields[1]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestMonthDay_Basics::testGetValue
    public void testGetValue() {
        MonthDay test = new MonthDay();
        assertEquals(6, test.getValue(0));
        assertEquals(9, test.getValue(1));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testGetValues
    public void testGetValues() {
        MonthDay test = new MonthDay();
        int[] values = test.getValues();
        assertEquals(2, values.length);
        assertEquals(6, values[0]);
        assertEquals(9, values[1]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestMonthDay_Basics::testIsSupported
    public void testIsSupported() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        assertEquals(false, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(false, test.isSupported(DateTimeFieldType.hourOfDay()));
    }

// org.joda.time.TestMonthDay_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        MonthDay test1 = new MonthDay(10, 6, COPTIC_PARIS);
        MonthDay test2 = new MonthDay(10, 6, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        MonthDay test3 = new MonthDay(10, 6);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockMD()));
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestMonthDay_Basics::testCompareTo
    public void testCompareTo() {
        MonthDay test1 = new MonthDay(6, 6);
        MonthDay test1a = new MonthDay(6, 6);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        MonthDay test2 = new MonthDay(6, 7);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        MonthDay test3 = new MonthDay(6, 7, GregorianChronology.getInstanceUTC());
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {6, 6};
        Partial p = new Partial(types, values);
        assertEquals(0, test1.compareTo(p));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            test1.compareTo(new LocalTime());
            fail();
        } catch (ClassCastException ex) {}
        Partial partial = new Partial()
            .with(DateTimeFieldType.centuryOfEra(), 1)
            .with(DateTimeFieldType.halfdayOfDay(), 0)
            .with(DateTimeFieldType.dayOfMonth(), 9);
        try {
            new MonthDay(10, 6).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testIsEqual_MD
    public void testIsEqual_MD() {
        MonthDay test1 = new MonthDay(6, 6);
        MonthDay test1a = new MonthDay(6, 6);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        MonthDay test2 = new MonthDay(6, 7);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        MonthDay test3 = new MonthDay(6, 7, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new MonthDay(6, 7).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testIsBefore_MD
    public void testIsBefore_MD() {
        MonthDay test1 = new MonthDay(6, 6);
        MonthDay test1a = new MonthDay(6, 6);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        MonthDay test2 = new MonthDay(6, 7);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        MonthDay test3 = new MonthDay(6, 7, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new MonthDay(6, 7).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testIsAfter_MD
    public void testIsAfter_MD() {
        MonthDay test1 = new MonthDay(6, 6);
        MonthDay test1a = new MonthDay(6, 6);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        MonthDay test2 = new MonthDay(6, 7);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        MonthDay test3 = new MonthDay(6, 7, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new MonthDay(6, 7).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithChronologyRetainFields_Chrono
    public void testWithChronologyRetainFields_Chrono() {
        MonthDay base = new MonthDay(6, 6, COPTIC_PARIS);
        MonthDay test = base.withChronologyRetainFields(BUDDHIST_TOKYO);
        check(base, 6, 6);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 6, 6);
        assertEquals(BUDDHIST_UTC, test.getChronology());
    }

// org.joda.time.TestMonthDay_Basics::testWithChronologyRetainFields_sameChrono
    public void testWithChronologyRetainFields_sameChrono() {
        MonthDay base = new MonthDay(6, 6, COPTIC_PARIS);
        MonthDay test = base.withChronologyRetainFields(COPTIC_TOKYO);
        assertSame(base, test);
    }

// org.joda.time.TestMonthDay_Basics::testWithChronologyRetainFields_nullChrono
    public void testWithChronologyRetainFields_nullChrono() {
        MonthDay base = new MonthDay(6, 6, COPTIC_PARIS);
        MonthDay test = base.withChronologyRetainFields(null);
        check(base, 6, 6);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 6, 6);
        assertEquals(ISO_UTC, test.getChronology());
    }

// org.joda.time.TestMonthDay_Basics::testWithField
    public void testWithField() {
        MonthDay test = new MonthDay(9, 6);
        MonthDay result = test.withField(DateTimeFieldType.monthOfYear(), 10);
        
        assertEquals(new MonthDay(9, 6), test);
        assertEquals(new MonthDay(10, 6), result);
    }

// org.joda.time.TestMonthDay_Basics::testWithField_nullField
    public void testWithField_nullField() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithField_unknownField
    public void testWithField_unknownField() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withField(DateTimeFieldType.hourOfDay(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithField_same
    public void testWithField_same() {
        MonthDay test = new MonthDay(9, 6);
        MonthDay result = test.withField(DateTimeFieldType.monthOfYear(), 9);
        assertEquals(new MonthDay(9, 6), test);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded
    public void testWithFieldAdded() {
        MonthDay test = new MonthDay(9, 6);
        MonthDay result = test.withFieldAdded(DurationFieldType.months(), 1);
        
        assertEquals(new MonthDay(9, 6), test);
        assertEquals(new MonthDay(10, 6), result);
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded_nullField_zero
    public void testWithFieldAdded_nullField_zero() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded_nullField_nonZero
    public void testWithFieldAdded_nullField_nonZero() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded_zero
    public void testWithFieldAdded_zero() {
        MonthDay test = new MonthDay(9, 6);
        MonthDay result = test.withFieldAdded(DurationFieldType.months(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded_unknownField
    public void testWithFieldAdded_unknownField() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withFieldAdded(DurationFieldType.hours(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testPlus_RP
    public void testPlus_RP() {
        MonthDay test = new MonthDay(6, 5, BuddhistChronology.getInstance());
        MonthDay result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        MonthDay expected = new MonthDay(8, 9, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        MonthDay test = new MonthDay(6, 5, BuddhistChronology.getInstance());
        MonthDay result = test.plusMonths(1);
        MonthDay expected = new MonthDay(7, 5, BuddhistChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_fromLeap
    public void testPlusMonths_int_fromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(1);
        MonthDay expected = new MonthDay(3, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_negativeFromLeap
    public void testPlusMonths_int_negativeFromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(-1);
        MonthDay expected = new MonthDay(1, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_endOfMonthAdjust
    public void testPlusMonths_int_endOfMonthAdjust() {
        MonthDay test = new MonthDay(3, 31, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(1);
        MonthDay expected = new MonthDay(4, 30, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_negativeEndOfMonthAdjust
    public void testPlusMonths_int_negativeEndOfMonthAdjust() {
        MonthDay test = new MonthDay(3, 31, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(-1);
        MonthDay expected = new MonthDay(2, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }
