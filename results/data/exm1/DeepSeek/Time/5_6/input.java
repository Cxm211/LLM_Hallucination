// buggy code
    public Period normalizedStandard(PeriodType type) {
        type = DateTimeUtils.getPeriodType(type);
        long millis = getMillis();  // no overflow can happen, even with Integer.MAX_VALUEs
        millis += (((long) getSeconds()) * ((long) DateTimeConstants.MILLIS_PER_SECOND));
        millis += (((long) getMinutes()) * ((long) DateTimeConstants.MILLIS_PER_MINUTE));
        millis += (((long) getHours()) * ((long) DateTimeConstants.MILLIS_PER_HOUR));
        millis += (((long) getDays()) * ((long) DateTimeConstants.MILLIS_PER_DAY));
        millis += (((long) getWeeks()) * ((long) DateTimeConstants.MILLIS_PER_WEEK));
        Period result = new Period(millis, type, ISOChronology.getInstanceUTC());
        int years = getYears();
        int months = getMonths();
        if (years != 0 || months != 0) {
            years = FieldUtils.safeAdd(years, months / 12);
            months = months % 12;
            if (years != 0) {
                result = result.withYears(years);
            }
            if (months != 0) {
                result = result.withMonths(months);
            }
        }
        return result;
    }

// relevant test
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

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_parseTwoFields
    public void test_wordBased_es_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(ES).parsePeriod("2 d\u00EDas y 5 horas"));
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

// org.joda.time.format.TestPeriodFormat::test_wordBased_da_formatMultiple
    public void test_wordBased_da_formatMultiple() {
        Period p = new Period(2, 3, 4, 2, 5, 6 ,7, 8);
        assertEquals("2 \u00E5r, 3 m\u00E5neder, 4 uger, 2 dage, 5 timer, 6 minutter, 7 sekunder og 8 millisekunder", PeriodFormat.wordBased(DA).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_da_formatSinglular
    public void test_wordBased_da_formatSinglular() {
        Period p = new Period(1, 1, 1, 1, 1, 1, 1, 1);
        assertEquals("1 \u00E5r, 1 m\u00E5ned, 1 uge, 1 dag, 1 time, 1 minut, 1 sekund og 1 millisekund", PeriodFormat.wordBased(DA).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_da_cached
    public void test_wordBased_da_cached() {
        assertSame(PeriodFormat.wordBased(DA), PeriodFormat.wordBased(DA));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_ja_formatMultiple
    public void test_wordBased_ja_formatMultiple() {
        Period p = new Period(2, 3, 4, 2, 5, 6 ,7, 8);
        assertEquals("2\u5E743\u304B\u67084\u9031\u95932\u65E55\u6642\u95936\u52067\u79D28\u30DF\u30EA\u79D2", PeriodFormat.wordBased(JA).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_ja_formatSingular
    public void test_wordBased_ja_formatSingular() {
        Period p = new Period(1, 1, 1, 1, 1, 1, 1, 1);
        assertEquals("1\u5E741\u304B\u67081\u9031\u95931\u65E51\u6642\u95931\u52061\u79D21\u30DF\u30EA\u79D2", PeriodFormat.wordBased(JA).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_ja_cached
    public void test_wordBased_ja_cached() {
        assertSame(PeriodFormat.wordBased(JA), PeriodFormat.wordBased(JA));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_ja_parseOneField
    public void test_wordBased_ja_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(JA).parsePeriod("2\u65E5"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_ja_parseTwoFields
    public void test_wordBased_ja_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(JA).parsePeriod("2\u65E55\u6642\u9593"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_ja_checkRedundantSeparator
    public void test_wordBased_ja_checkRedundantSeparator() {
        try {
            
            PeriodFormat.wordBased(JA).parsePeriod("2\u65E5 ");
            fail("No exception was caught");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_from_de
    public void test_wordBased_fr_from_de() {
      Locale.setDefault(DE);
      Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
      assertEquals("1 jour, 5 heures, 6 minutes, 7 secondes et 8 millisecondes", PeriodFormat.wordBased(FR).print(p));
  }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_from_nl
    public void test_wordBased_fr_from_nl() {
      Locale.setDefault(NL);
      Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
      assertEquals("1 jour, 5 heures, 6 minutes, 7 secondes et 8 millisecondes", PeriodFormat.wordBased(FR).print(p));
  }

// org.joda.time.format.TestPeriodFormat::test_wordBased_en_from_de
    public void test_wordBased_en_from_de() {
      Locale.setDefault(DE);
      Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
      assertEquals("1 day, 5 hours, 6 minutes, 7 seconds and 8 milliseconds", PeriodFormat.wordBased(EN).print(p));
  }

// org.joda.time.format.TestPeriodFormat::test_wordBased_en_from_nl
    public void test_wordBased_en_from_nl() {
      Locale.setDefault(NL);
      Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
      assertEquals("1 day, 5 hours, 6 minutes, 7 seconds and 8 milliseconds", PeriodFormat.wordBased(EN).print(p));
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

// org.joda.time.tz.TestCompiler::testDateTimeZoneBuilder
    public void testDateTimeZoneBuilder() throws Exception {
        
        getTestDataTimeZoneBuilder().toDateTimeZone("TestDTZ1", true);
        final DateTimeZone[] zone = new DateTimeZone[1];
        Thread t = new Thread(new Runnable() {
            public void run() {
                zone[0] = getTestDataTimeZoneBuilder().toDateTimeZone("TestDTZ2", true);
            }
        });
        t.start();
        t.join();
        assertNotNull(zone[0]);
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
