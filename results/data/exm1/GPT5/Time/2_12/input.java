// buggy code
    public int compareTo(DurationField durationField) {
        return 0;
    }

    public Partial(DateTimeFieldType[] types, int[] values, Chronology chronology) {
        super();
        chronology = DateTimeUtils.getChronology(chronology).withUTC();
        iChronology = chronology;
        if (types == null) {
            throw new IllegalArgumentException("Types array must not be null");
        }
        if (values == null) {
            throw new IllegalArgumentException("Values array must not be null");
        }
        if (values.length != types.length) {
            throw new IllegalArgumentException("Values array must be the same length as the types array");
        }
        if (types.length == 0) {
            iTypes = types;
            iValues = values;
            return;
        }
        for (int i = 0; i < types.length; i++) {
            if (types[i] == null) {
                throw new IllegalArgumentException("Types array must not contain null: index " + i);
            }
        }
        DurationField lastUnitField = null;
        for (int i = 0; i < types.length; i++) {
            DateTimeFieldType loopType = types[i];
            DurationField loopUnitField = loopType.getDurationType().getField(iChronology);
            if (i > 0) {
                int compare = lastUnitField.compareTo(loopUnitField);
                if (compare < 0 || (compare != 0 && loopUnitField.isSupported() == false)) {
                    throw new IllegalArgumentException("Types array must be in order largest-smallest: " +
                            types[i - 1].getName() + " < " + loopType.getName());
                } else if (compare == 0) {
                    if (types[i - 1].getRangeDurationType() == null) {
                        if (loopType.getRangeDurationType() == null) {
                            throw new IllegalArgumentException("Types array must not contain duplicate: " +
                                            types[i - 1].getName() + " and " + loopType.getName());
                        }
                    } else {
                        if (loopType.getRangeDurationType() == null) {
                            throw new IllegalArgumentException("Types array must be in order largest-smallest: " +
                                    types[i - 1].getName() + " < " + loopType.getName());
                        }
                        DurationField lastRangeField = types[i - 1].getRangeDurationType().getField(iChronology);
                        DurationField loopRangeField = loopType.getRangeDurationType().getField(iChronology);
                        if (lastRangeField.compareTo(loopRangeField) < 0) {
                            throw new IllegalArgumentException("Types array must be in order largest-smallest: " +
                                    types[i - 1].getName() + " < " + loopType.getName());
                        }
                        if (lastRangeField.compareTo(loopRangeField) == 0) {
                            throw new IllegalArgumentException("Types array must not contain duplicate: " +
                                            types[i - 1].getName() + " and " + loopType.getName());
                        }
                    }
                }
            }
            lastUnitField = loopUnitField;
        }
        
        iTypes = (DateTimeFieldType[]) types.clone();
        chronology.validate(this, values);
        iValues = (int[]) values.clone();
    }

    public Partial with(DateTimeFieldType fieldType, int value) {
        if (fieldType == null) {
            throw new IllegalArgumentException("The field type must not be null");
        }
        int index = indexOf(fieldType);
        if (index == -1) {
            DateTimeFieldType[] newTypes = new DateTimeFieldType[iTypes.length + 1];
            int[] newValues = new int[newTypes.length];
            
            // find correct insertion point to keep largest-smallest order
            int i = 0;
            DurationField unitField = fieldType.getDurationType().getField(iChronology);
            if (unitField.isSupported()) {
                for (; i < iTypes.length; i++) {
                    DateTimeFieldType loopType = iTypes[i];
                    DurationField loopUnitField = loopType.getDurationType().getField(iChronology);
                    if (loopUnitField.isSupported()) {
                        int compare = unitField.compareTo(loopUnitField);
                        if (compare > 0) {
                            break;
                        } else if (compare == 0) {
                            DurationField rangeField = fieldType.getRangeDurationType().getField(iChronology);
                            DurationField loopRangeField = loopType.getRangeDurationType().getField(iChronology);
                            if (rangeField.compareTo(loopRangeField) > 0) {
                                break;
                            }
                        }
                    }
                }
            }
            System.arraycopy(iTypes, 0, newTypes, 0, i);
            System.arraycopy(iValues, 0, newValues, 0, i);
            newTypes[i] = fieldType;
            newValues[i] = value;
            System.arraycopy(iTypes, i, newTypes, i + 1, newTypes.length - i - 1);
            System.arraycopy(iValues, i, newValues, i + 1, newValues.length - i - 1);
            // use public constructor to ensure full validation
            // this isn't overly efficient, but is safe
            Partial newPartial = new Partial(newTypes, newValues, iChronology);
            iChronology.validate(newPartial, newValues);
            return newPartial;
        }
        if (value == getValue(index)) {
            return this;
        }
        int[] newValues = getValues();
        newValues = getField(index).set(this, index, newValues, value);
        return new Partial(this, newValues);
    }

