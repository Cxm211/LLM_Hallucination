// buggy code
    private static PeriodFormatter toFormatter(List<Object> elementPairs, boolean notPrinter, boolean notParser) {
        if (notPrinter && notParser) {
            throw new IllegalStateException("Builder has created neither a printer nor a parser");
        }
        int size = elementPairs.size();
        if (size >= 2 && elementPairs.get(0) instanceof Separator) {
            Separator sep = (Separator) elementPairs.get(0);
                PeriodFormatter f = toFormatter(elementPairs.subList(2, size), notPrinter, notParser);
                sep = sep.finish(f.getPrinter(), f.getParser());
                return new PeriodFormatter(sep, sep);
        }
        Object[] comp = createComposite(elementPairs);
        if (notPrinter) {
            return new PeriodFormatter(null, (PeriodParser) comp[1]);
        } else if (notParser) {
            return new PeriodFormatter((PeriodPrinter) comp[0], null);
        } else {
            return new PeriodFormatter((PeriodPrinter) comp[0], (PeriodParser) comp[1]);
        }
    }

// relevant test
// org.joda.time.convert.TestStringConverter::testIsReadableInterval_Object_Chronology
    public void testIsReadableInterval_Object_Chronology() throws Exception {
        assertEquals(false, StringConverter.INSTANCE.isReadableInterval("", null));
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology1
    public void testSetIntoInterval_Object_Chronology1() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2004-06-09/P1Y2M", null);
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), m.getStart());
        assertEquals(new DateTime(2005, 8, 9, 0, 0, 0, 0), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology2
    public void testSetIntoInterval_Object_Chronology2() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "P1Y2M/2004-06-09", null);
        assertEquals(new DateTime(2003, 4, 9, 0, 0, 0, 0), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology3
    public void testSetIntoInterval_Object_Chronology3() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09/2004-06-09", null);
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology4
    public void testSetIntoInterval_Object_Chronology4() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2004-06-09T+06:00/P1Y2M", null);
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getStart());
        assertEquals(new DateTime(2005, 8, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology5
    public void testSetIntoInterval_Object_Chronology5() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "P1Y2M/2004-06-09T+06:00", null);
        assertEquals(new DateTime(2003, 4, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology6
    public void testSetIntoInterval_Object_Chronology6() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09T+06:00/2004-06-09T+07:00", null);
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, SEVEN).withChronology(null), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology7
    public void testSetIntoInterval_Object_Chronology7() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09/2004-06-09", BuddhistChronology.getInstance());
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0, BuddhistChronology.getInstance()), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, BuddhistChronology.getInstance()), m.getEnd());
        assertEquals(BuddhistChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology8
    public void testSetIntoInterval_Object_Chronology8() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09T+06:00/2004-06-09T+07:00", BuddhistChronology.getInstance(EIGHT));
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0, BuddhistChronology.getInstance(SIX)).withZone(EIGHT), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, BuddhistChronology.getInstance(SEVEN)).withZone(EIGHT), m.getEnd());
        assertEquals(BuddhistChronology.getInstance(EIGHT), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology1
    public void testSetIntoIntervalEx_Object_Chronology1() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology2
    public void testSetIntoIntervalEx_Object_Chronology2() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "/", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology3
    public void testSetIntoIntervalEx_Object_Chronology3() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "P1Y/", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology4
    public void testSetIntoIntervalEx_Object_Chronology4() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "/P1Y", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology5
    public void testSetIntoIntervalEx_Object_Chronology5() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "P1Y/P2Y", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testToString
    public void testToString() {
        assertEquals("Converter[java.lang.String]", StringConverter.INSTANCE.toString());
    }

// org.joda.time.format.TestISOPeriodFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        ISOPeriodFormat f = new ISOPeriodFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatStandard
    public void testFormatStandard() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
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

// org.joda.time.format.TestPeriodFormat::test_getDefault_cached
    public void test_getDefault_cached() {
        assertSame(PeriodFormat.getDefault(), PeriodFormat.getDefault());
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
        assertEquals("1 dia, 5 horas, 6 minutos, 7 segundos y 8 milisegundos", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_FormatOneField
    public void test_wordBased_es_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 dias", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_formatTwoFields
    public void test_wordBased_es_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 dias y 5 horas", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_parseOneField
    public void test_wordBased_es_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(ES).parsePeriod("2 dias"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_parseTwoFields
    public void test_wordBased_es_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(ES).parsePeriod("2 dias y 5 horas"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_cached
    public void test_wordBased_es_cached() {
        assertSame(PeriodFormat.wordBased(ES), PeriodFormat.wordBased(ES));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_formatStandard
    public void test_wordBased_de_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 Tag, 5 Stunden, 6 Minuten, 7 Sekunden und 8 Millisekunden", PeriodFormat.wordBased(DE).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_FormatOneField
    public void test_wordBased_de_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 Tage", PeriodFormat.wordBased(DE).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_formatTwoFields
    public void test_wordBased_de_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 Tage und 5 Stunden", PeriodFormat.wordBased(DE).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_parseOneField
    public void test_wordBased_de_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(DE).parsePeriod("2 Tage"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_parseTwoFields
    public void test_wordBased_de_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(DE).parsePeriod("2 Tage und 5 Stunden"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_cached
    public void test_wordBased_de_cached() {
        assertSame(PeriodFormat.wordBased(DE), PeriodFormat.wordBased(DE));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_formatStandard
    public void test_wordBased_nl_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 dag, 5 uur, 6 minuten, 7 seconden en 8 milliseconden", PeriodFormat.wordBased(NL).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_FormatOneField
    public void test_wordBased_nl_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 dagen", PeriodFormat.wordBased(NL).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_formatTwoFields
    public void test_wordBased_nl_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 dagen en 5 uur", PeriodFormat.wordBased(NL).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_parseOneField
    public void test_wordBased_nl_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(NL).parsePeriod("2 dagen"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_parseTwoFields
    public void test_wordBased_nl_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(NL).parsePeriod("2 dagen en 5 uur"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_cached
    public void test_wordBased_nl_cached() {
        assertSame(PeriodFormat.wordBased(NL), PeriodFormat.wordBased(NL));
    }

// org.joda.time.format.TestPeriodFormatParsing::testParseStandard1
    public void testParseStandard1() {}

// org.joda.time.format.TestPeriodFormatParsing::testParseCustom1
    public void testParseCustom1() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .printZeroAlways()
            .appendHours()
            .appendSuffix(":")
            .minimumPrintedDigits(2)
            .appendMinutes()
            .toFormatter();

        Period p;

        p = new Period(47, 55, 0, 0);
        assertEquals("47:55", formatter.print(p));
        assertEquals(p, formatter.parsePeriod("47:55"));
        assertEquals(p, formatter.parsePeriod("047:055"));

        p = new Period(7, 5, 0, 0);
        assertEquals("7:05", formatter.print(p));
        assertEquals(p, formatter.parsePeriod("7:05"));
        assertEquals(p, formatter.parsePeriod("7:5"));
        assertEquals(p, formatter.parsePeriod("07:05"));

        p = new Period(0, 5, 0, 0);
        assertEquals("0:05", formatter.print(p));
        assertEquals(p, formatter.parsePeriod("0:05"));
        assertEquals(p, formatter.parsePeriod("0:5"));
        assertEquals(p, formatter.parsePeriod("00:005"));
        assertEquals(p, formatter.parsePeriod("0:005"));

        p = new Period(0, 0, 0, 0);
        assertEquals("0:00", formatter.print(p));
        assertEquals(p, formatter.parsePeriod("0:00"));
        assertEquals(p, formatter.parsePeriod("0:0"));
        assertEquals(p, formatter.parsePeriod("00:00"));
    }

// org.joda.time.format.TestPeriodFormatter::testPrint_simple
    public void testPrint_simple() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", f.print(p));
    }

// org.joda.time.format.TestPeriodFormatter::testPrint_bufferMethods
    public void testPrint_bufferMethods() throws Exception {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, p);
        assertEquals("P1Y2M3W4DT5H6M7.008S", buf.toString());
        
        buf = new StringBuffer();
        try {
            f.printTo(buf, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatter::testPrint_writerMethods
    public void testPrint_writerMethods() throws Exception {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        CharArrayWriter out = new CharArrayWriter();
        f.printTo(out, p);
        assertEquals("P1Y2M3W4DT5H6M7.008S", out.toString());
        
        out = new CharArrayWriter();
        try {
            f.printTo(out, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatter::testWithGetLocaleMethods
    public void testWithGetLocaleMethods() {
        PeriodFormatter f2 = f.withLocale(Locale.FRENCH);
        assertEquals(Locale.FRENCH, f2.getLocale());
        assertSame(f2, f2.withLocale(Locale.FRENCH));
        
        f2 = f.withLocale(null);
        assertEquals(null, f2.getLocale());
        assertSame(f2, f2.withLocale(null));
    }

// org.joda.time.format.TestPeriodFormatter::testWithGetParseTypeMethods
    public void testWithGetParseTypeMethods() {
        PeriodFormatter f2 = f.withParseType(PeriodType.dayTime());
        assertEquals(PeriodType.dayTime(), f2.getParseType());
        assertSame(f2, f2.withParseType(PeriodType.dayTime()));
        
        f2 = f.withParseType(null);
        assertEquals(null, f2.getParseType());
        assertSame(f2, f2.withParseType(null));
    }

// org.joda.time.format.TestPeriodFormatter::testPrinterParserMethods
    public void testPrinterParserMethods() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        PeriodFormatter f2 = new PeriodFormatter(f.getPrinter(), f.getParser());
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(true, f2.isParser());
        assertNotNull(f2.print(p));
        assertNotNull(f2.parsePeriod("P1Y2M3W4DT5H6M7.008S"));
        
        f2 = new PeriodFormatter(f.getPrinter(), null);
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(null, f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(false, f2.isParser());
        assertNotNull(f2.print(p));
        try {
            assertNotNull(f2.parsePeriod("P1Y2M3W4DT5H6M7.008S"));
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        f2 = new PeriodFormatter(null, f.getParser());
        assertEquals(null, f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(false, f2.isPrinter());
        assertEquals(true, f2.isParser());
        try {
            f2.print(p);
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertNotNull(f2.parsePeriod("P1Y2M3W4DT5H6M7.008S"));
    }

// org.joda.time.format.TestPeriodFormatter::testParsePeriod_simple
    public void testParsePeriod_simple() {
        Period expect = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(expect, f.parsePeriod("P1Y2M3W4DT5H6M7.008S"));
        
        try {
            f.parsePeriod("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatter::testParsePeriod_parseType
    public void testParsePeriod_parseType() {
        Period expect = new Period(0, 0, 0, 4, 5, 6, 7, 8, PeriodType.dayTime());
        assertEquals(expect, f.withParseType(PeriodType.dayTime()).parsePeriod("P4DT5H6M7.008S"));
        try {
            f.withParseType(PeriodType.dayTime()).parsePeriod("P3W4DT5H6M7.008S");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatter::testParseMutablePeriod_simple
    public void testParseMutablePeriod_simple() {
        MutablePeriod expect = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(expect, f.parseMutablePeriod("P1Y2M3W4DT5H6M7.008S"));
        
        try {
            f.parseMutablePeriod("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatter::testParseInto_simple
    public void testParseInto_simple() {
        MutablePeriod expect = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        MutablePeriod result = new MutablePeriod();
        assertEquals(20, f.parseInto(result, "P1Y2M3W4DT5H6M7.008S", 0));
        assertEquals(expect, result);
        
        try {
            f.parseInto(null, "P1Y2M3W4DT5H6M7.008S", 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(~0, f.parseInto(result, "ABC", 0));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testToFormatterPrinterParser
    public void testToFormatterPrinterParser() {
        builder.appendYears();
        assertNotNull(builder.toFormatter());
        assertNotNull(builder.toPrinter());
        assertNotNull(builder.toParser());
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatYears
    public void testFormatYears() {
        PeriodFormatter f = builder.appendYears().toFormatter();
        assertEquals("1", f.print(PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatMonths
    public void testFormatMonths() {
        PeriodFormatter f = builder.appendMonths().toFormatter();
        assertEquals("2", f.print(PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatWeeks
    public void testFormatWeeks() {
        PeriodFormatter f = builder.appendWeeks().toFormatter();
        assertEquals("3", f.print(PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatDays
    public void testFormatDays() {
        PeriodFormatter f = builder.appendDays().toFormatter();
        assertEquals("4", f.print(PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatHours
    public void testFormatHours() {
        PeriodFormatter f = builder.appendHours().toFormatter();
        assertEquals("5", f.print(PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatMinutes
    public void testFormatMinutes() {
        PeriodFormatter f = builder.appendMinutes().toFormatter();
        assertEquals("6", f.print(PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSeconds
    public void testFormatSeconds() {
        PeriodFormatter f = builder.appendSeconds().toFormatter();
        assertEquals("7", f.print(PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSecondsWithMillis
    public void testFormatSecondsWithMillis() {
        PeriodFormatter f = builder.appendSecondsWithMillis().toFormatter();
        Period p = new Period(0, 0, 0, 0, 0, 0, 7, 0);
        assertEquals("7.000", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, 1);
        assertEquals("7.001", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, 999);
        assertEquals("7.999", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, 1000);
        assertEquals("8.000", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, 1001);
        assertEquals("8.001", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, -1);
        assertEquals("6.999", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, -7, 1);
        assertEquals("-6.999", f.print(p));
        assertEquals(6, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, -7, -1);
        assertEquals("-7.001", f.print(p));
        assertEquals(6, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0.000", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSecondsWithOptionalMillis
    public void testFormatSecondsWithOptionalMillis() {
        PeriodFormatter f = builder.appendSecondsWithOptionalMillis().toFormatter();
        Period p = new Period(0, 0, 0, 0, 0, 0, 7, 0);
        assertEquals("7", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, 1);
        assertEquals("7.001", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, 999);
        assertEquals("7.999", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, 1000);
        assertEquals("8", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, 1001);
        assertEquals("8.001", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 7, -1);
        assertEquals("6.999", f.print(p));
        assertEquals(5, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, -7, 1);
        assertEquals("-6.999", f.print(p));
        assertEquals(6, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, -7, -1);
        assertEquals("-7.001", f.print(p));
        assertEquals(6, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        
        p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatMillis
    public void testFormatMillis() {
        PeriodFormatter f = builder.appendMillis().toFormatter();
        assertEquals("8", f.print(PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", f.print(p));
        assertEquals(1, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatMillis3Digit
    public void testFormatMillis3Digit() {
        PeriodFormatter f = builder.appendMillis3Digit().toFormatter();
        assertEquals("008", f.print(PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("000", f.print(p));
        assertEquals(3, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrefixSimple1
    public void testFormatPrefixSimple1() {
        PeriodFormatter f = builder.appendPrefix("Years:").appendYears().toFormatter();
        assertEquals("Years:1", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("Years:0", f.print(p));
        assertEquals(7, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrefixSimple2
    public void testFormatPrefixSimple2() {
        PeriodFormatter f = builder.appendPrefix("Hours:").appendHours().toFormatter();
        assertEquals("Hours:5", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("Hours:0", f.print(p));
        assertEquals(7, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrefixSimple3
    public void testFormatPrefixSimple3() {
        try {
            builder.appendPrefix(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrefixPlural1
    public void testFormatPrefixPlural1() {
        PeriodFormatter f = builder.appendPrefix("Year:", "Years:").appendYears().toFormatter();
        assertEquals("Year:1", f.print(PERIOD));
        assertEquals(6, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("Years:0", f.print(p));
        assertEquals(7, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrefixPlural2
    public void testFormatPrefixPlural2() {
        PeriodFormatter f = builder.appendPrefix("Hour:", "Hours:").appendHours().toFormatter();
        assertEquals("Hours:5", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("Hours:0", f.print(p));
        assertEquals(7, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrefixPlural3
    public void testFormatPrefixPlural3() {
        try {
            builder.appendPrefix(null, "");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            builder.appendPrefix("", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            builder.appendPrefix(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSuffixSimple1
    public void testFormatSuffixSimple1() {
        PeriodFormatter f = builder.appendYears().appendSuffix(" years").toFormatter();
        assertEquals("1 years", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0 years", f.print(p));
        assertEquals(7, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSuffixSimple2
    public void testFormatSuffixSimple2() {
        PeriodFormatter f = builder.appendHours().appendSuffix(" hours").toFormatter();
        assertEquals("5 hours", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0 hours", f.print(p));
        assertEquals(7, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSuffixSimple3
    public void testFormatSuffixSimple3() {
        try {
            builder.appendSuffix(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSuffixSimple4
    public void testFormatSuffixSimple4() {
        try {
            builder.appendSuffix(" hours");
            fail();
        } catch (IllegalStateException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSuffixPlural1
    public void testFormatSuffixPlural1() {
        PeriodFormatter f = builder.appendYears().appendSuffix(" year", " years").toFormatter();
        assertEquals("1 year", f.print(PERIOD));
        assertEquals(6, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0 years", f.print(p));
        assertEquals(7, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSuffixPlural2
    public void testFormatSuffixPlural2() {
        PeriodFormatter f = builder.appendHours().appendSuffix(" hour", " hours").toFormatter();
        assertEquals("5 hours", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0 hours", f.print(p));
        assertEquals(7, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSuffixPlural3
    public void testFormatSuffixPlural3() {
        try {
            builder.appendSuffix(null, "");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            builder.appendSuffix("", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            builder.appendSuffix(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSuffixPlural4
    public void testFormatSuffixPlural4() {
        try {
            builder.appendSuffix(" hour", " hours");
            fail();
        } catch (IllegalStateException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrefixSuffix
    public void testFormatPrefixSuffix() {
        PeriodFormatter f = builder.appendPrefix("P").appendYears().appendSuffix("Y").toFormatter();
        assertEquals("P1Y", f.print(PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("P0Y", f.print(p));
        assertEquals(3, f.getPrinter().calculatePrintedLength(p, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSeparatorSimple
    public void testFormatSeparatorSimple() {
        PeriodFormatter f = builder.appendYears().appendSeparator("T").appendHours().toFormatter();
        assertEquals("1T5", f.print(PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("5", f.print(TIME_PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(TIME_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(TIME_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1", f.print(DATE_PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(DATE_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(DATE_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSeparatorComplex
    public void testFormatSeparatorComplex() {
        PeriodFormatter f = builder
            .appendYears().appendSeparator(", ", " and ")
            .appendHours().appendSeparator(", ", " and ")
            .appendMinutes().appendSeparator(", ", " and ")
            .toFormatter();
        assertEquals("1, 5 and 6", f.print(PERIOD));
        assertEquals(10, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(3, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("5 and 6", f.print(TIME_PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(TIME_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(TIME_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1", f.print(DATE_PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(DATE_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(DATE_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSeparatorIfFieldsAfter
    public void testFormatSeparatorIfFieldsAfter() {
        PeriodFormatter f = builder.appendYears().appendSeparatorIfFieldsAfter("T").appendHours().toFormatter();
        assertEquals("1T5", f.print(PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("T5", f.print(TIME_PERIOD));
        assertEquals(2, f.getPrinter().calculatePrintedLength(TIME_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(TIME_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1", f.print(DATE_PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(DATE_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(DATE_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatSeparatorIfFieldsBefore
    public void testFormatSeparatorIfFieldsBefore() {
        PeriodFormatter f = builder.appendYears().appendSeparatorIfFieldsBefore("T").appendHours().toFormatter();
        assertEquals("1T5", f.print(PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("5", f.print(TIME_PERIOD));
        assertEquals(1, f.getPrinter().calculatePrintedLength(TIME_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(TIME_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1T", f.print(DATE_PERIOD));
        assertEquals(2, f.getPrinter().calculatePrintedLength(DATE_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(DATE_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatLiteral
    public void testFormatLiteral() {
        PeriodFormatter f = builder.appendLiteral("HELLO").toFormatter();
        assertEquals("HELLO", f.print(PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(0, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppendFormatter
    public void testFormatAppendFormatter() {
        PeriodFormatter base = builder.appendYears().appendLiteral("-").toFormatter();
        PeriodFormatter f = new PeriodFormatterBuilder().append(base).appendYears().toFormatter();
        assertEquals("1-1", f.print(PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatMinDigits
    public void testFormatMinDigits() {
        PeriodFormatter f = new PeriodFormatterBuilder().minimumPrintedDigits(4).appendYears().toFormatter();
        assertEquals("0001", f.print(PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroDefault
    public void testFormatPrintZeroDefault() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---0", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---0", f.print(EMPTY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
        
        
        f = new PeriodFormatterBuilder()
                .appendYears().appendLiteral("-")
                .appendYears().toFormatter();
        assertEquals("-0", f.print(EMPTY_PERIOD));
        assertEquals(2, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyLast
    public void testFormatPrintZeroRarelyLast() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroRarelyLast()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---0", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---0", f.print(EMPTY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirst
    public void testFormatPrintZeroRarelyFirst() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroRarelyFirst()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0---", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0---", f.print(EMPTY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstYears
    public void testFormatPrintZeroRarelyFirstYears() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendYears().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstMonths
    public void testFormatPrintZeroRarelyFirstMonths() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendMonths().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstWeeks
    public void testFormatPrintZeroRarelyFirstWeeks() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendWeeks().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstDays
    public void testFormatPrintZeroRarelyFirstDays() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendDays().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstHours
    public void testFormatPrintZeroRarelyFirstHours() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendHours().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstMinutes
    public void testFormatPrintZeroRarelyFirstMinutes() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendMinutes().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstSeconds
    public void testFormatPrintZeroRarelyFirstSeconds() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendSeconds().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroIfSupported
    public void testFormatPrintZeroIfSupported() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroIfSupported()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0---0", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0-0-0-0", f.print(EMPTY_PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroAlways
    public void testFormatPrintZeroAlways() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroAlways()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0-0-0-0", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1-0-0-4", f.print(YEAR_DAY_PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0-0-0-0", f.print(EMPTY_PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroNever
    public void testFormatPrintZeroNever() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroNever()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(0, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---", f.print(EMPTY_PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(0, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_null_null
    public void testFormatAppend_PrinterParser_null_null() {
        try {
            new PeriodFormatterBuilder().append(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_Printer_null
    public void testFormatAppend_PrinterParser_Printer_null() {
        PeriodPrinter printer = new PeriodFormatterBuilder().appendYears().appendLiteral("-").toPrinter();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(printer, null).appendMonths();
        assertNotNull(bld.toPrinter());
        assertNull(bld.toParser());
        
        PeriodFormatter f = bld.toFormatter();
        assertEquals("1-2", f.print(PERIOD));
        try {
            f.parsePeriod("1-2");
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_null_Parser
    public void testFormatAppend_PrinterParser_null_Parser() {
        PeriodParser parser = new PeriodFormatterBuilder().appendWeeks().appendLiteral("-").toParser();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(null, parser).appendMonths();
        assertNull(bld.toPrinter());
        assertNotNull(bld.toParser());
        
        PeriodFormatter f = bld.toFormatter();
        try {
            f.print(PERIOD);
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertEquals(new Period(0, 2, 1, 0, 0, 0, 0, 0), f.parsePeriod("1-2"));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_PrinterParser
    public void testFormatAppend_PrinterParser_PrinterParser() {
        PeriodPrinter printer = new PeriodFormatterBuilder().appendYears().appendLiteral("-").toPrinter();
        PeriodParser parser = new PeriodFormatterBuilder().appendWeeks().appendLiteral("-").toParser();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(printer, parser).appendMonths();
        assertNotNull(bld.toPrinter());
        assertNotNull(bld.toParser());
        
        PeriodFormatter f = bld.toFormatter();
        assertEquals("1-2", f.print(PERIOD));
        assertEquals(new Period(0, 2, 1, 0, 0, 0, 0, 0), f.parsePeriod("1-2"));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_Printer_null_null_Parser
    public void testFormatAppend_PrinterParser_Printer_null_null_Parser() {
        PeriodPrinter printer = new PeriodFormatterBuilder().appendYears().appendLiteral("-").toPrinter();
        PeriodParser parser = new PeriodFormatterBuilder().appendWeeks().appendLiteral("-").toParser();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(printer, null).append(null, parser);
        assertNull(bld.toPrinter());
        assertNull(bld.toParser());
        
        try {
            bld.toFormatter();
            fail();
        } catch (IllegalStateException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParserThenClear
    public void testFormatAppend_PrinterParserThenClear() {
        PeriodPrinter printer = new PeriodFormatterBuilder().appendYears().appendLiteral("-").toPrinter();
        PeriodParser parser = new PeriodFormatterBuilder().appendWeeks().appendLiteral("-").toParser();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(printer, null).append(null, parser);
        assertNull(bld.toPrinter());
        assertNull(bld.toParser());
        bld.clear();
        bld.appendMonths();
        assertNotNull(bld.toPrinter());
        assertNotNull(bld.toParser());
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testBug2495455
    public void testBug2495455() {
        PeriodFormatter pfmt1 = new PeriodFormatterBuilder()
            .appendLiteral("P")
            .appendYears()
            .appendSuffix("Y")
            .appendMonths()
            .appendSuffix("M")
            .appendWeeks()
            .appendSuffix("W")
            .appendDays()
            .appendSuffix("D")
            .appendSeparatorIfFieldsAfter("T")
            .appendHours()
            .appendSuffix("H")
            .appendMinutes()
            .appendSuffix("M")
            .appendSecondsWithOptionalMillis()
            .appendSuffix("S")
            .toFormatter();
        PeriodFormatter pfmt2 = new PeriodFormatterBuilder()
            .append(ISOPeriodFormat.standard())
            .toFormatter();
        pfmt1.parsePeriod("PT1003199059S");
        pfmt2.parsePeriod("PT1003199059S");
    }
