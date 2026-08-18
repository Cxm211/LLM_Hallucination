// buggy code
    private StringBuffer appendQuotedString(String pattern, ParsePosition pos,
            StringBuffer appendTo, boolean escapingOn) {
        int start = pos.getIndex();
        char[] c = pattern.toCharArray();
        if (escapingOn && c[start] == QUOTE) {
            return appendTo == null ? null : appendTo.append(QUOTE);
        }
        int lastHold = start;
        for (int i = pos.getIndex(); i < pattern.length(); i++) {
            if (escapingOn && pattern.substring(i).startsWith(ESCAPED_QUOTE)) {
                appendTo.append(c, lastHold, pos.getIndex() - lastHold).append(
                        QUOTE);
                pos.setIndex(i + ESCAPED_QUOTE.length());
                lastHold = pos.getIndex();
                continue;
            }
            switch (c[pos.getIndex()]) {
            case QUOTE:
                next(pos);
                return appendTo == null ? null : appendTo.append(c, lastHold,
                        pos.getIndex() - lastHold);
            default:
                next(pos);
            }
        }
        throw new IllegalArgumentException(
                "Unterminated quoted string at position " + start);
    }

// relevant test
// org.apache.commons.lang.text.ExtendedMessageFormatTest::testExtendedFormats
    public void testExtendedFormats() {
        String pattern = "Lower: {0,lower} Upper: {1,upper}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertPatternsEqual("TOPATTERN", pattern, emf.toPattern());
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"foo", "bar"}));
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"Foo", "Bar"}));
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"FOO", "BAR"}));
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"FOO", "bar"}));
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"foo", "BAR"}));
    }

// org.apache.commons.lang.text.ExtendedMessageFormatTest::testEscapedQuote_LANG_477
    public void testEscapedQuote_LANG_477() {
        String pattern = "it''s a {0,lower} 'test'!";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("it's a dummy test!", emf.format(new Object[] {"DUMMY"}));
    }

// org.apache.commons.lang.text.ExtendedMessageFormatTest::testExtendedAndBuiltInFormats
    public void testExtendedAndBuiltInFormats() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 23, 18, 33, 05);
        Object[] args = new Object[] {"John Doe", cal.getTime(), new Double("12345.67")};
        String builtinsPattern = "DOB: {1,date,short} Salary: {2,number,currency}";
        String extendedPattern = "Name: {0,upper} ";
        String pattern = extendedPattern + builtinsPattern;

        HashSet testLocales = new HashSet();
        testLocales.addAll(Arrays.asList(DateFormat.getAvailableLocales()));
        testLocales.retainAll(Arrays.asList(NumberFormat.getAvailableLocales()));
        testLocales.add(null);

        for (Iterator l = testLocales.iterator(); l.hasNext();) {
            Locale locale = (Locale) l.next();
            MessageFormat builtins = createMessageFormat(builtinsPattern, locale);
            String expectedPattern = extendedPattern + builtins.toPattern();;
            DateFormat df = null;
            NumberFormat nf = null;
            ExtendedMessageFormat emf = null;
            if (locale == null) {
                df = DateFormat.getDateInstance(DateFormat.SHORT);
                nf = NumberFormat.getCurrencyInstance();
                emf = new ExtendedMessageFormat(pattern, registry);
            } else {
                df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                nf = NumberFormat.getCurrencyInstance(locale);
                emf = new ExtendedMessageFormat(pattern, locale, registry);
            }
            StringBuffer expected = new StringBuffer();
            expected.append("Name: ");
            expected.append(args[0].toString().toUpperCase());
            expected.append(" DOB: ");
            expected.append(df.format(args[1]));
            expected.append(" Salary: ");
            expected.append(nf.format(args[2]));
            assertPatternsEqual("pattern comparison for locale " + locale, expectedPattern, emf.toPattern());
            assertEquals(String.valueOf(locale), expected.toString(), emf.format(args));
        }
    }