// relevant test
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

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthOnly
    public void testParseInto_monthOnly() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 9, 12, 20, 30, 0, LONDON);
        assertEquals(1, f.parseInto(result, "5", 0));
        assertEquals(new MutableDateTime(2004, 5, 9, 12, 20, 30, 0, LONDON), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthOnly_baseStartYear
    public void testParseInto_monthOnly_baseStartYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 1, 12, 20, 30, 0, TOKYO);
        assertEquals(1, f.parseInto(result, "5", 0));
        assertEquals(new MutableDateTime(2004, 5, 1, 12, 20, 30, 0, TOKYO), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthOnly_parseStartYear
    public void testParseInto_monthOnly_parseStartYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 2, 1, 12, 20, 30, 0, TOKYO);
        assertEquals(1, f.parseInto(result, "1", 0));
        assertEquals(new MutableDateTime(2004, 1, 1, 12, 20, 30, 0, TOKYO), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthOnly_baseEndYear
    public void testParseInto_monthOnly_baseEndYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 12, 31, 12, 20, 30, 0, TOKYO);
        assertEquals(1, f.parseInto(result, "5", 0));
        assertEquals(new MutableDateTime(2004, 5, 31, 12, 20, 30, 0, TOKYO), result);
   }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthOnly_parseEndYear
    public void testParseInto_monthOnly_parseEndYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 31, 12, 20, 30, 0,TOKYO);
        assertEquals(2, f.parseInto(result, "12", 0));
        assertEquals(new MutableDateTime(2004, 12, 31, 12, 20, 30, 0, TOKYO), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_feb29
    public void testParseInto_monthDay_feb29() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 9, 12, 20, 30, 0, LONDON);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 12, 20, 30, 0, LONDON), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_feb29_startOfYear
    public void testParseInto_monthDay_feb29_startOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 1, 0, 0, 0, 0, LONDON);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 0, 0, 0, 0, LONDON), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_feb29_OfYear
    public void testParseInto_monthDay_feb29_OfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 12, 31, 23, 59, 59, 999, LONDON);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 23, 59, 59, 999, LONDON), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_feb29_newYork
    public void testParseInto_monthDay_feb29_newYork() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 9, 12, 20, 30, 0, NEWYORK);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 12, 20, 30, 0, NEWYORK), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_feb29_newYork_startOfYear
    public void testParseInto_monthDay_feb29_newYork_startOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 1, 0, 0, 0, 0, NEWYORK);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 0, 0, 0, 0, NEWYORK), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_feb29_newYork_endOfYear
    public void testParseInto_monthDay_feb29_newYork_endOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 12, 31, 23, 59, 59, 999, NEWYORK);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 23, 59, 59, 999, NEWYORK), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_feb29_tokyo
    public void testParseInto_monthDay_feb29_tokyo() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 9, 12, 20, 30, 0, TOKYO);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 12, 20, 30, 0, TOKYO), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_feb29_tokyo_startOfYear
    public void testParseInto_monthDay_feb29_tokyo_startOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 1, 1, 0, 0, 0, 0, TOKYO);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 0, 0, 0, 0, TOKYO), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_feb29_tokyo_endOfYear
    public void testParseInto_monthDay_feb29_tokyo_endOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        MutableDateTime result = new MutableDateTime(2004, 12, 31, 23, 59, 59, 999, TOKYO);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 23, 59, 59, 999, TOKYO), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_withDefaultYear_feb29
    public void testParseInto_monthDay_withDefaultYear_feb29() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withDefaultYear(2012);
        MutableDateTime result = new MutableDateTime(2004, 1, 9, 12, 20, 30, 0, LONDON);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 12, 20, 30, 0, LONDON), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_withDefaultYear_feb29_newYork
    public void testParseInto_monthDay_withDefaultYear_feb29_newYork() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withDefaultYear(2012);
        MutableDateTime result = new MutableDateTime(2004, 1, 9, 12, 20, 30, 0, NEWYORK);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 12, 20, 30, 0, NEWYORK), result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_monthDay_withDefaultYear_feb29_newYork_endOfYear
    public void testParseInto_monthDay_withDefaultYear_feb29_newYork_endOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withDefaultYear(2012);
        MutableDateTime result = new MutableDateTime(2004, 12, 9, 12, 20, 30, 0, NEWYORK);
        assertEquals(4, f.parseInto(result, "2 29", 0));
        assertEquals(new MutableDateTime(2004, 2, 29, 12, 20, 30, 0, NEWYORK), result);
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
        DateTimeFormatter f = bld2.toFormatter();
        assertEquals(true, f.isPrinter());
        assertEquals(false, f.isParser());
        assertEquals("XYZ", f.print(0L));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_nullPrinter
    public void test_append_nullPrinter() {
        try {
            DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
            bld2.append((DateTimePrinter) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_Parser
    public void test_append_Parser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimeParser p = bld.toParser();
        
        DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
        bld2.appendLiteral('X');
        bld2.append(p);
        bld2.appendLiteral('Z');
        DateTimeFormatter f = bld2.toFormatter();
        assertEquals(false, f.isPrinter());
        assertEquals(true, f.isParser());
        assertEquals(0, f.withZoneUTC().parseMillis("XYZ"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_nullParser
    public void test_append_nullParser() {
        try {
            DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
            bld2.append((DateTimeParser) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_Printer_nullParser
    public void test_append_Printer_nullParser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimePrinter p = bld.toPrinter();
        
        try {
            DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
            bld2.append(p, (DateTimeParser) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_nullPrinter_Parser
    public void test_append_nullPrinter_Parser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimeParser p = bld.toParser();
        
        try {
            DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
            bld2.append((DateTimePrinter) null, p);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_appendOptional_Parser
    public void test_appendOptional_Parser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimeParser p = bld.toParser();
        
        DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
        bld2.appendLiteral('X');
        bld2.appendOptional(p);
        bld2.appendLiteral('Z');
        DateTimeFormatter f = bld2.toFormatter();
        assertEquals(false, f.isPrinter());
        assertEquals(true, f.isParser());
        assertEquals(0, f.withZoneUTC().parseMillis("XYZ"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_appendOptional_nullParser
    public void test_appendOptional_nullParser() {
        try {
            DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
            bld2.appendOptional((DateTimeParser) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
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

// org.joda.time.format.TestDateTimeFormatterBuilder::test_appendTimeZoneId
    public void test_appendTimeZoneId() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        assertEquals("Asia/Tokyo", f.print(new DateTime(2007, 3, 4, 0, 0, 0, TOKYO)));
        assertEquals(TOKYO, f.parseDateTime("Asia/Tokyo").getZone());
        try {
            f.parseDateTime("Nonsense");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseZoneTokyo
    public void test_printParseZoneTokyo() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, TOKYO);
        assertEquals("2007-03-04 12:30 Asia/Tokyo", f.print(dt));
        assertEquals(dt, f.parseDateTime("2007-03-04 12:30 Asia/Tokyo"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseZoneParis
    public void test_printParseZoneParis() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, PARIS);
        assertEquals("2007-03-04 12:30 Europe/Paris", f.print(dt));
        assertEquals(dt, f.parseDateTime("2007-03-04 12:30 Europe/Paris"));
        assertEquals(dt, f.withOffsetParsed().parseDateTime("2007-03-04 12:30 Europe/Paris"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseZoneDawsonCreek
    public void test_printParseZoneDawsonCreek() {  
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, DateTimeZone.forID("America/Dawson_Creek"));
        assertEquals("2007-03-04 12:30 America/Dawson_Creek", f.print(dt));
        assertEquals(dt, f.parseDateTime("2007-03-04 12:30 America/Dawson_Creek"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseZoneBahiaBanderas
    public void test_printParseZoneBahiaBanderas() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, DateTimeZone.forID("America/Bahia_Banderas"));
        assertEquals("2007-03-04 12:30 America/Bahia_Banderas", f.print(dt));
        assertEquals(dt, f.parseDateTime("2007-03-04 12:30 America/Bahia_Banderas"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseOffset
    public void test_printParseOffset() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneOffset("Z", true, 2, 2);
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, TOKYO);
        assertEquals("2007-03-04 12:30 +09:00", f.print(dt));
        assertEquals(dt.withZone(DateTimeZone.getDefault()), f.parseDateTime("2007-03-04 12:30 +09:00"));
        assertEquals(dt, f.withZone(TOKYO).parseDateTime("2007-03-04 12:30 +09:00"));
        assertEquals(dt.withZone(DateTimeZone.forOffsetHours(9)), f.withOffsetParsed().parseDateTime("2007-03-04 12:30 +09:00"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseOffsetAndZone
    public void test_printParseOffsetAndZone() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneOffset("Z", true, 2, 2).appendLiteral(' ').appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, TOKYO);
        assertEquals("2007-03-04 12:30 +09:00 Asia/Tokyo", f.print(dt));
        assertEquals(dt, f.withZone(TOKYO).parseDateTime("2007-03-04 12:30 +09:00 Asia/Tokyo"));
        assertEquals(dt.withZone(PARIS), f.withZone(PARIS).parseDateTime("2007-03-04 12:30 +09:00 Asia/Tokyo"));
        assertEquals(dt.withZone(DateTimeZone.forOffsetHours(9)), f.withOffsetParsed().parseDateTime("2007-03-04 12:30 +09:00 Asia/Tokyo"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_parseWrongOffset
    public void test_parseWrongOffset() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneOffset("Z", true, 2, 2);
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime expected = new DateTime(2007, 3, 4, 12, 30, 0, DateTimeZone.forOffsetHours(7));
        
        assertEquals(expected.withZone(TOKYO), f.withZone(TOKYO).parseDateTime("2007-03-04 12:30 +07:00"));
        
        assertEquals(expected, f.withOffsetParsed().parseDateTime("2007-03-04 12:30 +07:00"));
        
        assertEquals(expected.withZone(DateTimeZone.getDefault()), f.parseDateTime("2007-03-04 12:30 +07:00"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_parseWrongOffsetAndZone
    public void test_parseWrongOffsetAndZone() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneOffset("Z", true, 2, 2).appendLiteral(' ').appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime expected = new DateTime(2007, 3, 4, 12, 30, 0, DateTimeZone.forOffsetHours(7));
        
        assertEquals(expected.withZone(TOKYO), f.parseDateTime("2007-03-04 12:30 +07:00 Asia/Tokyo"));
        
        assertEquals(expected.withZone(TOKYO), f.withZone(TOKYO).parseDateTime("2007-03-04 12:30 +07:00 Asia/Tokyo"));
        
        assertEquals(expected, f.withOffsetParsed().parseDateTime("2007-03-04 12:30 +07:00 Asia/Tokyo"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_localPrintParseZoneTokyo
    public void test_localPrintParseZoneTokyo() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, TOKYO);
        assertEquals("2007-03-04 12:30 Asia/Tokyo", f.print(dt));
        
        LocalDateTime expected = new LocalDateTime(2007, 3, 4, 12, 30);
        assertEquals(expected, f.parseLocalDateTime("2007-03-04 12:30 Asia/Tokyo"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_localPrintParseOffset
    public void test_localPrintParseOffset() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneOffset("Z", true, 2, 2);
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, TOKYO);
        assertEquals("2007-03-04 12:30 +09:00", f.print(dt));
        
        LocalDateTime expected = new LocalDateTime(2007, 3, 4, 12, 30);
        assertEquals(expected, f.parseLocalDateTime("2007-03-04 12:30 +09:00"));
        assertEquals(expected, f.withZone(TOKYO).parseLocalDateTime("2007-03-04 12:30 +09:00"));
        assertEquals(expected, f.withOffsetParsed().parseLocalDateTime("2007-03-04 12:30 +09:00"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_localPrintParseOffsetAndZone
    public void test_localPrintParseOffsetAndZone() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneOffset("Z", true, 2, 2).appendLiteral(' ').appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, TOKYO);
        assertEquals("2007-03-04 12:30 +09:00 Asia/Tokyo", f.print(dt));
        
        LocalDateTime expected = new LocalDateTime(2007, 3, 4, 12, 30);
        assertEquals(expected, f.withZone(TOKYO).parseLocalDateTime("2007-03-04 12:30 +09:00 Asia/Tokyo"));
        assertEquals(expected, f.withZone(PARIS).parseLocalDateTime("2007-03-04 12:30 +09:00 Asia/Tokyo"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_localParseWrongOffsetAndZone
    public void test_localParseWrongOffsetAndZone() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneOffset("Z", true, 2, 2).appendLiteral(' ').appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        LocalDateTime expected = new LocalDateTime(2007, 3, 4, 12, 30);
        
        assertEquals(expected, f.parseLocalDateTime("2007-03-04 12:30 +07:00 Asia/Tokyo"));
        
        assertEquals(expected, f.withZone(TOKYO).parseLocalDateTime("2007-03-04 12:30 +07:00 Asia/Tokyo"));
        
        assertEquals(expected, f.withOffsetParsed().parseLocalDateTime("2007-03-04 12:30 +07:00 Asia/Tokyo"));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseShortName
    public void test_printParseShortName() {}

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseShortNameWithLookup
    public void test_printParseShortNameWithLookup() {}

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseShortNameWithAutoLookup
    public void test_printParseShortNameWithAutoLookup() {}

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseLongName
    public void test_printParseLongName() {}

// org.joda.time.format.TestDateTimeFormatterBuilder::test_printParseLongNameWithLookup
    public void test_printParseLongNameWithLookup() {}

// org.joda.time.format.TestISODateTimeFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        ISODateTimeFormat f = new ISODateTimeFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_date
    public void testFormat_date() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09", ISODateTimeFormat.date().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09", ISODateTimeFormat.date().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09", ISODateTimeFormat.date().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_date_partial
    public void testFormat_date_partial() {
        Partial dt = new Partial(
                new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()},
                new int[] {2004, 6, 9});
        assertEquals("2004-06-09", ISODateTimeFormat.date().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_time
    public void testFormat_time() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30.040Z", ISODateTimeFormat.time().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30.040+01:00", ISODateTimeFormat.time().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30.040+02:00", ISODateTimeFormat.time().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_time_partial
    public void testFormat_time_partial() {
        Partial dt = new Partial(
                new DateTimeFieldType[] {DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour(),
                        DateTimeFieldType.secondOfMinute(), DateTimeFieldType.millisOfSecond()},
                new int[] {10, 20, 30, 40});
        assertEquals("10:20:30.040", ISODateTimeFormat.time().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_timeNoMillis
    public void testFormat_timeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30Z", ISODateTimeFormat.timeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30+01:00", ISODateTimeFormat.timeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30+02:00", ISODateTimeFormat.timeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_timeNoMillis_partial
    public void testFormat_timeNoMillis_partial() {
        Partial dt = new Partial(
                new DateTimeFieldType[] {DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour(),
                        DateTimeFieldType.secondOfMinute(), DateTimeFieldType.millisOfSecond()},
                new int[] {10, 20, 30, 40});
        assertEquals("10:20:30", ISODateTimeFormat.timeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_tTime
    public void testFormat_tTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("T10:20:30.040Z", ISODateTimeFormat.tTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("T11:20:30.040+01:00", ISODateTimeFormat.tTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("T12:20:30.040+02:00", ISODateTimeFormat.tTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_tTimeNoMillis
    public void testFormat_tTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("T10:20:30Z", ISODateTimeFormat.tTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("T11:20:30+01:00", ISODateTimeFormat.tTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("T12:20:30+02:00", ISODateTimeFormat.tTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateTime
    public void testFormat_dateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30.040Z", ISODateTimeFormat.dateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30.040+01:00", ISODateTimeFormat.dateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30.040+02:00", ISODateTimeFormat.dateTime().print(dt));
        

    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateTimeNoMillis
    public void testFormat_dateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30Z", ISODateTimeFormat.dateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30+01:00", ISODateTimeFormat.dateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30+02:00", ISODateTimeFormat.dateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_ordinalDate
    public void testFormat_ordinalDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-161", ISODateTimeFormat.ordinalDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-161", ISODateTimeFormat.ordinalDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-161", ISODateTimeFormat.ordinalDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_ordinalDateTime
    public void testFormat_ordinalDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-161T10:20:30.040Z", ISODateTimeFormat.ordinalDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-161T11:20:30.040+01:00", ISODateTimeFormat.ordinalDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-161T12:20:30.040+02:00", ISODateTimeFormat.ordinalDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_ordinalDateTimeNoMillis
    public void testFormat_ordinalDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-161T10:20:30Z", ISODateTimeFormat.ordinalDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-161T11:20:30+01:00", ISODateTimeFormat.ordinalDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-161T12:20:30+02:00", ISODateTimeFormat.ordinalDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekDate
    public void testFormat_weekDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekDateTime
    public void testFormat_weekDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24-3T10:20:30.040Z", ISODateTimeFormat.weekDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24-3T11:20:30.040+01:00", ISODateTimeFormat.weekDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24-3T12:20:30.040+02:00", ISODateTimeFormat.weekDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekDateTimeNoMillis
    public void testFormat_weekDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24-3T10:20:30Z", ISODateTimeFormat.weekDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24-3T11:20:30+01:00", ISODateTimeFormat.weekDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24-3T12:20:30+02:00", ISODateTimeFormat.weekDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicDate
    public void testFormat_basicDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("20040609", ISODateTimeFormat.basicDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("20040609", ISODateTimeFormat.basicDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("20040609", ISODateTimeFormat.basicDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicTime
    public void testFormat_basicTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("102030.040Z", ISODateTimeFormat.basicTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("112030.040+0100", ISODateTimeFormat.basicTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("122030.040+0200", ISODateTimeFormat.basicTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicTimeNoMillis
    public void testFormat_basicTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("102030Z", ISODateTimeFormat.basicTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("112030+0100", ISODateTimeFormat.basicTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("122030+0200", ISODateTimeFormat.basicTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicTTime
    public void testFormat_basicTTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("T102030.040Z", ISODateTimeFormat.basicTTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("T112030.040+0100", ISODateTimeFormat.basicTTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("T122030.040+0200", ISODateTimeFormat.basicTTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicTTimeNoMillis
    public void testFormat_basicTTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("T102030Z", ISODateTimeFormat.basicTTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("T112030+0100", ISODateTimeFormat.basicTTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("T122030+0200", ISODateTimeFormat.basicTTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicDateTime
    public void testFormat_basicDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("20040609T102030.040Z", ISODateTimeFormat.basicDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("20040609T112030.040+0100", ISODateTimeFormat.basicDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("20040609T122030.040+0200", ISODateTimeFormat.basicDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicDateTimeNoMillis
    public void testFormat_basicDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("20040609T102030Z", ISODateTimeFormat.basicDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("20040609T112030+0100", ISODateTimeFormat.basicDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("20040609T122030+0200", ISODateTimeFormat.basicDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicOrdinalDate
    public void testFormat_basicOrdinalDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004161", ISODateTimeFormat.basicOrdinalDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004161", ISODateTimeFormat.basicOrdinalDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004161", ISODateTimeFormat.basicOrdinalDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicOrdinalDateTime
    public void testFormat_basicOrdinalDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004161T102030.040Z", ISODateTimeFormat.basicOrdinalDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004161T112030.040+0100", ISODateTimeFormat.basicOrdinalDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004161T122030.040+0200", ISODateTimeFormat.basicOrdinalDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicOrdinalDateTimeNoMillis
    public void testFormat_basicOrdinalDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004161T102030Z", ISODateTimeFormat.basicOrdinalDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004161T112030+0100", ISODateTimeFormat.basicOrdinalDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004161T122030+0200", ISODateTimeFormat.basicOrdinalDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicWeekDate
    public void testFormat_basicWeekDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004W243", ISODateTimeFormat.basicWeekDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004W243", ISODateTimeFormat.basicWeekDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004W243", ISODateTimeFormat.basicWeekDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicWeekDateTime
    public void testFormat_basicWeekDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004W243T102030.040Z", ISODateTimeFormat.basicWeekDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004W243T112030.040+0100", ISODateTimeFormat.basicWeekDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004W243T122030.040+0200", ISODateTimeFormat.basicWeekDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicWeekDateTimeNoMillis
    public void testFormat_basicWeekDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004W243T102030Z", ISODateTimeFormat.basicWeekDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004W243T112030+0100", ISODateTimeFormat.basicWeekDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004W243T122030+0200", ISODateTimeFormat.basicWeekDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_year
    public void testFormat_year() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004", ISODateTimeFormat.year().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004", ISODateTimeFormat.year().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004", ISODateTimeFormat.year().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_yearMonth
    public void testFormat_yearMonth() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06", ISODateTimeFormat.yearMonth().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06", ISODateTimeFormat.yearMonth().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06", ISODateTimeFormat.yearMonth().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_yearMonthDay
    public void testFormat_yearMonthDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09", ISODateTimeFormat.yearMonthDay().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09", ISODateTimeFormat.yearMonthDay().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09", ISODateTimeFormat.yearMonthDay().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekyear
    public void testFormat_weekyear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004", ISODateTimeFormat.weekyear().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004", ISODateTimeFormat.weekyear().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004", ISODateTimeFormat.weekyear().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekyearWeek
    public void testFormat_weekyearWeek() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24", ISODateTimeFormat.weekyearWeek().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24", ISODateTimeFormat.weekyearWeek().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24", ISODateTimeFormat.weekyearWeek().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekyearWeekDay
    public void testFormat_weekyearWeekDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekyearWeekDay().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekyearWeekDay().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekyearWeekDay().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hour
    public void testFormat_hour() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10", ISODateTimeFormat.hour().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11", ISODateTimeFormat.hour().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12", ISODateTimeFormat.hour().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hourMinute
    public void testFormat_hourMinute() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20", ISODateTimeFormat.hourMinute().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20", ISODateTimeFormat.hourMinute().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20", ISODateTimeFormat.hourMinute().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hourMinuteSecond
    public void testFormat_hourMinuteSecond() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30", ISODateTimeFormat.hourMinuteSecond().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30", ISODateTimeFormat.hourMinuteSecond().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30", ISODateTimeFormat.hourMinuteSecond().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hourMinuteSecondMillis
    public void testFormat_hourMinuteSecondMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30.040", ISODateTimeFormat.hourMinuteSecondMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30.040", ISODateTimeFormat.hourMinuteSecondMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30.040", ISODateTimeFormat.hourMinuteSecondMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hourMinuteSecondFraction
    public void testFormat_hourMinuteSecondFraction() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30.040", ISODateTimeFormat.hourMinuteSecondFraction().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30.040", ISODateTimeFormat.hourMinuteSecondFraction().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30.040", ISODateTimeFormat.hourMinuteSecondFraction().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHour
    public void testFormat_dateHour() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10", ISODateTimeFormat.dateHour().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11", ISODateTimeFormat.dateHour().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12", ISODateTimeFormat.dateHour().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHourMinute
    public void testFormat_dateHourMinute() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20", ISODateTimeFormat.dateHourMinute().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20", ISODateTimeFormat.dateHourMinute().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20", ISODateTimeFormat.dateHourMinute().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHourMinuteSecond
    public void testFormat_dateHourMinuteSecond() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30", ISODateTimeFormat.dateHourMinuteSecond().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30", ISODateTimeFormat.dateHourMinuteSecond().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30", ISODateTimeFormat.dateHourMinuteSecond().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHourMinuteSecondMillis
    public void testFormat_dateHourMinuteSecondMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30.040", ISODateTimeFormat.dateHourMinuteSecondMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30.040", ISODateTimeFormat.dateHourMinuteSecondMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30.040", ISODateTimeFormat.dateHourMinuteSecondMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHourMinuteSecondFraction
    public void testFormat_dateHourMinuteSecondFraction() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30.040", ISODateTimeFormat.dateHourMinuteSecondFraction().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30.040", ISODateTimeFormat.dateHourMinuteSecondFraction().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30.040", ISODateTimeFormat.dateHourMinuteSecondFraction().print(dt));
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

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_null
    public void testForFields_null() {
        try {
            ISODateTimeFormat.forFields((Collection) null, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_empty
    public void testForFields_empty() {
        try {
            ISODateTimeFormat.forFields(new ArrayList(), true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_calBased_YMD
    public void testForFields_calBased_YMD() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
                DateTimeFieldType.monthOfYear(),
                DateTimeFieldType.dayOfMonth(),
        };
        int[] values = new int[] {2005, 6, 25};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-06-25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005-06-25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("20050625", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("20050625", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_calBased_YMD_unmodifiable
    public void testForFields_calBased_YMD_unmodifiable() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
                DateTimeFieldType.monthOfYear(),
                DateTimeFieldType.dayOfMonth(),
        };
        int[] values = new int[] {2005, 6, 25};
        List types = Collections.unmodifiableList(new ArrayList(Arrays.asList(fields)));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-06-25", f.print(new Partial(fields, values)));
        assertEquals(3, types.size());
        
        types = Arrays.asList(fields);
        f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-06-25", f.print(new Partial(fields, values)));
        assertEquals(3, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_calBased_YMD_duplicates
    public void testForFields_calBased_YMD_duplicates() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
                DateTimeFieldType.monthOfYear(),
                DateTimeFieldType.dayOfMonth(),
        };
        DateTimeFieldType[] dupFields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
                DateTimeFieldType.monthOfYear(),
                DateTimeFieldType.dayOfMonth(),
                DateTimeFieldType.monthOfYear(),
        };
        int[] values = new int[] {2005, 6, 25};
        List types = new ArrayList(Arrays.asList(dupFields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-06-25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = Arrays.asList(dupFields);
        f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-06-25", f.print(new Partial(fields, values)));
        assertEquals(4, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_calBased_Y
    public void testForFields_calBased_Y() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
        };
        int[] values = new int[] {2005};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_calBased_M
    public void testForFields_calBased_M() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.monthOfYear(),
        };
        int[] values = new int[] {6};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("--06", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("--06", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("--06", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("--06", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_calBased_D
    public void testForFields_calBased_D() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.dayOfMonth(),
        };
        int[] values = new int[] {25};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("---25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("---25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("---25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("---25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_calBased_YM
    public void testForFields_calBased_YM() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
                DateTimeFieldType.monthOfYear(),
        };
        int[] values = new int[] {2005, 6};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-06", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005-06", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("2005-06", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005-06", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_calBased_MD
    public void testForFields_calBased_MD() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.monthOfYear(),
                DateTimeFieldType.dayOfMonth(),
        };
        int[] values = new int[] {6, 25};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("--06-25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("--06-25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("--0625", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("--0625", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_calBased_YD
    public void testForFields_calBased_YD() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
                DateTimeFieldType.dayOfMonth(),
        };
        int[] values = new int[] {2005, 25};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005--25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005--25", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_weekBased_YWD
    public void testForFields_weekBased_YWD() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.weekyear(),
                DateTimeFieldType.weekOfWeekyear(),
                DateTimeFieldType.dayOfWeek(),
        };
        int[] values = new int[] {2005, 8, 5};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-W08-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005-W08-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("2005W085", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005W085", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_weekBased_Y
    public void testForFields_weekBased_Y() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.weekyear(),
        };
        int[] values = new int[] {2005};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_weekBased_W
    public void testForFields_weekBased_W() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.weekOfWeekyear(),
        };
        int[] values = new int[] {8};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("-W08", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("-W08", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("-W08", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("-W08", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_weekBased_D
    public void testForFields_weekBased_D() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.dayOfWeek(),
        };
        int[] values = new int[] {5};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("-W-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("-W-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("-W-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("-W-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_weekBased_YW
    public void testForFields_weekBased_YW() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.weekyear(),
                DateTimeFieldType.weekOfWeekyear(),
        };
        int[] values = new int[] {2005, 8};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-W08", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005-W08", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("2005W08", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005W08", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_weekBased_WD
    public void testForFields_weekBased_WD() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.weekOfWeekyear(),
                DateTimeFieldType.dayOfWeek(),
        };
        int[] values = new int[] {8, 5};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("-W08-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("-W08-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("-W085", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("-W085", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_weekBased_YD
    public void testForFields_weekBased_YD() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.weekyear(),
                DateTimeFieldType.dayOfWeek(),
        };
        int[] values = new int[] {2005, 5};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005-W-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005W-5", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_ordinalBased_YD
    public void testForFields_ordinalBased_YD() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
                DateTimeFieldType.dayOfYear(),
        };
        int[] values = new int[] {2005, 177};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-177", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005-177", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("2005177", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005177", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_ordinalBased_Y
    public void testForFields_ordinalBased_Y() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
        };
        int[] values = new int[] {2005};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_ordinalBased_D
    public void testForFields_ordinalBased_D() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.dayOfYear(),
        };
        int[] values = new int[] {177};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("-177", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("-177", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("-177", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("-177", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_HMSm
    public void testForFields_time_HMSm() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.hourOfDay(),
                DateTimeFieldType.minuteOfHour(),
                DateTimeFieldType.secondOfMinute(),
                DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {10, 20, 30, 40};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("10:20:30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("10:20:30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("102030.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("102030.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_HMS
    public void testForFields_time_HMS() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.hourOfDay(),
                DateTimeFieldType.minuteOfHour(),
                DateTimeFieldType.secondOfMinute(),
        };
        int[] values = new int[] {10, 20, 30};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("10:20:30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("10:20:30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("102030", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("102030", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_HM
    public void testForFields_time_HM() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.hourOfDay(),
                DateTimeFieldType.minuteOfHour(),
        };
        int[] values = new int[] {10, 20};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("10:20", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("10:20", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("1020", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("1020", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_H
    public void testForFields_time_H() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.hourOfDay(),
        };
        int[] values = new int[] {10};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("10", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("10", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("10", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("10", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_MSm
    public void testForFields_time_MSm() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.minuteOfHour(),
                DateTimeFieldType.secondOfMinute(),
                DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {20, 30, 40};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("-20:30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("-20:30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("-2030.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("-2030.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_MS
    public void testForFields_time_MS() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.minuteOfHour(),
                DateTimeFieldType.secondOfMinute(),
        };
        int[] values = new int[] {20, 30};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("-20:30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("-20:30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("-2030", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("-2030", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_M
    public void testForFields_time_M() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.minuteOfHour(),
        };
        int[] values = new int[] {20};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("-20", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("-20", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("-20", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("-20", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_Sm
    public void testForFields_time_Sm() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.secondOfMinute(),
                DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {30, 40};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("--30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("--30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("--30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("--30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_S
    public void testForFields_time_S() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.secondOfMinute(),
        };
        int[] values = new int[] {30};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("--30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("--30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("--30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("--30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_m
    public void testForFields_time_m() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {40};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("---.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("---.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_Hm
    public void testForFields_time_Hm() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.hourOfDay(),
                DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {10, 40};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("10--.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("10--.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_HS
    public void testForFields_time_HS() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.hourOfDay(),
                DateTimeFieldType.secondOfMinute(),
        };
        int[] values = new int[] {10, 30};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("10-30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("10-30", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_Mm
    public void testForFields_time_Mm() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.minuteOfHour(),
                DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {20, 40};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("-20-.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("-20-.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_HSm
    public void testForFields_time_HSm() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.hourOfDay(),
                DateTimeFieldType.secondOfMinute(),
                DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {10, 30, 40};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("10-30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("10-30.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_time_HMm
    public void testForFields_time_HMm() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.hourOfDay(),
                DateTimeFieldType.minuteOfHour(),
                DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {10, 20, 40};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("10:20-.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("1020-.040", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_datetime_YMDH
    public void testForFields_datetime_YMDH() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
                DateTimeFieldType.monthOfYear(),
                DateTimeFieldType.dayOfMonth(),
                DateTimeFieldType.hourOfDay(),
        };
        int[] values = new int[] {2005, 6, 25, 12};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("2005-06-25T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005-06-25T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("20050625T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("20050625T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_datetime_DH
    public void testForFields_datetime_DH() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.dayOfMonth(),
                DateTimeFieldType.hourOfDay(),
        };
        int[] values = new int[] {25, 12};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, true);
        assertEquals("---25T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("---25T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, true);
        assertEquals("---25T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("---25T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_datetime_YH
    public void testForFields_datetime_YH() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.year(),
                DateTimeFieldType.hourOfDay(),
        };
        int[] values = new int[] {2005, 12};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("2005T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("2005T12", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISODateTimeFormat_Fields::testForFields_datetime_DM
    public void testForFields_datetime_DM() {
        DateTimeFieldType[] fields = new DateTimeFieldType[] {
                DateTimeFieldType.dayOfMonth(),
                DateTimeFieldType.minuteOfHour(),
        };
        int[] values = new int[] {25, 20};
        List types = new ArrayList(Arrays.asList(fields));
        DateTimeFormatter f = ISODateTimeFormat.forFields(types, true, false);
        assertEquals("---25T-20", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        f = ISODateTimeFormat.forFields(types, false, false);
        assertEquals("---25T-20", f.print(new Partial(fields, values)));
        assertEquals(0, types.size());
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        types = new ArrayList(Arrays.asList(fields));
        try {
            ISODateTimeFormat.forFields(types, false, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        ISOPeriodFormat f = new ISOPeriodFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatStandard
    public void testFormatStandard() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", ISOPeriodFormat.standard().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P1Y2M3W4DT5H6M7S", ISOPeriodFormat.standard().print(p));
        
        p = new Period(0);
        assertEquals("PT0S", ISOPeriodFormat.standard().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("PT0M", ISOPeriodFormat.standard().print(p));
        
        assertEquals("P1Y4DT5H6M7.008S", ISOPeriodFormat.standard().print(YEAR_DAY_PERIOD));
        assertEquals("PT0S", ISOPeriodFormat.standard().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P1Y2M3W4D", ISOPeriodFormat.standard().print(DATE_PERIOD));
        assertEquals("PT5H6M7.008S", ISOPeriodFormat.standard().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatStandard_negative
    public void testFormatStandard_negative() {
        Period p = new Period(-1, -2, -3, -4, -5, -6, -7, -8);
        assertEquals("P-1Y-2M-3W-4DT-5H-6M-7.008S", ISOPeriodFormat.standard().print(p));
        
        p = Period.years(-54);
        assertEquals("P-54Y", ISOPeriodFormat.standard().print(p));
        
        p = Period.seconds(4).withMillis(-8);
        assertEquals("PT3.992S", ISOPeriodFormat.standard().print(p));
        
        p = Period.seconds(-4).withMillis(8);
        assertEquals("PT-3.992S", ISOPeriodFormat.standard().print(p));
        
        p = Period.seconds(-23);
        assertEquals("PT-23S", ISOPeriodFormat.standard().print(p));
        
        p = Period.millis(-8);
        assertEquals("PT-0.008S", ISOPeriodFormat.standard().print(p));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternate
    public void testFormatAlternate() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P00010204T050607.008", ISOPeriodFormat.alternate().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P00010204T050607", ISOPeriodFormat.alternate().print(p));
        
        p = new Period(0);
        assertEquals("P00000000T000000", ISOPeriodFormat.alternate().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P00000000T000000", ISOPeriodFormat.alternate().print(p));
        
        assertEquals("P00010004T050607.008", ISOPeriodFormat.alternate().print(YEAR_DAY_PERIOD));
        assertEquals("P00000000T000000", ISOPeriodFormat.alternate().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P00010204T000000", ISOPeriodFormat.alternate().print(DATE_PERIOD));
        assertEquals("P00000000T050607.008", ISOPeriodFormat.alternate().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternateExtended
    public void testFormatAlternateExtended() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P0001-02-04T05:06:07.008", ISOPeriodFormat.alternateExtended().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P0001-02-04T05:06:07", ISOPeriodFormat.alternateExtended().print(p));
        
        p = new Period(0);
        assertEquals("P0000-00-00T00:00:00", ISOPeriodFormat.alternateExtended().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P0000-00-00T00:00:00", ISOPeriodFormat.alternateExtended().print(p));
        
        assertEquals("P0001-00-04T05:06:07.008", ISOPeriodFormat.alternateExtended().print(YEAR_DAY_PERIOD));
        assertEquals("P0000-00-00T00:00:00", ISOPeriodFormat.alternateExtended().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P0001-02-04T00:00:00", ISOPeriodFormat.alternateExtended().print(DATE_PERIOD));
        assertEquals("P0000-00-00T05:06:07.008", ISOPeriodFormat.alternateExtended().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternateWithWeeks
    public void testFormatAlternateWithWeeks() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P0001W0304T050607.008", ISOPeriodFormat.alternateWithWeeks().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P0001W0304T050607", ISOPeriodFormat.alternateWithWeeks().print(p));
        
        p = new Period(0);
        assertEquals("P0000W0000T000000", ISOPeriodFormat.alternateWithWeeks().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P0000W0000T000000", ISOPeriodFormat.alternateWithWeeks().print(p));
        
        assertEquals("P0001W0004T050607.008", ISOPeriodFormat.alternateWithWeeks().print(YEAR_DAY_PERIOD));
        assertEquals("P0000W0000T000000", ISOPeriodFormat.alternateWithWeeks().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P0001W0304T000000", ISOPeriodFormat.alternateWithWeeks().print(DATE_PERIOD));
        assertEquals("P0000W0000T050607.008", ISOPeriodFormat.alternateWithWeeks().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternateExtendedWithWeeks
    public void testFormatAlternateExtendedWithWeeks() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P0001-W03-04T05:06:07.008", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P0001-W03-04T05:06:07", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        
        p = new Period(0);
        assertEquals("P0000-W00-00T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P0000-W00-00T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        
        assertEquals("P0001-W00-04T05:06:07.008", ISOPeriodFormat.alternateExtendedWithWeeks().print(YEAR_DAY_PERIOD));
        assertEquals("P0000-W00-00T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P0001-W03-04T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(DATE_PERIOD));
        assertEquals("P0000-W00-00T05:06:07.008", ISOPeriodFormat.alternateExtendedWithWeeks().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard1
    public void testParseStandard1() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P1Y2M3W4DT5H6M7.008S");
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard2
    public void testParseStandard2() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0Y0M0W0DT5H6M7.008S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard3
    public void testParseStandard3() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0DT5H6M7.008S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard4
    public void testParseStandard4() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P2Y3DT5H6M7.008S");
        assertEquals(new Period(2, 0, 0, 3, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard5
    public void testParseStandard5() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P2YT5H6M7.008S");
        assertEquals(new Period(2, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard6
    public void testParseStandard6() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("PT5H6M7.008S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard7
    public void testParseStandard7() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P1Y2M3W4D");
        assertEquals(new Period(1, 2, 3, 4, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard8
    public void testParseStandard8() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("PT5H6M7S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard9
    public void testParseStandard9() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("PT0S");
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard10
    public void testParseStandard10() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0D");
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard11
    public void testParseStandard11() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0Y");
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail1
    public void testParseStandardFail1() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("P1Y2S");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail2
    public void testParseStandardFail2() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("PS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail3
    public void testParseStandardFail3() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("PTS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail4
    public void testParseStandardFail4() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("PXS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        PeriodFormat f = new PeriodFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_formatStandard
    public void test_getDefault_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 day, 5 hours, 6 minutes, 7 seconds and 8 milliseconds", PeriodFormat.getDefault().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_FormatOneField
    public void test_getDefault_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 days", PeriodFormat.getDefault().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_formatTwoFields
    public void test_getDefault_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 days and 5 hours", PeriodFormat.getDefault().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_parseOneField
    public void test_getDefault_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.getDefault().parsePeriod("2 days"));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_parseTwoFields
    public void test_getDefault_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.getDefault().parsePeriod("2 days and 5 hours"));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_checkRedundantSeparator
    public void test_getDefault_checkRedundantSeparator() {
        try {
            PeriodFormat.getDefault().parsePeriod("2 days and 5 hours ");
            fail("No exception was caught");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_cached
    public void test_getDefault_cached() {
        assertSame(PeriodFormat.getDefault(), PeriodFormat.getDefault());
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_default
    public void test_wordBased_default() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 Tag, 5 Stunden, 6 Minuten, 7 Sekunden und 8 Millisekunden", PeriodFormat.wordBased().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_formatStandard
    public void test_wordBased_fr_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 jour, 5 heures, 6 minutes, 7 secondes et 8 millisecondes", PeriodFormat.wordBased(FR).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_FormatOneField
    public void test_wordBased_fr_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 jours", PeriodFormat.wordBased(FR).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_formatTwoFields
    public void test_wordBased_fr_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 jours et 5 heures", PeriodFormat.wordBased(FR).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_parseOneField
    public void test_wordBased_fr_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(FR).parsePeriod("2 jours"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_parseTwoFields
    public void test_wordBased_fr_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(FR).parsePeriod("2 jours et 5 heures"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_cached
    public void test_wordBased_fr_cached() {
        assertSame(PeriodFormat.wordBased(FR), PeriodFormat.wordBased(FR));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_formatStandard
    public void test_wordBased_pt_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 dia, 5 horas, 6 minutos, 7 segundos e 8 milissegundos", PeriodFormat.wordBased(PT).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_FormatOneField
    public void test_wordBased_pt_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 dias", PeriodFormat.wordBased(PT).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_formatTwoFields
    public void test_wordBased_pt_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 dias e 5 horas", PeriodFormat.wordBased(PT).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_parseOneField
    public void test_wordBased_pt_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(PT).parsePeriod("2 dias"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_parseTwoFields
    public void test_wordBased_pt_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(PT).parsePeriod("2 dias e 5 horas"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_cached
    public void test_wordBased_pt_cached() {
        assertSame(PeriodFormat.wordBased(PT), PeriodFormat.wordBased(PT));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_formatStandard
    public void test_wordBased_es_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 d\u00EDa, 5 horas, 6 minutos, 7 segundos y 8 milisegundos", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_FormatOneField
    public void test_wordBased_es_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 d\u00EDas", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_formatTwoFields
    public void test_wordBased_es_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 d\u00EDas y 5 horas", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_parseOneField
    public void test_wordBased_es_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(ES).parsePeriod("2 d\u00EDas"));
    }
