// buggy code
    public long computeMillis(boolean resetFields, String text) {
        SavedField[] savedFields = iSavedFields;
        int count = iSavedFieldsCount;
        if (iSavedFieldsShared) {
            iSavedFields = savedFields = (SavedField[])iSavedFields.clone();
            iSavedFieldsShared = false;
        }
        sort(savedFields, count);
        if (count > 0) {
            // alter base year for parsing if first field is month or day
            DurationField months = DurationFieldType.months().getField(iChrono);
            DurationField days = DurationFieldType.days().getField(iChrono);
            DurationField first = savedFields[0].iField.getDurationField();
            if (compareReverse(first, months) >= 0 && compareReverse(first, days) <= 0) {
                saveField(DateTimeFieldType.year(), iDefaultYear);
                return computeMillis(resetFields, text);
            }
        }

        long millis = iMillis;
        try {
            for (int i = 0; i < count; i++) {
                millis = savedFields[i].set(millis, resetFields);
            }
        } catch (IllegalFieldValueException e) {
            if (text != null) {
                e.prependMessage("Cannot parse \"" + text + '"');
            }
            throw e;
        }
        
        if (iZone == null) {
            millis -= iOffset;
        } else {
            int offset = iZone.getOffsetFromLocal(millis);
            millis -= offset;
            if (offset != iZone.getOffset(millis)) {
                String message =
                    "Illegal instant due to time zone offset transition (" + iZone + ')';
                if (text != null) {
                    message = "Cannot parse \"" + text + "\": " + message;
                }
                throw new IllegalArgumentException(message);
            }
        }
        
        return millis;
    }