// org.apache.commons.lang.text.ExtendedMessageFormatTest::testBuiltInChoiceFormat
    public void testBuiltInChoiceFormat() {
        Object[] values = new Number[] {new Integer(1), new Double("2.2"), new Double("1234.5")};
        String choicePattern = null;
        Locale[] availableLocales = ChoiceFormat.getAvailableLocales();

        choicePattern = "{0,choice,1#One|2#Two|3#Many {0,number}}";
        for (int i = 0; i < values.length; i++) {
            checkBuiltInFormat(values[i] + ": " + choicePattern, new Object[] {values[i]}, availableLocales);
        }

        choicePattern = "{0,choice,1#''One''|2#\"Two\"|3#''{Many}'' {0,number}}";
        for (int i = 0; i < values.length; i++) {
            checkBuiltInFormat(values[i] + ": " + choicePattern, new Object[] {values[i]}, availableLocales);
        }
    }

// org.apache.commons.lang.text.ExtendedMessageFormatTest::testBuiltInDateTimeFormat
    public void testBuiltInDateTimeFormat() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 23, 18, 33, 05);
        Object[] args = new Object[] {cal.getTime()};
        Locale[] availableLocales = DateFormat.getAvailableLocales();

        checkBuiltInFormat("1: {0,date,short}",    args, availableLocales);
        checkBuiltInFormat("2: {0,date,medium}",   args, availableLocales);
        checkBuiltInFormat("3: {0,date,long}",     args, availableLocales);
        checkBuiltInFormat("4: {0,date,full}",     args, availableLocales);
        checkBuiltInFormat("5: {0,date,d MMM yy}", args, availableLocales);
        checkBuiltInFormat("6: {0,time,short}",    args, availableLocales);
        checkBuiltInFormat("7: {0,time,medium}",   args, availableLocales);
        checkBuiltInFormat("8: {0,time,long}",     args, availableLocales);
        checkBuiltInFormat("9: {0,time,full}",     args, availableLocales);
        checkBuiltInFormat("10: {0,time,HH:mm}",   args, availableLocales);
        checkBuiltInFormat("11: {0,date}",         args, availableLocales);
        checkBuiltInFormat("12: {0,time}",         args, availableLocales);
    }

// org.apache.commons.lang.text.ExtendedMessageFormatTest::testOverriddenBuiltinFormat
    public void testOverriddenBuiltinFormat() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 23);
        Object[] args = new Object[] {cal.getTime()};
        Locale[] availableLocales = DateFormat.getAvailableLocales();
        Map registry = Collections.singletonMap("date", new OverrideShortDateFormatFactory());

        
        checkBuiltInFormat("1: {0,date}", registry,          args, availableLocales);
        checkBuiltInFormat("2: {0,date,medium}", registry,   args, availableLocales);
        checkBuiltInFormat("3: {0,date,long}", registry,     args, availableLocales);
        checkBuiltInFormat("4: {0,date,full}", registry,     args, availableLocales);
        checkBuiltInFormat("5: {0,date,d MMM yy}", registry, args, availableLocales);

        
        for (int i = -1; i < availableLocales.length; i++) {
            Locale locale = i < 0 ? null : availableLocales[i];
            MessageFormat dateDefault = createMessageFormat("{0,date}", locale);
            String pattern = "{0,date,short}";
            ExtendedMessageFormat dateShort = new ExtendedMessageFormat(pattern, locale, registry);
            assertEquals("overridden date,short format", dateDefault.format(args), dateShort.format(args));
            assertEquals("overridden date,short pattern", pattern, dateShort.toPattern());
        }
    }

// org.apache.commons.lang.text.ExtendedMessageFormatTest::testBuiltInNumberFormat
    public void testBuiltInNumberFormat() {
        Object[] args = new Object[] {new Double("6543.21")};
        Locale[] availableLocales = NumberFormat.getAvailableLocales();
        checkBuiltInFormat("1: {0,number}",            args, availableLocales);
        checkBuiltInFormat("2: {0,number,integer}",    args, availableLocales);
        checkBuiltInFormat("3: {0,number,currency}",   args, availableLocales);
        checkBuiltInFormat("4: {0,number,percent}",    args, availableLocales);
        checkBuiltInFormat("5: {0,number,00000.000}",  args, availableLocales);
    }