// relevant test
// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_stringLengths
    public void testForStyle_stringLengths() {
        try {
            DateTimeFormat.forStyle(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("SSS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_invalidStrings
    public void testForStyle_invalidStrings() {
        try {
            DateTimeFormat.forStyle("AA");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("--");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("ss");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortDate
    public void testForStyle_shortDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("S-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        DateTime date = new DateTime(
                DateFormat.getDateInstance(DateFormat.SHORT, FRANCE).parse(expect));
        assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortTime
    public void testForStyle_shortTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("-S");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getTimeInstance(DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        if (TimeZone.getDefault() instanceof SimpleTimeZone) {
            
        } else {
            DateTime date = new DateTime(
                DateFormat.getTimeInstance(DateFormat.SHORT, FRANCE).parse(expect));
            assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
        }
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortDateTime
    public void testForStyle_shortDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortDateTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("SS");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        DateTime date = new DateTime(
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, FRANCE).parse(expect));
        assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumDate
    public void testForStyle_mediumDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("M-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumTime
    public void testForStyle_mediumTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("-M");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumDateTime
    public void testForStyle_mediumDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumDateTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("MM");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longDate
    public void testForStyle_longDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.longDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("L-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.LONG, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.LONG, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.LONG, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longTime
    public void testForStyle_longTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longDateTime
    public void testForStyle_longDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullDate
    public void testForStyle_fullDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.fullDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("F-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.FULL, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.FULL, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.FULL, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullTime
    public void testForStyle_fullTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullDateTime
    public void testForStyle_fullDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortMediumDateTime
    public void testForStyle_shortMediumDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forStyle("SM");
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortLongDateTime
    public void testForStyle_shortLongDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortFullDateTime
    public void testForStyle_shortFullDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumShortDateTime
    public void testForStyle_mediumShortDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forStyle("MS");
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumLongDateTime
    public void testForStyle_mediumLongDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumFullDateTime
    public void testForStyle_mediumFullDateTime() {}

// org.joda.time.format.TestDateTimeFormatter::testPrint_simple
    public void testPrint_simple() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T10:20:30Z", f.print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_locale
    public void testPrint_locale() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("mer. 2004-06-09T10:20:30Z", f.withLocale(Locale.FRENCH).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withLocale(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_zone
    public void testPrint_zone() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withZone(null).print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withZoneUTC().print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_chrono
    public void testPrint_chrono() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withChronology(ISO_PARIS).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(BUDDHIST_PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withChronology(null).print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withChronology(ISO_PARIS).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(BUDDHIST_PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withChronology(ISO_UTC).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_bufferMethods
    public void testPrint_bufferMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", buf.toString());
        
        buf = new StringBuffer();
        f.printTo(buf, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", buf.toString());
        
        buf = new StringBuffer();
        ISODateTimeFormat.yearMonthDay().printTo(buf, dt.toYearMonthDay());
        assertEquals("2004-06-09", buf.toString());
        
        buf = new StringBuffer();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(buf, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_writerMethods
    public void testPrint_writerMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        CharArrayWriter out = new CharArrayWriter();
        f.printTo(out, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", out.toString());
        
        out = new CharArrayWriter();
        f.printTo(out, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", out.toString());
        
        out = new CharArrayWriter();
        ISODateTimeFormat.yearMonthDay().printTo(out, dt.toYearMonthDay());
        assertEquals("2004-06-09", out.toString());
        
        out = new CharArrayWriter();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(out, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_appendableMethods
    public void testPrint_appendableMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        StringBuilder buf = new StringBuilder();
        f.printTo(buf, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", buf.toString());
        
        buf = new StringBuilder();
        f.printTo(buf, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", buf.toString());
        
        buf = new StringBuilder();
        ISODateTimeFormat.yearMonthDay().printTo(buf, dt.toLocalDate());
        assertEquals("2004-06-09", buf.toString());
        
        buf = new StringBuilder();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(buf, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_chrono_and_zone
    public void testPrint_chrono_and_zone() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T10:20:30Z",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
        
        dt = dt.withChronology(ISO_PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2547-06-09T12:20:30+02:00",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2547-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetLocale
    public void testWithGetLocale() {
        DateTimeFormatter f2 = f.withLocale(Locale.FRENCH);
        assertEquals(Locale.FRENCH, f2.getLocale());
        assertSame(f2, f2.withLocale(Locale.FRENCH));
        
        f2 = f.withLocale(null);
        assertEquals(null, f2.getLocale());
        assertSame(f2, f2.withLocale(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetZone
    public void testWithGetZone() {
        DateTimeFormatter f2 = f.withZone(PARIS);
        assertEquals(PARIS, f2.getZone());
        assertSame(f2, f2.withZone(PARIS));
        
        f2 = f.withZone(null);
        assertEquals(null, f2.getZone());
        assertSame(f2, f2.withZone(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetChronology
    public void testWithGetChronology() {
        DateTimeFormatter f2 = f.withChronology(BUDDHIST_PARIS);
        assertEquals(BUDDHIST_PARIS, f2.getChronology());
        assertSame(f2, f2.withChronology(BUDDHIST_PARIS));
        
        f2 = f.withChronology(null);
        assertEquals(null, f2.getChronology());
        assertSame(f2, f2.withChronology(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetPivotYear
    public void testWithGetPivotYear() {
        DateTimeFormatter f2 = f.withPivotYear(13);
        assertEquals(new Integer(13), f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(13));
        
        f2 = f.withPivotYear(new Integer(14));
        assertEquals(new Integer(14), f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(new Integer(14)));
        
        f2 = f.withPivotYear(null);
        assertEquals(null, f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetOffsetParsedMethods
    public void testWithGetOffsetParsedMethods() {
        DateTimeFormatter f2 = f;
        assertEquals(false, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f.withOffsetParsed();
        assertEquals(true, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f2.withZone(PARIS);
        assertEquals(false, f2.isOffsetParsed());
        assertEquals(PARIS, f2.getZone());
        
        f2 = f2.withOffsetParsed();
        assertEquals(true, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f.withOffsetParsed();
        assertNotSame(f, f2);
        DateTimeFormatter f3 = f2.withOffsetParsed();
        assertSame(f2, f3);
    }

// org.joda.time.format.TestDateTimeFormatter::testPrinterParserMethods
    public void testPrinterParserMethods() {
        DateTimeFormatter f2 = new DateTimeFormatter(f.getPrinter(), f.getParser());
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(true, f2.isParser());
        assertNotNull(f2.print(0L));
        assertNotNull(f2.parseDateTime("Thu 1970-01-01T00:00:00Z"));
        
        f2 = new DateTimeFormatter(f.getPrinter(), null);
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(null, f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(false, f2.isParser());
        assertNotNull(f2.print(0L));
        try {
            f2.parseDateTime("Thu 1970-01-01T00:00:00Z");
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        f2 = new DateTimeFormatter(null, f.getParser());
        assertEquals(null, f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(false, f2.isPrinter());
        assertEquals(true, f2.isParser());
        try {
            f2.print(0L);
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertNotNull(f2.parseDateTime("Thu 1970-01-01T00:00:00Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_simple
    public void testParseLocalDate_simple() {
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30Z"));
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalDate(2004, 6, 9, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalDate("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_yearOfEra
    public void testParseLocalDate_yearOfEra() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("YYYY-MM GG")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        LocalDate date = new LocalDate(2005, 10, 1, chrono);
        assertEquals(date, f.parseLocalDate("2005-10 AD"));
        assertEquals(date, f.parseLocalDate("2005-10 CE"));
        
        date = new LocalDate(-2005, 10, 1, chrono);
        assertEquals(date, f.parseLocalDate("2005-10 BC"));
        assertEquals(date, f.parseLocalDate("2005-10 BCE"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_yearOfCentury
    public void testParseLocalDate_yearOfCentury() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("yy M d")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withPivotYear(2050);
        
        LocalDate date = new LocalDate(2050, 8, 4, chrono);
        assertEquals(date, f.parseLocalDate("50 8 4"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_monthDay_feb29
    public void testParseLocalDate_monthDay_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        assertEquals(new LocalDate(2000, 2, 29, chrono), f.parseLocalDate("2 29"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_monthDay_withDefaultYear_feb29
    public void testParseLocalDate_monthDay_withDefaultYear_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withDefaultYear(2012);
        
        assertEquals(new LocalDate(2012, 2, 29, chrono), f.parseLocalDate("2 29"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_weekyear_month_week_2010
    public void testParseLocalDate_weekyear_month_week_2010() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2010, 1, 4, chrono), f.parseLocalDate("2010-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_weekyear_month_week_2011
    public void testParseLocalDate_weekyear_month_week_2011() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2011, 1, 3, chrono), f.parseLocalDate("2011-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_weekyear_month_week_2012
    public void testParseLocalDate_weekyear_month_week_2012() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2012, 1, 2, chrono), f.parseLocalDate("2012-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2010
    public void testParseLocalDate_year_month_week_2010() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2010, 1, 4, chrono), f.parseLocalDate("2010-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2011
    public void testParseLocalDate_year_month_week_2011() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2011, 1, 3, chrono), f.parseLocalDate("2011-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2012
    public void testParseLocalDate_year_month_week_2012() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2012, 1, 2, chrono), f.parseLocalDate("2012-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2013
    public void testParseLocalDate_year_month_week_2013() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2012, 12, 31, chrono), f.parseLocalDate("2013-01-01"));  
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2014
    public void testParseLocalDate_year_month_week_2014() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2013, 12, 30, chrono), f.parseLocalDate("2014-01-01"));  
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2015
    public void testParseLocalDate_year_month_week_2015() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2014, 12, 29, chrono), f.parseLocalDate("2015-01-01"));  
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2016
    public void testParseLocalDate_year_month_week_2016() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2016, 1, 4, chrono), f.parseLocalDate("2016-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalTime_simple
    public void testParseLocalTime_simple() {
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30Z"));
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalTime(10, 20, 30, 0, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalTime("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_simple
    public void testParseLocalDateTime_simple() {
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30Z"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 0, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalDateTime("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_monthDay_feb29
    public void testParseLocalDateTime_monthDay_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d H m")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        assertEquals(new LocalDateTime(2000, 2, 29, 13, 40, 0, 0, chrono), f.parseLocalDateTime("2 29 13 40"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_monthDay_withDefaultYear_feb29
    public void testParseLocalDateTime_monthDay_withDefaultYear_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d H m")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withDefaultYear(2012);
        
        assertEquals(new LocalDateTime(2012, 2, 29, 13, 40, 0, 0, chrono), f.parseLocalDateTime("2 29 13 40"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_simple
    public void testParseDateTime_simple() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.parseDateTime("2004-06-09T10:20:30Z"));
        
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone
    public void testParseDateTime_zone() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone2
    public void testParseDateTime_zone2() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseDateTime("2004-06-09T06:20:30-04:00"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone3
    public void testParseDateTime_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(LONDON).parseDateTime("2004-06-09T10:20:30"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(null).parseDateTime("2004-06-09T10:20:30"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        assertEquals(expect, h.withZone(PARIS).parseDateTime("2004-06-09T10:20:30"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_simple_precedence
    public void testParseDateTime_simple_precedence() {
        DateTime expect = null;
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, f.parseDateTime("Wed 2004-06-09T10:20:30Z"));
        
        
        expect = new DateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        
        assertEquals(expect, f.parseDateTime("Mon 2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_offsetParsed
    public void testParseDateTime_offsetParsed() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withOffsetParsed().parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        assertEquals(expect, g.withOffsetParsed().parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withZone(PARIS).withOffsetParsed().parseDateTime("2004-06-09T10:20:30Z"));
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withOffsetParsed().withZone(PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_chrono
    public void testParseDateTime_chrono() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withChronology(ISO_PARIS).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0,LONDON);
        assertEquals(expect, g.withChronology(null).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseDateTime("2547-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS); 
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_simple
    public void testParseMutableDateTime_simple() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        try {
            g.parseMutableDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone
    public void testParseMutableDateTime_zone() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone2
    public void testParseMutableDateTime_zone2() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone3
    public void testParseMutableDateTime_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(LONDON).parseMutableDateTime("2004-06-09T10:20:30"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(null).parseMutableDateTime("2004-06-09T10:20:30"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        assertEquals(expect, h.withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_simple_precedence
    public void testParseMutableDateTime_simple_precedence() {
        MutableDateTime expect = null;
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, f.parseDateTime("Wed 2004-06-09T10:20:30Z"));
        
        
        expect = new MutableDateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        
        assertEquals(expect, f.parseDateTime("Mon 2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_offsetParsed
    public void testParseMutableDateTime_offsetParsed() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withOffsetParsed().parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        assertEquals(expect, g.withOffsetParsed().parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withZone(PARIS).withOffsetParsed().parseMutableDateTime("2004-06-09T10:20:30Z"));
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withOffsetParsed().withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_chrono
    public void testParseMutableDateTime_chrono() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withChronology(ISO_PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0,LONDON);
        assertEquals(expect, g.withChronology(null).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseMutableDateTime("2547-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS); 
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_simple
    public void testParseInto_simple() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        MutableDateTime result = new MutableDateTime(0L);
        assertEquals(20, g.parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        try {
            g.parseInto(null, "2004-06-09T10:20:30Z", 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(~0, g.parseInto(result, "ABC", 0));
        assertEquals(~10, g.parseInto(result, "2004-06-09", 0));
        assertEquals(~13, g.parseInto(result, "XX2004-06-09T", 2));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_zone
    public void testParseInto_zone() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(LONDON).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(null).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_zone2
    public void testParseInto_zone2() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(25, g.withZone(LONDON).parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(25, g.withZone(null).parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(25, g.withZone(PARIS).parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_zone3
    public void testParseInto_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(19, h.withZone(LONDON).parseInto(result, "2004-06-09T10:20:30", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(19, h.withZone(null).parseInto(result, "2004-06-09T10:20:30", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(19, h.withZone(PARIS).parseInto(result, "2004-06-09T10:20:30", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_simple_precedence
    public void testParseInto_simple_precedence() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        
        assertEquals(24, f.parseInto(result, "Mon 2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_offsetParsed
    public void testParseInto_offsetParsed() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withOffsetParsed().parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        result = new MutableDateTime(0L);
        assertEquals(25, g.withOffsetParsed().parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(PARIS).withOffsetParsed().parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withOffsetParsed().withZone(PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_chrono
    public void testParseInto_chrono() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(ISO_PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(null).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(BUDDHIST_PARIS).parseInto(result, "2547-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(BUDDHIST_PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMillis_fractionOfSecondLong
    public void testParseMillis_fractionOfSecondLong() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
            .appendSecondOfDay(2).appendLiteral('.').appendFractionOfSecond(1, 9)
                .toFormatter().withZoneUTC();
        assertEquals(10512, f.parseMillis("10.5123456"));
        assertEquals(10512, f.parseMillis("10.512999"));
    }

// org.joda.time.format.TestDateTimeFormatter::testZoneNameNearTransition
    public void testZoneNameNearTransition() {}

// org.joda.time.format.TestDateTimeFormatter::testZoneShortNameNearTransition
    public void testZoneShortNameNearTransition() {}

// org.joda.time.format.TestDateTimeFormatterBuilder::test_toFormatter
    public void test_toFormatter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        try {
            bld.toFormatter();
            fail();
        } catch (UnsupportedOperationException ex) {}
        bld.appendLiteral('X');
        assertNotNull(bld.toFormatter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_toPrinter
    public void test_toPrinter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        try {
            bld.toPrinter();
            fail();
        } catch (UnsupportedOperationException ex) {}
        bld.appendLiteral('X');
        assertNotNull(bld.toPrinter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_toParser
    public void test_toParser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        try {
            bld.toParser();
            fail();
        } catch (UnsupportedOperationException ex) {}
        bld.appendLiteral('X');
        assertNotNull(bld.toParser());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_canBuildFormatter
    public void test_canBuildFormatter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertEquals(false, bld.canBuildFormatter());
        bld.appendLiteral('X');
        assertEquals(true, bld.canBuildFormatter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_canBuildPrinter
    public void test_canBuildPrinter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertEquals(false, bld.canBuildPrinter());
        bld.appendLiteral('X');
        assertEquals(true, bld.canBuildPrinter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_canBuildParser
    public void test_canBuildParser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertEquals(false, bld.canBuildParser());
        bld.appendLiteral('X');
        assertEquals(true, bld.canBuildParser());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_Formatter
    public void test_append_Formatter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimeFormatter f = bld.toFormatter();
        
        DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
        bld2.appendLiteral('X');
        bld2.append(f);
        bld2.appendLiteral('Z');
        assertEquals("XYZ", bld2.toFormatter().print(0L));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_Printer
    public void test_append_Printer() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimePrinter p = bld.toPrinter();
        
        DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
        bld2.appendLiteral('X');
        bld2.append(p);
        bld2.appendLiteral('Z');
        assertEquals("XYZ", bld2.toFormatter().print(0L));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_appendFixedDecimal
    public void test_appendFixedDecimal() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendFixedDecimal(DateTimeFieldType.year(), 4);
        DateTimeFormatter f = bld.toFormatter();

        assertEquals("2007", f.print(new DateTime("2007-01-01")));
        assertEquals("0123", f.print(new DateTime("123-01-01")));
        assertEquals("0001", f.print(new DateTime("1-2-3")));
        assertEquals("99999", f.print(new DateTime("99999-2-3")));
        assertEquals("-0099", f.print(new DateTime("-99-2-3")));
        assertEquals("0000", f.print(new DateTime("0-2-3")));

        assertEquals(2001, f.parseDateTime("2001").getYear());
        try {
            f.parseDateTime("-2001");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            f.parseDateTime("200");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            f.parseDateTime("20016");
            fail();
        } catch (IllegalArgumentException e) {
        }

        bld = new DateTimeFormatterBuilder();
        bld.appendFixedDecimal(DateTimeFieldType.hourOfDay(), 2);
        bld.appendLiteral(':');
        bld.appendFixedDecimal(DateTimeFieldType.minuteOfHour(), 2);
        bld.appendLiteral(':');
        bld.appendFixedDecimal(DateTimeFieldType.secondOfMinute(), 2);
        f = bld.toFormatter();

        assertEquals("01:02:34", f.print(new DateTime("T1:2:34")));

        DateTime dt = f.parseDateTime("01:02:34");
        assertEquals(1, dt.getHourOfDay());
        assertEquals(2, dt.getMinuteOfHour());
        assertEquals(34, dt.getSecondOfMinute());

        try {
            f.parseDateTime("0145:02:34");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            f.parseDateTime("01:0:34");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_appendFixedSignedDecimal
    public void test_appendFixedSignedDecimal() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendFixedSignedDecimal(DateTimeFieldType.year(), 4);
        DateTimeFormatter f = bld.toFormatter();

        assertEquals("2007", f.print(new DateTime("2007-01-01")));
        assertEquals("0123", f.print(new DateTime("123-01-01")));
        assertEquals("0001", f.print(new DateTime("1-2-3")));
        assertEquals("99999", f.print(new DateTime("99999-2-3")));
        assertEquals("-0099", f.print(new DateTime("-99-2-3")));
        assertEquals("0000", f.print(new DateTime("0-2-3")));

        assertEquals(2001, f.parseDateTime("2001").getYear());
        assertEquals(-2001, f.parseDateTime("-2001").getYear());
        assertEquals(2001, f.parseDateTime("+2001").getYear());
        try {
            f.parseDateTime("20016");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateParser
    public void test_dateParser() {
        DateTimeFormatter parser = ISODateTimeFormat.dateParser();
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, true, "2006-06-09T+02:00");
        assertParse(parser, true, "2006-W27-3T+02:00");
        assertParse(parser, true, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_localDateParser
    public void test_localDateParser() {
        DateTimeFormatter parser = ISODateTimeFormat.localDateParser();
        assertEquals(DateTimeZone.UTC, parser.getZone());
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateElementParser
    public void test_dateElementParser() {
        DateTimeFormatter parser = ISODateTimeFormat.dateElementParser();
        assertParse(parser, "2006-06-09", new DateTime(2006, 6, 9, 0, 0, 0, 0));
        assertParse(parser, "2006-06-9", new DateTime(2006, 6, 9, 0, 0, 0, 0));
        assertParse(parser, "2006-6-09", new DateTime(2006, 6, 9, 0, 0, 0, 0));
        assertParse(parser, "2006-6-9", new DateTime(2006, 6, 9, 0, 0, 0, 0));
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_timeParser
    public void test_timeParser() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.timeParser();
        assertParse(parser, false, "2006-06-09");
        assertParse(parser, false, "2006-W27-3");
        assertParse(parser, false, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, "T10:20:30.040000000", new DateTime(1970, 1, 1, 10, 20, 30, 40));
        assertParse(parser, "T10:20:30.004", new DateTime(1970, 1, 1, 10, 20, 30, 4));
        assertParse(parser, "T10:20:30.040", new DateTime(1970, 1, 1, 10, 20, 30, 40));
        assertParse(parser, "T10:20:30.400", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T10.5", new DateTime(1970, 1, 1, 10, 30, 0, 0));
        assertParse(parser, "T10:20:30.040+02:00", new DateTime(1970, 1, 1, 8, 20, 30, 40));
        assertParse(parser, "T10.5+02:00", new DateTime(1970, 1, 1, 8, 30, 0, 0));
        
        assertParse(parser, true, "10:20:30.040");
        assertParse(parser, true, "10.5");
        assertParse(parser, true, "10:20:30.040+02:00");
        assertParse(parser, true, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_localTimeParser
    public void test_localTimeParser() {
        DateTimeFormatter parser = ISODateTimeFormat.localTimeParser();
        assertEquals(DateTimeZone.UTC, parser.getZone());
        assertParse(parser, false, "2006-06-09");
        assertParse(parser, false, "2006-W27-3");
        assertParse(parser, false, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, true, "T10:20:30.040");
        assertParse(parser, true, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, true, "10:20:30.040");
        assertParse(parser, true, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
        
        assertParse(parser, true, "00:00:10.512345678");
        assertEquals(10512, parser.parseMillis("00:00:10.512345678"));
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_timeElementParser
    public void test_timeElementParser() {
        DateTimeFormatter parser = ISODateTimeFormat.timeElementParser();
        assertParse(parser, false, "2006-06-09");
        assertParse(parser, false, "2006-W27-3");
        assertParse(parser, false, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, true, "10:20:30.040");
        assertParse(parser, true, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
        
        assertParse(parser, true, "00:00:10.512345678");
        
        assertEquals(10512, parser.parseMillis("00:00:10.512345678") + DateTimeZone.getDefault().getOffset(0L));
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateTimeParser
    public void test_dateTimeParser() {
        DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, true, "2006-06-09T+02:00");
        assertParse(parser, true, "2006-W27-3T+02:00");
        assertParse(parser, true, "2006-123T+02:00");
        
        assertParse(parser, true, "2006-06-09T10:20:30.040");
        assertParse(parser, true, "2006-W27-3T10:20:30.040");
        assertParse(parser, true, "2006-123T10:20:30.040");
        assertParse(parser, true, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, true, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, true, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, true, "T10:20:30.040");
        assertParse(parser, true, "T10.5");
        assertParse(parser, true, "T10:20:30.040+02:00");
        assertParse(parser, true, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateOptionalTimeParser
    public void test_dateOptionalTimeParser() {
        DateTimeFormatter parser = ISODateTimeFormat.dateOptionalTimeParser();
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, true, "2006-06-09T+02:00");
        assertParse(parser, true, "2006-W27-3T+02:00");
        assertParse(parser, true, "2006-123T+02:00");
        
        assertParse(parser, true, "2006-06-09T10:20:30.040");
        assertParse(parser, true, "2006-W27-3T10:20:30.040");
        assertParse(parser, true, "2006-123T10:20:30.040");
        assertParse(parser, true, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, true, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, true, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_localDateOptionalTimeParser
    public void test_localDateOptionalTimeParser() {
        DateTimeFormatter parser = ISODateTimeFormat.localDateOptionalTimeParser();
        assertEquals(DateTimeZone.UTC, parser.getZone());
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, true, "2006-06-09T10:20:30.040");
        assertParse(parser, true, "2006-W27-3T10:20:30.040");
        assertParse(parser, true, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_date
    public void test_date() {
        DateTimeFormatter parser = ISODateTimeFormat.date();
        assertParse(parser, "2006-02-04", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, "2006-2-04", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, "2006-02-4", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, "2006-2-4", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, false, "2006-02-");
        assertParse(parser, false, "2006-02");
        assertParse(parser, false, "2006--4");
        assertParse(parser, false, "2006-1");
        assertParse(parser, false, "2006");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_time
    public void test_time() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.time();
        assertParse(parser, "10:20:30.400999999Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.400Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.40Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.4Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "5:6:7.8Z", new DateTime(1970, 1, 1, 5, 6, 7, 800));
        assertParse(parser, false, "10:20.400Z");
        assertParse(parser, false, "10:2.400Z");
        assertParse(parser, false, "10.400Z");
        assertParse(parser, false, "1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_timeNoMillis
    public void test_timeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.timeNoMillis();
        assertParse(parser, "10:20:30Z", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, "5:6:7Z", new DateTime(1970, 1, 1, 5, 6, 7, 0));
        assertParse(parser, false, "10:20Z");
        assertParse(parser, false, "10:2Z");
        assertParse(parser, false, "10Z");
        assertParse(parser, false, "1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_tTime
    public void test_tTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.tTime();
        assertParse(parser, "T10:20:30.400999999Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T10:20:30.400Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T10:20:30.40Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T10:20:30.4Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T5:6:7.8Z", new DateTime(1970, 1, 1, 5, 6, 7, 800));
        assertParse(parser, false, "T10:20.400Z");
        assertParse(parser, false, "T102.400Z");
        assertParse(parser, false, "T10.400Z");
        assertParse(parser, false, "T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_tTimeNoMillis
    public void test_tTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.tTimeNoMillis();
        assertParse(parser, "T10:20:30Z", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, "T5:6:7Z", new DateTime(1970, 1, 1, 5, 6, 7, 0));
        assertParse(parser, false, "T10:20Z");
        assertParse(parser, false, "T10:2Z");
        assertParse(parser, false, "T10Z");
        assertParse(parser, false, "T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateTime
    public void test_dateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.dateTime();
        assertParse(parser, "2006-02-04T10:20:30.400999999Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-04T10:20:30.400Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-04T10:20:30.40Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-04T10:20:30.4Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-4T10:20:30.400Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-2-04T10:20:30.400Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-2-4T10:20:30.400Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-04T5:6:7.800Z", new DateTime(2006, 2, 4, 5, 6, 7, 800));
        assertParse(parser, false, "2006-02-T10:20:30.400Z");
        assertParse(parser, false, "2006-12T10:20:30.400Z");
        assertParse(parser, false, "2006-1T10:20:30.400Z");
        assertParse(parser, false, "2006T10:20:30.400Z");
        assertParse(parser, false, "200T10:20:30.400Z");
        assertParse(parser, false, "20T10:20:30.400Z");
        assertParse(parser, false, "2T10:20:30.400Z");
        assertParse(parser, false, "2006-02-04T10:20.400Z");
        assertParse(parser, false, "2006-02-04T10:2.400Z");
        assertParse(parser, false, "2006-02-04T10.400Z");
        assertParse(parser, false, "2006-02-04T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateTimeNoMillis
    public void test_dateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
        assertParse(parser, "2006-02-04T10:20:30Z", new DateTime(2006, 2, 4, 10, 20, 30, 0));
        assertParse(parser, "2006-02-4T10:20:30Z", new DateTime(2006, 2, 4, 10, 20, 30, 0));
        assertParse(parser, "2006-2-04T10:20:30Z", new DateTime(2006, 2, 4, 10, 20, 30, 0));
        assertParse(parser, "2006-2-4T10:20:30Z", new DateTime(2006, 2, 4, 10, 20, 30, 0));
        assertParse(parser, "2006-02-04T5:6:7Z", new DateTime(2006, 2, 4, 5, 6, 7, 0));
        assertParse(parser, false, "2006-02-T10:20:30Z");
        assertParse(parser, false, "2006-12T10:20:30Z");
        assertParse(parser, false, "2006-1T10:20:30Z");
        assertParse(parser, false, "2006T10:20:30Z");
        assertParse(parser, false, "200T10:20:30Z");
        assertParse(parser, false, "20T10:20:30Z");
        assertParse(parser, false, "2T10:20:30Z");
        assertParse(parser, false, "2006-02-04T10:20Z");
        assertParse(parser, false, "2006-02-04T10:2Z");
        assertParse(parser, false, "2006-02-04T10Z");
        assertParse(parser, false, "2006-02-04T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_ordinalDate
    public void test_ordinalDate() {
        DateTimeFormatter parser = ISODateTimeFormat.ordinalDate();
        assertParse(parser, "2006-123", new DateTime(2006, 1, 1, 0, 0, 0, 0).withDayOfYear(123));
        assertParse(parser, "2006-12", new DateTime(2006, 1, 1, 0, 0, 0, 0).withDayOfYear(12));
        assertParse(parser, "2006-1", new DateTime(2006, 1, 1, 0, 0, 0, 0).withDayOfYear(1));
        assertParse(parser, false, "2006-");
        assertParse(parser, false, "2006");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_ordinalDateTime
    public void test_ordinalDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.ordinalDateTime();
        assertParse(parser, "2006-123T10:20:30.400999999Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006-123T10:20:30.400Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006-123T10:20:30.40Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006-123T10:20:30.4Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006-12T10:20:30.400Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(12));
        assertParse(parser, "2006-1T10:20:30.400Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(1));
        assertParse(parser, "2006-123T5:6:7.800Z", new DateTime(2006, 1, 1, 5, 6, 7, 800).withDayOfYear(123));
        assertParse(parser, false, "2006-T10:20:30.400Z");
        assertParse(parser, false, "2006T10:20:30.400Z");
        assertParse(parser, false, "2006-123T10:20.400Z");
        assertParse(parser, false, "2006-123T10:2.400Z");
        assertParse(parser, false, "2006-123T10.400Z");
        assertParse(parser, false, "2006-123T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_ordinalDateTimeNoMillis
    public void test_ordinalDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.ordinalDateTimeNoMillis();
        assertParse(parser, "2006-123T10:20:30Z", new DateTime(2006, 1, 1, 10, 20, 30, 0).withDayOfYear(123));
        assertParse(parser, "2006-12T10:20:30Z", new DateTime(2006, 1, 1, 10, 20, 30, 0).withDayOfYear(12));
        assertParse(parser, "2006-1T10:20:30Z", new DateTime(2006, 1, 1, 10, 20, 30, 0).withDayOfYear(1));
        assertParse(parser, "2006-123T5:6:7Z", new DateTime(2006, 1, 1, 5, 6, 7, 0).withDayOfYear(123));
        assertParse(parser, false, "2006-T10:20:30Z");
        assertParse(parser, false, "2006T10:20:30Z");
        assertParse(parser, false, "2006-123T10:20Z");
        assertParse(parser, false, "2006-123T10:2Z");
        assertParse(parser, false, "2006-123T10Z");
        assertParse(parser, false, "2006-123T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_weekDate
    public void test_weekDate() {
        DateTimeFormatter parser = ISODateTimeFormat.weekDate();
        assertParse(parser, "2006-W27-3", new DateTime(2006, 6, 1, 0, 0, 0, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W2-3", new DateTime(2006, 6, 1, 0, 0, 0, 0).withWeekOfWeekyear(2).withDayOfWeek(3));
        assertParse(parser, false, "2006-W-3");
        assertParse(parser, false, "2006-W27-");
        assertParse(parser, false, "2006-W27");
        assertParse(parser, false, "2006-W2");
        assertParse(parser, false, "2006-W");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_weekDateTime
    public void test_weekDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.weekDateTime();
        assertParse(parser, "2006-W27-3T10:20:30.400999999Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T10:20:30.400Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T10:20:30.40Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T10:20:30.4Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W2-3T10:20:30.400Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(2).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T5:6:7.800Z", new DateTime(2006, 6, 1, 5, 6, 7, 800).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006-W27-T10:20:30.400Z");
        assertParse(parser, false, "2006-W27T10:20:30.400Z");
        assertParse(parser, false, "2006-W2T10:20:30.400Z");
        assertParse(parser, false, "2006-W-3T10:20:30.400Z");
        assertParse(parser, false, "2006-W27-3T10:20.400Z");
        assertParse(parser, false, "2006-W27-3T10:2.400Z");
        assertParse(parser, false, "2006-W27-3T10.400Z");
        assertParse(parser, false, "2006-W27-3T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_weekDateTimeNoMillis
    public void test_weekDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.weekDateTimeNoMillis();
        assertParse(parser, "2006-W27-3T10:20:30Z", new DateTime(2006, 6, 1, 10, 20, 30, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W2-3T10:20:30Z", new DateTime(2006, 6, 1, 10, 20, 30, 0).withWeekOfWeekyear(2).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T5:6:7Z", new DateTime(2006, 6, 1, 5, 6, 7, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006-W27-T10:20:30Z");
        assertParse(parser, false, "2006-W27T10:20:30Z");
        assertParse(parser, false, "2006-W2T10:20:30Z");
        assertParse(parser, false, "2006-W-3T10:20:30Z");
        assertParse(parser, false, "2006-W27-3T10:20Z");
        assertParse(parser, false, "2006-W27-3T10:2Z");
        assertParse(parser, false, "2006-W27-3T10Z");
        assertParse(parser, false, "2006-W27-3T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicDate
    public void test_basicDate() {
        DateTimeFormatter parser = ISODateTimeFormat.basicDate();
        assertParse(parser, "20060204", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, false, "2006024");
        assertParse(parser, false, "200602");
        assertParse(parser, false, "20061");
        assertParse(parser, false, "2006");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicTime
    public void test_basicTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicTime();
        assertParse(parser, "102030.400999999Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "102030.400Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "102030.40Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "102030.4Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, false, "10203.400Z");
        assertParse(parser, false, "1020.400Z");
        assertParse(parser, false, "102.400Z");
        assertParse(parser, false, "10.400Z");
        assertParse(parser, false, "1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicTimeNoMillis
    public void test_basicTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicTimeNoMillis();
        assertParse(parser, "102030Z", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, false, "10203Z");
        assertParse(parser, false, "1020Z");
        assertParse(parser, false, "102Z");
        assertParse(parser, false, "10Z");
        assertParse(parser, false, "1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicTTime
    public void test_basicTTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicTTime();
        assertParse(parser, "T102030.400999999Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T102030.400Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T102030.40Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T102030.4Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, false, "T10203.400Z");
        assertParse(parser, false, "T1020.400Z");
        assertParse(parser, false, "T102.400Z");
        assertParse(parser, false, "T10.400Z");
        assertParse(parser, false, "T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicTTimeNoMillis
    public void test_basicTTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicTTimeNoMillis();
        assertParse(parser, "T102030Z", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, false, "T10203Z");
        assertParse(parser, false, "T1020Z");
        assertParse(parser, false, "T102Z");
        assertParse(parser, false, "T10Z");
        assertParse(parser, false, "T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicDateTime
    public void test_basicDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicDateTime();
        assertParse(parser, "20061204T102030.400999999Z", new DateTime(2006, 12, 4, 10, 20, 30, 400));
        assertParse(parser, "20061204T102030.400Z", new DateTime(2006, 12, 4, 10, 20, 30, 400));
        assertParse(parser, "20061204T102030.40Z", new DateTime(2006, 12, 4, 10, 20, 30, 400));
        assertParse(parser, "20061204T102030.4Z", new DateTime(2006, 12, 4, 10, 20, 30, 400));
        assertParse(parser, false, "2006120T102030.400Z");
        assertParse(parser, false, "200612T102030.400Z");
        assertParse(parser, false, "20061T102030.400Z");
        assertParse(parser, false, "2006T102030.400Z");
        assertParse(parser, false, "200T102030.400Z");
        assertParse(parser, false, "20T102030.400Z");
        assertParse(parser, false, "2T102030.400Z");
        assertParse(parser, false, "20061204T10203.400Z");
        assertParse(parser, false, "20061204T1020.400Z");
        assertParse(parser, false, "20061204T102.400Z");
        assertParse(parser, false, "20061204T10.400Z");
        assertParse(parser, false, "20061204T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicDateTimeNoMillis
    public void test_basicDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicDateTimeNoMillis();
        assertParse(parser, "20061204T102030Z", new DateTime(2006, 12, 4, 10, 20, 30, 0));
        assertParse(parser, false, "2006120T102030Z");
        assertParse(parser, false, "200612T102030Z");
        assertParse(parser, false, "20061T102030Z");
        assertParse(parser, false, "2006T102030Z");
        assertParse(parser, false, "200T102030Z");
        assertParse(parser, false, "20T102030Z");
        assertParse(parser, false, "2T102030Z");
        assertParse(parser, false, "20061204T10203Z");
        assertParse(parser, false, "20061204T1020Z");
        assertParse(parser, false, "20061204T102Z");
        assertParse(parser, false, "20061204T10Z");
        assertParse(parser, false, "20061204T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicOrdinalDate
    public void test_basicOrdinalDate() {
        DateTimeFormatter parser = ISODateTimeFormat.basicOrdinalDate();
        assertParse(parser, "2006123", new DateTime(2006, 1, 1, 0, 0, 0, 0).withDayOfYear(123));
        assertParse(parser, false, "200612");
        assertParse(parser, false, "20061");
        assertParse(parser, false, "2006");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicOrdinalDateTime
    public void test_basicOrdinalDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicOrdinalDateTime();
        assertParse(parser, "2006123T102030.400999999Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006123T102030.400Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006123T102030.40Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006123T102030.4Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, false, "200612T102030.400Z");
        assertParse(parser, false, "20061T102030.400Z");
        assertParse(parser, false, "2006T102030.400Z");
        assertParse(parser, false, "200T102030.400Z");
        assertParse(parser, false, "20T102030.400Z");
        assertParse(parser, false, "2T102030.400Z");
        assertParse(parser, false, "2006123T10203.400Z");
        assertParse(parser, false, "2006123T1020.400Z");
        assertParse(parser, false, "2006123T102.400Z");
        assertParse(parser, false, "2006123T10.400Z");
        assertParse(parser, false, "2006123T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicOrdinalDateTimeNoMillis
    public void test_basicOrdinalDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicOrdinalDateTimeNoMillis();
        assertParse(parser, "2006123T102030Z", new DateTime(2006, 1, 1, 10, 20, 30, 0).withDayOfYear(123));
        assertParse(parser, false, "200612T102030Z");
        assertParse(parser, false, "20061T102030Z");
        assertParse(parser, false, "2006T102030Z");
        assertParse(parser, false, "200T102030Z");
        assertParse(parser, false, "20T102030Z");
        assertParse(parser, false, "2T102030Z");
        assertParse(parser, false, "2006123T10203Z");
        assertParse(parser, false, "2006123T1020Z");
        assertParse(parser, false, "2006123T102Z");
        assertParse(parser, false, "2006123T10Z");
        assertParse(parser, false, "2006123T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicWeekDate
    public void test_basicWeekDate() {
        DateTimeFormatter parser = ISODateTimeFormat.basicWeekDate();
        assertParse(parser, "2006W273", new DateTime(2006, 6, 1, 0, 0, 0, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006W27");
        assertParse(parser, false, "2006W2");
        assertParse(parser, false, "2006W");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicWeekDateTime
    public void test_basicWeekDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicWeekDateTime();
        assertParse(parser, "2006W273T102030.400999999Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006W273T102030.400Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006W273T102030.40Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006W273T102030.4Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006W27T102030.400Z");
        assertParse(parser, false, "2006W2T102030.400Z");
        assertParse(parser, false, "2006W273T10203.400Z");
        assertParse(parser, false, "2006W273T1020.400Z");
        assertParse(parser, false, "2006W273T102.400Z");
        assertParse(parser, false, "2006W273T10.400Z");
        assertParse(parser, false, "2006W273T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicWeekDateTimeNoMillis
    public void test_basicWeekDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicWeekDateTimeNoMillis();
        assertParse(parser, "2006W273T102030Z", new DateTime(2006, 6, 1, 10, 20, 30, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006W27T102030Z");
        assertParse(parser, false, "2006W2T102030Z");
        assertParse(parser, false, "2006W273T10203Z");
        assertParse(parser, false, "2006W273T1020Z");
        assertParse(parser, false, "2006W273T102Z");
        assertParse(parser, false, "2006W273T10Z");
        assertParse(parser, false, "2006W273T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_hourMinute
    public void test_hourMinute() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.hourMinute();
        assertParse(parser, "10:20", new DateTime(1970, 1, 1, 10, 20, 0, 0));
        assertParse(parser, "5:6", new DateTime(1970, 1, 1, 5, 6, 0, 0));
        assertParse(parser, false, "10:20:30.400999999");
        assertParse(parser, false, "10:20:30.400");
        assertParse(parser, false, "10:20:30");
        assertParse(parser, false, "10:20.400");
        assertParse(parser, false, "10:2.400");
        assertParse(parser, false, "10.400");
        assertParse(parser, false, "1.400");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_hourMinuteSecond
    public void test_hourMinuteSecond() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.hourMinuteSecond();
        assertParse(parser, "10:20:30", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, "5:6:7", new DateTime(1970, 1, 1, 5, 6, 7, 0));
        assertParse(parser, false, "10:20:30.400999999");
        assertParse(parser, false, "10:20:30.400");
        assertParse(parser, false, "10:20:30.4");
        assertParse(parser, false, "10:20.400");
        assertParse(parser, false, "10:2.400");
        assertParse(parser, false, "10.400");
        assertParse(parser, false, "1.400");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_hourMinuteSecondMillis
    public void test_hourMinuteSecondMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.hourMinuteSecondMillis();
        assertParse(parser, "10:20:30.400", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.40", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.4", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "5:6:7.8", new DateTime(1970, 1, 1, 5, 6, 7, 800));
        assertParse(parser, false, "10:20:30.400999999");
        assertParse(parser, false, "10:20.400");
        assertParse(parser, false, "10:2.400");
        assertParse(parser, false, "10.400");
        assertParse(parser, false, "1.400");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_hourMinuteSecondFraction
    public void test_hourMinuteSecondFraction() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.hourMinuteSecondFraction();
        assertParse(parser, "10:20:30.400999999", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.400", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.40", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.4", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "5:6:7.8", new DateTime(1970, 1, 1, 5, 6, 7, 800));
        assertParse(parser, false, "10:20.400");
        assertParse(parser, false, "10:2.400");
        assertParse(parser, false, "10.400");
        assertParse(parser, false, "1.400");
    }

// org.joda.time.tz.TestBuilder::testID
    public void testID() {
        DateTimeZone tz = buildAmericaLosAngeles();
        assertEquals("America/Los_Angeles", tz.getID());
        assertEquals(false, tz.isFixed());
    }

// org.joda.time.tz.TestBuilder::testForwardTransitions
    public void testForwardTransitions() {
        DateTimeZone tz = buildAmericaLosAngeles();
        testForwardTransitions(tz, AMERICA_LOS_ANGELES_DATA);
    }

// org.joda.time.tz.TestBuilder::testReverseTransitions
    public void testReverseTransitions() {
        DateTimeZone tz = buildAmericaLosAngeles();
        testReverseTransitions(tz, AMERICA_LOS_ANGELES_DATA);
    }

// org.joda.time.tz.TestBuilder::testSerialization
    public void testSerialization() throws IOException {
        DateTimeZone tz = testSerialization
            (buildAmericaLosAngelesBuilder(), "America/Los_Angeles");

        assertEquals(false, tz.isFixed());
        testForwardTransitions(tz, AMERICA_LOS_ANGELES_DATA);
        testReverseTransitions(tz, AMERICA_LOS_ANGELES_DATA);
    }

// org.joda.time.tz.TestBuilder::testFixed
    public void testFixed() throws IOException {
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder()
            .setStandardOffset(3600000)
            .setFixedSavings("LMT", 0);
        DateTimeZone tz = builder.toDateTimeZone("Test", true);

        for (int i=0; i<2; i++) {
            assertEquals("Test", tz.getID());
            assertEquals(true, tz.isFixed());
            assertEquals(3600000, tz.getOffset(0));
            assertEquals(3600000, tz.getStandardOffset(0));
            assertEquals(0, tz.nextTransition(0));
            assertEquals(0, tz.previousTransition(0));

            tz = testSerialization(builder, "Test");
        }
    }

// org.joda.time.tz.TestCompiler::testCompile
    public void testCompile() throws Exception {
        Provider provider = compileAndLoad(AMERICA_LOS_ANGELES_FILE);
        DateTimeZone tz = provider.getZone("America/Los_Angeles");

        assertEquals("America/Los_Angeles", tz.getID());
        assertEquals(false, tz.isFixed());
        TestBuilder.testForwardTransitions(tz, TestBuilder.AMERICA_LOS_ANGELES_DATA);
        TestBuilder.testReverseTransitions(tz, TestBuilder.AMERICA_LOS_ANGELES_DATA);
    }

// org.joda.time.tz.TestCompiler::test_2400_fromDay
    public void test_2400_fromDay() {
        StringTokenizer st = new StringTokenizer("Apr Sun>=1  24:00");
        DateTimeOfYear test = new DateTimeOfYear(st);
        assertEquals(4, test.iMonthOfYear);  
        assertEquals(2, test.iDayOfMonth);   
        assertEquals(1, test.iDayOfWeek);    
        assertEquals(0, test.iMillisOfDay);  
        assertEquals(true, test.iAdvanceDayOfWeek);
    }

// org.joda.time.tz.TestCompiler::test_2400_last
    public void test_2400_last() {
        StringTokenizer st = new StringTokenizer("Mar lastSun 24:00");
        DateTimeOfYear test = new DateTimeOfYear(st);
        assertEquals(4, test.iMonthOfYear);  
        assertEquals(1, test.iDayOfMonth);   
        assertEquals(1, test.iDayOfWeek);    
        assertEquals(0, test.iMillisOfDay);  
        assertEquals(false, test.iAdvanceDayOfWeek);
    }

// org.joda.time.tz.TestCompiler::test_Amman_2003
    public void test_Amman_2003() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Amman");
        DateTime dt = new DateTime(2003, 3, 1, 0, 0, zone);
        long next = zone.nextTransition(dt.getMillis());
        assertEquals(next, new DateTime(2003, 3, 28, 0, 0, DateTimeZone.forOffsetHours(2)).getMillis());
    }

// org.joda.time.tz.TestCompiler::test_Amman_2004
    public void test_Amman_2004() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Amman");
        DateTime dt = new DateTime(2004, 3, 1, 0, 0, zone);
        long next = zone.nextTransition(dt.getMillis());
        assertEquals(next, new DateTime(2004, 3, 26, 0, 0, DateTimeZone.forOffsetHours(2)).getMillis());
    }

// org.joda.time.tz.TestCompiler::test_Amman_2005
    public void test_Amman_2005() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Amman");
        DateTime dt = new DateTime(2005, 3, 1, 0, 0, zone);
        long next = zone.nextTransition(dt.getMillis());
        assertEquals(next, new DateTime(2005, 4, 1, 0, 0, DateTimeZone.forOffsetHours(2)).getMillis());
    }

// org.joda.time.tz.TestCompiler::test_Amman_2006
    public void test_Amman_2006() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Amman");
        DateTime dt = new DateTime(2006, 3, 1, 0, 0, zone);
        long next = zone.nextTransition(dt.getMillis());
        assertEquals(next, new DateTime(2006, 3, 31, 0, 0, DateTimeZone.forOffsetHours(2)).getMillis());
    }
