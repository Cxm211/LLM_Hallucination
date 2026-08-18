// buggy code
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException
    {
        // May need to resolve types for delegate-based creators:
        JsonDeserializer<Object> delegate = null;
        if (_valueInstantiator != null) {
            // [databind#2324]: check both array-delegating and delegating
            AnnotatedWithParams delegateCreator = _valueInstantiator.getDelegateCreator();
            if (delegateCreator != null) {
                JavaType delegateType = _valueInstantiator.getDelegateType(ctxt.getConfig());
                delegate = findDeserializer(ctxt, delegateType, property);
            }
        }
        JsonDeserializer<?> valueDeser = _valueDeserializer;
        final JavaType valueType = _containerType.getContentType();
        if (valueDeser == null) {
            // [databind#125]: May have a content converter
            valueDeser = findConvertingContentDeserializer(ctxt, property, valueDeser);
            if (valueDeser == null) {
            // And we may also need to get deserializer for String
                valueDeser = ctxt.findContextualValueDeserializer(valueType, property);
            }
        } else { // if directly assigned, probably not yet contextual, so:
            valueDeser = ctxt.handleSecondaryContextualization(valueDeser, property, valueType);
        }
        // 11-Dec-2015, tatu: Should we pass basic `Collection.class`, or more refined? Mostly
        //   comes down to "List vs Collection" I suppose... for now, pass Collection
        Boolean unwrapSingle = findFormatFeature(ctxt, property, Collection.class,
                JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        NullValueProvider nuller = findContentNullProvider(ctxt, property, valueDeser);
        if (isDefaultDeserializer(valueDeser)) {
            valueDeser = null;
        }
        return withResolved(delegate, valueDeser, nuller, unwrapSingle);
    }

// relevant test
// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601FractionalTimezoneOffset
    public void testISO8601FractionalTimezoneOffset() throws Exception
    {
        String inputStr = "1997-07-16T19:20:30.45+01:30";
        java.util.Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 2, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(50, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601FractSecondsLong
    public void testISO8601FractSecondsLong() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        inputStr = "2014-10-03T18:00:00.3456-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        
        assertEquals(345, c.get(Calendar.MILLISECOND));

        
        try {
            MAPPER.readValue(quote("2014-10-03T18:00:00.1234567890-05:00"), java.util.Date.class);
        } catch (InvalidFormatException e) {
            verifyException(e, "invalid fractional seconds");
            verifyException(e, "can use at most 9 digits");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601MissingSeconds
    public void testISO8601MissingSeconds() throws Exception
    {
        String inputStr;
        Date inputDate;

        
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        inputStr = "1997-07-16T19:20+01:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20+0200";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 2, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20+04";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 4, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601NoTimezone
    public void testDateUtilISO8601NoTimezone() throws Exception
    {
        
        String inputStr = "1984-11-13T00:00:09";
        Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1984, c.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(9, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601NoTimezoneNonDefault
    public void testDateUtilISO8601NoTimezoneNonDefault() throws Exception
    {
        
        ObjectReader r = MAPPER.readerFor(Date.class);
        TimeZone tz = TimeZone.getTimeZone("GMT-2");
        Date date1 = r.with(tz)
                .readValue(quote("1970-01-01T00:00:00.000"));
        
        Date date2 = r.with(TimeZone.getTimeZone("GMT+5"))
                .readValue(quote("1970-01-01T00:00:00.000-02:00"));
        assertEquals(date1, date2);

        
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(date1);
        assertEquals(1970, c.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(2, c.get(Calendar.HOUR_OF_DAY));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testFormatAndCtors1722
    public void testFormatAndCtors1722() throws Exception
    {
        Date1722 input = new Date1722(new Date(0L), "bogus");
        String json = MAPPER.writeValueAsString(input);
        Date1722 result = MAPPER.readValue(json, Date1722.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601NoMilliseconds
    public void testDateUtilISO8601NoMilliseconds() throws Exception
    {
        final String INPUT_STR = "2013-10-31T17:27:00";
        Date inputDate;
        Calendar c;
        
        inputDate = MAPPER.readValue(quote(INPUT_STR), java.util.Date.class);
        c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(2013, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(31, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(17, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(27, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        
        
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601JustDate
    public void testDateUtilISO8601JustDate() throws Exception
    {
        
        String inputStr = "1972-12-28";
        Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateSql
    public void testDateSql() throws Exception
    {
        java.sql.Date value = new java.sql.Date(0L);
        value.setYear(99); 
        value.setDate(19);
        value.setMonth(Calendar.APRIL);
        long now = value.getTime();

        
        assertEquals(value, MAPPER.readValue(String.valueOf(now), java.sql.Date.class));

        
        
        java.sql.Date result = MAPPER.readValue(quote(value.toString()), java.sql.Date.class);
        Calendar c = gmtCalendar(result.getTime());
        assertEquals(1999, c.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, c.get(Calendar.MONTH));
        assertEquals(19, c.get(Calendar.DAY_OF_MONTH));

        
        String expStr = "1981-07-13";
        result = MAPPER.readValue(quote(expStr), java.sql.Date.class);
        c.setTimeInMillis(result.getTime());
        assertEquals(1981, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));

        

    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCalendar
    public void testCalendar() throws Exception
    {
        
        java.util.Calendar value = Calendar.getInstance();
        long l = 12345678L;
        value.setTimeInMillis(l);

        
        Calendar result = MAPPER.readValue(""+l, Calendar.class);
        assertEquals(l, result.getTimeInMillis());

        
        String dateStr = dateToString(new Date(l));
        result = MAPPER.readValue(quote(dateStr), Calendar.class);

        
        if (l != result.getTimeInMillis()) {
            fail(String.format("Expected timestamp %d, got %d, for '%s'",
                    l, result.getTimeInMillis(), dateStr));
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustom
    public void testCustom() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("PST"));
        mapper.setDateFormat(df);

        String dateStr = "1972-12-28X15:45:00";
        java.util.Date exp = df.parse(dateStr);
        java.util.Date result = mapper.readValue("\""+dateStr+"\"", java.util.Date.class);
        assertEquals(exp, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDatesWithEmptyStrings
    public void testDatesWithEmptyStrings() throws Exception
    {
        assertNull(MAPPER.readValue(quote(""), java.util.Date.class));
        assertNull(MAPPER.readValue(quote(""), java.util.Calendar.class));
        assertNull(MAPPER.readValue(quote(""), java.sql.Date.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::test8601DateTimeNoMilliSecs
    public void test8601DateTimeNoMilliSecs() throws Exception
    {
        
        for (String inputStr : new String[] {
               "2010-06-28T23:34:22Z",
               "2010-06-28T23:34:22+0000",
               "2010-06-28T23:34:22+00:00",
               "2010-06-28T23:34:22+00",
        }) {
            Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.setTime(inputDate);
            assertEquals(2010, c.get(Calendar.YEAR));
            assertEquals(Calendar.JUNE, c.get(Calendar.MONTH));
            assertEquals(28, c.get(Calendar.DAY_OF_MONTH));
            assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
            assertEquals(34, c.get(Calendar.MINUTE));
            assertEquals(22, c.get(Calendar.SECOND));
            assertEquals(0, c.get(Calendar.MILLISECOND));
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testTimeZone
    public void testTimeZone() throws Exception
    {
        TimeZone result = MAPPER.readValue(quote("PST"), TimeZone.class);
        assertEquals("PST", result.getID());
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustomDateWithAnnotation
    public void testCustomDateWithAnnotation() throws Exception
    {
        final String INPUT = "{\"date\":\"/2005/05/25/\"}";
        DateAsStringBean result = MAPPER.readValue(INPUT, DateAsStringBean.class);
        assertNotNull(result);
        assertNotNull(result.date);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long l = result.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));

        
        
        result = MAPPER.readerFor(DateAsStringBean.class)
                .with(Locale.GERMANY)
                .readValue(INPUT);
        assertNotNull(result);
        assertNotNull(result.date);
        l = result.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));

        
        DateAsStringBeanGermany result2 = MAPPER.readerFor(DateAsStringBeanGermany.class).readValue(INPUT);
        assertNotNull(result2);
        assertNotNull(result2.date);
        l = result2.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustomCalendarWithAnnotation
    public void testCustomCalendarWithAnnotation() throws Exception
    {
        CalendarAsStringBean cbean = MAPPER.readValue("{\"cal\":\";2007/07/13;\"}",
                CalendarAsStringBean.class);
        assertNotNull(cbean);
        assertNotNull(cbean.cal);
        Calendar c = cbean.cal;
        assertEquals(2007, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustomCalendarWithTimeZone
    public void testCustomCalendarWithTimeZone() throws Exception
    {
        
        DateInCETBean cet = MAPPER.readValue("{\"date\":\"2001-01-01,10\"}",
                DateInCETBean.class);
        Calendar c = Calendar.getInstance(getUTCTimeZone());
        c.setTimeInMillis(cet.date.getTime());
        
        assertEquals(2001, c.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, c.get(Calendar.HOUR_OF_DAY));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateEndingWithZNonDefTZ1651
    public void testDateEndingWithZNonDefTZ1651() throws Exception
    {
        String json = quote("1970-01-01T00:00:00.000Z");

        
        
        ObjectMapper mapper = newObjectMapper();
        Date dateUTC = mapper.readValue(json, Date.class);  
    
        
        
        mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("GMT-2"));
        Date dateGMT1 = mapper.readValue(json, Date.class);  
    
        
        assertEquals(dateUTC.getTime(), dateGMT1.getTime());
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testContextTimezone
    public void testContextTimezone() throws Exception
    {
        String inputStr = "1997-07-16T19:20:30.45+0100";
        final String tzId = "PST";

        
        assertTrue(MAPPER.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE));
        final ObjectReader r = MAPPER
                .readerFor(Calendar.class)
                .with(TimeZone.getTimeZone(tzId));

        
        Calendar cal = r.readValue(quote(inputStr));
        TimeZone tz = cal.getTimeZone();
        assertEquals(tzId, tz.getID());

        assertEquals(1997, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH));

        
        assertEquals(20, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
        assertEquals(11, cal.get(Calendar.HOUR_OF_DAY));

        
        cal = r.without(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .readValue(quote(inputStr));

        
        
        
        
        

    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCalendarArrayUnwrap
    public void testCalendarArrayUnwrap() throws Exception
    {
        ObjectReader reader = new ObjectMapper()
                .readerFor(CalendarBean.class)
                .without(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        final String inputDate = "1972-12-28T00:00:00.000+0000";
        final String input = aposToQuotes("{'v':['"+inputDate+"']}");
        try {
            reader.readValue(input);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (MismatchedInputException exp) {
            verifyException(exp, "Cannot deserialize");
            verifyException(exp, "out of START_ARRAY");
        }

        reader = reader.with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        CalendarBean bean = reader.readValue(input);
        assertNotNull(bean._v);
        assertEquals(1972, bean._v.get(Calendar.YEAR));

        
        try {
            reader.readValue(aposToQuotes("{'v':['"+inputDate+"','"+inputDate+"']}"));
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            verifyException(exp, "Attempted to unwrap");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testLenientCalendar
    public void testLenientCalendar() throws Exception
    {
        final String JSON = aposToQuotes("{'value':'2015-11-32'}");

        
        LenientCalendarBean lenBean = MAPPER.readValue(JSON, LenientCalendarBean.class);
        assertEquals(Calendar.DECEMBER, lenBean.value.get(Calendar.MONTH));
        assertEquals(2, lenBean.value.get(Calendar.DAY_OF_MONTH));

        
        try {
            MAPPER.readValue(JSON, StrictCalendarBean.class);
            fail("Should not pass with invalid (with strict) date value");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize value of type `java.util.Calendar`");
            verifyException(e, "from String \"2015-11-32\"");
            verifyException(e, "expected format");
        }

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(java.util.Date.class)
            .setFormat(JsonFormat.Value.forLeniency(Boolean.FALSE));
        try {
            mapper.readValue(quote("2015-11-32"), java.util.Date.class);
            fail("Should not pass with invalid (with strict) date value");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize value of type `java.util.Date`");
            verifyException(e, "from String \"2015-11-32\"");
            verifyException(e, "expected format");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testInvalidFormat
    public void testInvalidFormat() throws Exception
    {
        try {
            MAPPER.readValue(quote("foobar"), Date.class);
            fail("Should have failed with an exception");
        } catch (InvalidFormatException e) {
            verifyException(e, "Cannot deserialize value of type `java.util.Date` from String");
            assertEquals("foobar", e.getValue());
            assertEquals(Date.class, e.getTargetType());
        } catch (Exception e) {
            fail("Wrong type of exception ("+e.getClass().getName()+"), should get "
                    +InvalidFormatException.class.getName());
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testFailWhenCaseSensitiveAndNameIsNotUpperCase
    public void testFailWhenCaseSensitiveAndNameIsNotUpperCase() throws IOException {
        try {
            READER_DEFAULT.forType(TestEnum.class).readValue("\"Jackson\"");
            fail("InvalidFormatException expected");
        } catch (InvalidFormatException e) {
            verifyException(e, "value not one of declared Enum instance names: [JACKSON, OK, RULES]");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testFailWhenCaseSensitiveAndToStringIsUpperCase
    public void testFailWhenCaseSensitiveAndToStringIsUpperCase() throws IOException {
        ObjectReader r = READER_DEFAULT.forType(LowerCaseEnum.class)
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        try {
            r.readValue("\"A\"");
            fail("InvalidFormatException expected");
        } catch (InvalidFormatException e) {
            verifyException(e, "value not one of declared Enum instance names: [a, b, c]");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testEnumDesIgnoringCaseWithLowerCaseContent
    public void testEnumDesIgnoringCaseWithLowerCaseContent() throws IOException {
        assertEquals(TestEnum.JACKSON,
                READER_IGNORE_CASE.forType(TestEnum.class).readValue(quote("jackson")));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testEnumDesIgnoringCaseWithUpperCaseToString
    public void testEnumDesIgnoringCaseWithUpperCaseToString() throws IOException {
        ObjectReader r = MAPPER_IGNORE_CASE.readerFor(LowerCaseEnum.class)
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        assertEquals(LowerCaseEnum.A, r.readValue("\"A\""));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testIgnoreCaseInEnumList
    public void testIgnoreCaseInEnumList() throws Exception {
        TestEnum[] enums = READER_IGNORE_CASE.forType(TestEnum[].class)
            .readValue("[\"jacksON\", \"ruLes\"]");

        assertEquals(2, enums.length);
        assertEquals(TestEnum.JACKSON, enums[0]);
        assertEquals(TestEnum.RULES, enums[1]);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testIgnoreCaseInEnumSet
    public void testIgnoreCaseInEnumSet() throws IOException {
        ObjectReader r = READER_IGNORE_CASE.forType(new TypeReference<EnumSet<TestEnum>>() { });
        EnumSet<TestEnum> set = r.readValue("[\"jackson\"]");
        assertEquals(1, set.size());
        assertTrue(set.contains(TestEnum.JACKSON));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testIgnoreCaseViaFormat
    public void testIgnoreCaseViaFormat() throws Exception
    {
        final String JSON = aposToQuotes("{'value':'ok'}");

        
        EnumBean pojo = READER_DEFAULT.forType(EnumBean.class)
            .readValue(JSON);
        assertEquals(TestEnum.OK, pojo.value);

        
        try {
            READER_DEFAULT.forType(StrictCaseBean.class)
                    .readValue(JSON);
            fail("Should not pass");
        } catch (InvalidFormatException e) {
            verifyException(e, "value not one of declared Enum instance names: [JACKSON, OK, RULES]");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDefaultReadTest::testWithoutCustomFeatures
    public void testWithoutCustomFeatures() throws Exception
    {
        final ObjectReader r = MAPPER.reader();

        _verifyOkDeserialization(r, "ZERO", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnum.class, SimpleEnum.ONE);
        _verifyOkDeserialization(r, "0", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnum.class, SimpleEnum.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnum.class);
        _verifyFailingDeserialization(r, "2", SimpleEnum.class);

        _verifyOkDeserialization(r, "ZERO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnumWithDefault.class);
        _verifyOkDeserialization(r, "0", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "2", SimpleEnumWithDefault.class);

        _verifyFailingDeserialization(r, "ZERO", CustomEnum.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnum.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnum.class);
        _verifyOkDeserialization(r, "0", CustomEnum.class, CustomEnum.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnum.class, CustomEnum.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnum.class);

        _verifyFailingDeserialization(r, "ZERO", CustomEnumWithDefault.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnumWithDefault.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnumWithDefault.class);
        _verifyOkDeserialization(r, "0", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnumWithDefault.class, CustomEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnumWithDefault.class);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDefaultReadTest::testWithFailOnNumbers
    public void testWithFailOnNumbers() throws Exception
    {
        ObjectReader r = MAPPER.reader()
                .with(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);

        _verifyOkDeserialization(r, "ZERO", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnum.class, SimpleEnum.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnum.class);
        _verifyFailingDeserialization(r, "0", SimpleEnum.class);
        _verifyFailingDeserialization(r, "1", SimpleEnum.class);
        _verifyFailingDeserialization(r, "2", SimpleEnum.class);

        _verifyOkDeserialization(r, "ZERO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnumWithDefault.class);
        _verifyFailingDeserialization(r, "0", SimpleEnumWithDefault.class);
        _verifyFailingDeserialization(r, "1", SimpleEnumWithDefault.class);
        _verifyFailingDeserialization(r, "2", SimpleEnumWithDefault.class);

        _verifyFailingDeserialization(r, "ZERO", CustomEnum.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnum.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnum.class);
        _verifyOkDeserialization(r, "0", CustomEnum.class, CustomEnum.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnum.class, CustomEnum.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnum.class);

        _verifyFailingDeserialization(r, "ZERO", CustomEnumWithDefault.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnumWithDefault.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnumWithDefault.class);
        _verifyOkDeserialization(r, "0", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnumWithDefault.class, CustomEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnumWithDefault.class);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDefaultReadTest::testWithReadUnknownAsDefault
    public void testWithReadUnknownAsDefault() throws Exception
    {
        ObjectReader r = MAPPER.reader()
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        _verifyOkDeserialization(r, "ZERO", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnum.class, SimpleEnum.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnum.class);
        _verifyOkDeserialization(r, "0", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnum.class, SimpleEnum.ONE);
        _verifyFailingDeserialization(r, "2", SimpleEnum.class);

        _verifyOkDeserialization(r, "ZERO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyOkDeserialization(r, "TWO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "0", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyOkDeserialization(r, "2", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);

        _verifyFailingDeserialization(r, "ZERO", CustomEnum.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnum.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnum.class);
        _verifyOkDeserialization(r, "0", CustomEnum.class, CustomEnum.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnum.class, CustomEnum.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnum.class);

        _verifyOkDeserialization(r, "ZERO", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "TWO", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "0", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnumWithDefault.class, CustomEnumWithDefault.ONE);
        _verifyOkDeserialization(r, "2", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDefaultReadTest::testWithFailOnNumbersAndReadUnknownAsDefault
    public void testWithFailOnNumbersAndReadUnknownAsDefault()
        throws Exception
    {
        ObjectReader r = MAPPER.reader()
                .with(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        _verifyOkDeserialization(r, "ZERO", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnum.class, SimpleEnum.ONE);

        _verifyFailingDeserialization(r, "TWO", SimpleEnum.class);
        _verifyFailingDeserialization(r, "0", SimpleEnum.class);
        _verifyFailingDeserialization(r, "1", SimpleEnum.class);
        _verifyFailingDeserialization(r, "2", SimpleEnum.class);

        _verifyOkDeserialization(r, "ZERO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyOkDeserialization(r, "TWO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);

        _verifyFailingDeserialization(r, "ZERO", CustomEnum.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnum.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnum.class);

        _verifyOkDeserialization(r, "0", CustomEnum.class, CustomEnum.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnum.class, CustomEnum.ONE);

        _verifyFailingDeserialization(r, "2", CustomEnum.class);

        _verifyOkDeserialization(r, "ZERO", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "TWO", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "0", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnumWithDefault.class, CustomEnumWithDefault.ONE);

        
        
        
        _verifyOkDeserialization(r, "0", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "2", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        
        
        
        _verifyOkDeserialization(r, "2", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testSimple
    public void testSimple() throws Exception
    {
        
        String JSON = "\"OK\" \"RULES\"  null";
        
        JsonParser jp = MAPPER.getFactory().createParser(JSON);

        assertEquals(TestEnum.OK, MAPPER.readValue(jp, TestEnum.class));
        assertEquals(TestEnum.RULES, MAPPER.readValue(jp, TestEnum.class));

        
        assertNull(MAPPER.readValue(jp, TestEnum.class));

        
        assertFalse(jp.hasCurrentToken());

        
        assertEquals(TestEnum.JACKSON, MAPPER.readValue(" 0 ", TestEnum.class));

        
        try {
             MAPPER.readValue("\"NO-SUCH-VALUE\"", TestEnum.class);
            fail("Expected an exception for bogus enum value...");
        } catch (MismatchedInputException jex) {
            verifyException(jex, "value not one of declared");
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testComplexEnum
    public void testComplexEnum() throws Exception
    {
        String json = MAPPER.writeValueAsString(TimeUnit.SECONDS);
        assertEquals(quote("SECONDS"), json);
        TimeUnit result = MAPPER.readValue(json, TimeUnit.class);
        assertSame(TimeUnit.SECONDS, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAnnotated
    public void testAnnotated() throws Exception
    {
        AnnotatedTestEnum e = MAPPER.readValue("\"JACKSON\"", AnnotatedTestEnum.class);
        
        assertEquals(AnnotatedTestEnum.OK, e);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testSubclassedEnums
    public void testSubclassedEnums() throws Exception
    {
        EnumWithSubClass value = MAPPER.readValue("\"A\"", EnumWithSubClass.class);
        assertEquals(EnumWithSubClass.A, value);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testToStringEnums
    public void testToStringEnums() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        LowerCaseEnum value = m.readValue("\"c\"", LowerCaseEnum.class);
        assertEquals(LowerCaseEnum.C, value);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testNumbersToEnums
    public void testNumbersToEnums() throws Exception
    {
        
        assertFalse(MAPPER.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS));
        TestEnum value = MAPPER.readValue("1", TestEnum.class);
        assertSame(TestEnum.RULES, value);

        
        ObjectReader r = MAPPER.readerFor(TestEnum.class)
                .with(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
        try {
            value = r.readValue("1");
            fail("Expected an error");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize");
            verifyException(e, "not allowed to deserialize Enum value out of number: disable");
        }

        
        try {
            value = r.readValue(quote("1"));
            fail("Expected an error");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize");
            
            verifyException(e, "value not one of declared Enum");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumsWithIndex
    public void testEnumsWithIndex() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        String json = m.writeValueAsString(TestEnum.RULES);
        assertEquals(String.valueOf(TestEnum.RULES.ordinal()), json);
        TestEnum result = m.readValue(json, TestEnum.class);
        assertSame(TestEnum.RULES, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumsWithJsonValue
    public void testEnumsWithJsonValue() throws Exception
    {
        
        EnumWithJsonValue e = MAPPER.readValue(quote("foo"), EnumWithJsonValue.class);
        assertSame(EnumWithJsonValue.A, e);
        e = MAPPER.readValue(quote("bar"), EnumWithJsonValue.class);
        assertSame(EnumWithJsonValue.B, e);

        
        EnumSet<EnumWithJsonValue> set = MAPPER.readValue("[\"bar\"]",
                new TypeReference<EnumSet<EnumWithJsonValue>>() { });
        assertNotNull(set);
        assertEquals(1, set.size());
        assertTrue(set.contains(EnumWithJsonValue.B));
        assertFalse(set.contains(EnumWithJsonValue.A));

        
        EnumMap<EnumWithJsonValue,Integer> map = MAPPER.readValue("{\"foo\":13}",
                new TypeReference<EnumMap<EnumWithJsonValue, Integer>>() { });
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(13), map.get(EnumWithJsonValue.A));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesReadAsNull
    public void testAllowUnknownEnumValuesReadAsNull() throws Exception
    {
        
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        assertNull(reader.forType(TestEnum.class).readValue("\"NO-SUCH-VALUE\""));
        assertNull(reader.forType(TestEnum.class).readValue(" 4343 "));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesReadAsNullWithCreatorMethod
    public void testAllowUnknownEnumValuesReadAsNullWithCreatorMethod() throws Exception
    {
        
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        assertNull(reader.forType(StrictEnumCreator.class).readValue("\"NO-SUCH-VALUE\""));
        assertNull(reader.forType(StrictEnumCreator.class).readValue(" 4343 "));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesForEnumSets
    public void testAllowUnknownEnumValuesForEnumSets() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        EnumSet<TestEnum> result = reader.forType(new TypeReference<EnumSet<TestEnum>>() { })
                .readValue("[\"NO-SUCH-VALUE\"]");
        assertEquals(0, result.size());
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesAsMapKeysReadAsNull
    public void testAllowUnknownEnumValuesAsMapKeysReadAsNull() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        ClassWithEnumMapKey result = reader.forType(ClassWithEnumMapKey.class)
                .readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}");
        assertTrue(result.map.containsKey(null));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled
    public void testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled() throws Exception
    {
        assertFalse(MAPPER.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL));
         try {
             MAPPER.readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}", ClassWithEnumMapKey.class);
             fail("Expected an exception for bogus enum value...");
         } catch (InvalidFormatException jex) {
             verifyException(jex, "Cannot deserialize Map key of type `com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest$TestEnum`");
         }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumsWithEmpty
    public void testEnumsWithEmpty() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
       TestEnum result = mapper.readValue("\"\"", TestEnum.class);
       assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testGenericEnumDeserialization
    public void testGenericEnumDeserialization() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       SimpleModule module = new SimpleModule("foobar");
       module.addDeserializer(Enum.class, new LcEnumDeserializer());
       mapper.registerModule(module);
       
       assertEquals(TestEnum.JACKSON, mapper.readValue(quote("jackson"), TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testUnwrappedEnum
    public void testUnwrappedEnum() throws Exception {
        final ObjectMapper mapper = newObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        assertEquals(TestEnum.JACKSON, mapper.readValue("[" + quote("JACKSON") + "]", TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testUnwrappedEnumException
    public void testUnwrappedEnumException() throws Exception {
        final ObjectMapper mapper = newObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            Object v = mapper.readValue("[" + quote("JACKSON") + "]",
                    TestEnum.class);
            fail("Exception was not thrown on deserializing a single array element of type enum; instead got: "+v);
        } catch (MismatchedInputException exp) {
            
            verifyException(exp, "Cannot deserialize");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testIndexAsString
    public void testIndexAsString() throws Exception
    {
        
        TestEnum en = MAPPER.readValue("2", TestEnum.class);
        assertSame(TestEnum.values()[2], en);

        
        en = MAPPER.readValue(quote("1"), TestEnum.class);
        assertSame(TestEnum.values()[1], en);

        
        final ObjectMapper mapper = newObjectMapper();
        mapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        try {
            en = mapper.readValue(quote("1"), TestEnum.class);
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize value of type");
            verifyException(e, "EnumDeserializationTest$TestEnum");
            verifyException(e, "value looks like quoted Enum index");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithJsonPropertyRename
    public void testEnumWithJsonPropertyRename() throws Exception
    {
        String json = MAPPER.writeValueAsString(new EnumWithPropertyAnno[] {
                EnumWithPropertyAnno.B, EnumWithPropertyAnno.A
        });
        assertEquals("[\"b\",\"a\"]", json);

        
        EnumWithPropertyAnno[] result = MAPPER.readValue(json, EnumWithPropertyAnno[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertSame(EnumWithPropertyAnno.B, result[0]);
        assertSame(EnumWithPropertyAnno.A, result[1]);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testDeserWithToString1161
    public void testDeserWithToString1161() throws Exception
    {
        Enum1161 result = MAPPER.readerFor(Enum1161.class)
                .readValue(quote("A"));
        assertSame(Enum1161.A, result);

        result = MAPPER.readerFor(Enum1161.class)
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .readValue(quote("a"));
        assertSame(Enum1161.A, result);

        
        result = MAPPER.readerFor(Enum1161.class)
                .without(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .readValue(quote("A"));
        assertSame(Enum1161.A, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotation
    public void testEnumWithDefaultAnnotation() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("\"foo\"", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.OTHER, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationUsingIndexInBound1
    public void testEnumWithDefaultAnnotationUsingIndexInBound1() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("1", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.B, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationUsingIndexInBound2
    public void testEnumWithDefaultAnnotationUsingIndexInBound2() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("2", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.OTHER, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationUsingIndexSameAsLength
    public void testEnumWithDefaultAnnotationUsingIndexSameAsLength() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("3", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.OTHER, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationUsingIndexOutOfBound
    public void testEnumWithDefaultAnnotationUsingIndexOutOfBound() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("4", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.OTHER, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationWithConstructor
    public void testEnumWithDefaultAnnotationWithConstructor() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnnoAndConstructor myEnum = mapper.readValue("\"foo\"", EnumWithDefaultAnnoAndConstructor.class);
        assertNull("When using a constructor, the default value annotation shouldn't be used.", myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testExceptionFromCustomEnumKeyDeserializer
    public void testExceptionFromCustomEnumKeyDeserializer() throws Exception {
        ObjectMapper mapper = newObjectMapper()
                .registerModule(new EnumModule());
        try {
            mapper.readValue("{\"TWO\": \"dumpling\"}",
                    new TypeReference<Map<AnEnum, String>>() {});
            fail("No exception");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Undefined AnEnum"));
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testEnumMaps
    public void testEnumMaps() throws Exception
    {
        EnumMap<TestEnum,String> value = MAPPER.readValue("{\"OK\":\"value\"}",
                new TypeReference<EnumMap<TestEnum,String>>() { });
        assertEquals("value", value.get(TestEnum.OK));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testToStringEnumMaps
    public void testToStringEnumMaps() throws Exception
    {
        
        ObjectReader r = MAPPER.reader()
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        EnumMap<LowerCaseEnum,String> value = r.forType(
            new TypeReference<EnumMap<LowerCaseEnum,String>>() { })
                .readValue("{\"a\":\"value\"}");
        assertEquals("value", value.get(LowerCaseEnum.A));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testCustomEnumMapWithDefaultCtor
    public void testCustomEnumMapWithDefaultCtor() throws Exception
    {
        MySimpleEnumMap map = MAPPER.readValue(aposToQuotes("{'RULES':'waves'}"),
                MySimpleEnumMap.class);   
        assertEquals(1, map.size());
        assertEquals("waves", map.get(TestEnum.RULES));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testCustomEnumMapFromString
    public void testCustomEnumMapFromString() throws Exception
    {
        FromStringEnumMap map = MAPPER.readValue(quote("kewl"), FromStringEnumMap.class);   
        assertEquals(1, map.size());
        assertEquals("kewl", map.get(TestEnum.JACKSON));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testCustomEnumMapWithDelegate
    public void testCustomEnumMapWithDelegate() throws Exception
    {
        FromDelegateEnumMap map = MAPPER.readValue(aposToQuotes("{'foo':'bar'}"), FromDelegateEnumMap.class);   
        assertEquals(1, map.size());
        assertEquals("{foo=bar}", map.get(TestEnum.OK));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testCustomEnumMapFromProps
    public void testCustomEnumMapFromProps() throws Exception
    {
        FromPropertiesEnumMap map = MAPPER.readValue(aposToQuotes(
                "{'a':13,'RULES':'jackson','b':-731,'OK':'yes'}"),
                FromPropertiesEnumMap.class);

        assertEquals(13, map.a0);
        assertEquals(-731, map.b0);

        assertEquals("jackson", map.get(TestEnum.RULES));
        assertEquals("yes", map.get(TestEnum.OK));
        assertEquals(2, map.size());
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testEnumMapAsPolymorphic
    public void testEnumMapAsPolymorphic() throws Exception
    {
        EnumMap<Enum1859, String> enumMap = new EnumMap<>(Enum1859.class);
        enumMap.put(Enum1859.A, "Test");
        enumMap.put(Enum1859.B, "stuff");
        Pojo1859 input = new Pojo1859(enumMap);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@type");

        
         

        String json = mapper.writeValueAsString(input);
        Pojo1859 result = mapper.readValue(json, Pojo1859.class);
        assertNotNull(result);
        assertNotNull(result.values);
        assertEquals(2, result.values.size());
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testUnknownKeyAsDefault
    public void testUnknownKeyAsDefault() throws Exception
    {
        
        EnumMap<TestEnumWithDefault,String> value = MAPPER
                .readerFor(new TypeReference<EnumMap<TestEnumWithDefault,String>>() { })
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .readValue("{\"unknown\":\"value\"}");
        assertEquals(1, value.size());
        assertEquals("value", value.get(TestEnumWithDefault.OK));

        Map<TestEnumWithDefault,String> value2 = MAPPER
                .readerFor(new TypeReference<Map<TestEnumWithDefault,String>>() { })
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .readValue("{\"unknown\":\"value\"}");
        assertEquals(1, value2.size());
        assertEquals("value", value2.get(TestEnumWithDefault.OK));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testUnknownKeyAsNull
    public void testUnknownKeyAsNull() throws Exception
    {
        
        EnumMap<TestEnumWithDefault,String> value = MAPPER
                .readerFor(new TypeReference<EnumMap<TestEnumWithDefault,String>>() { })
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .readValue("{\"unknown\":\"value\"}");
        assertEquals(0, value.size());

        
        Map<TestEnumWithDefault,String> value2 = MAPPER
                .readerFor(new TypeReference<Map<TestEnumWithDefault,String>>() { })
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .readValue("{\"unknown\":\"value\"}");
        
        
        assertEquals(1, value2.size());
        assertEquals("value", value2.get(null));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testAtomicBoolean
    public void testAtomicBoolean() throws Exception
    {
        AtomicBoolean b = MAPPER.readValue("true", AtomicBoolean.class);
        assertTrue(b.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testAtomicInt
    public void testAtomicInt() throws Exception
    {
        AtomicInteger value = MAPPER.readValue("13", AtomicInteger.class);
        assertEquals(13, value.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testAtomicLong
    public void testAtomicLong() throws Exception
    {
        AtomicLong value = MAPPER.readValue("12345678901", AtomicLong.class);
        assertEquals(12345678901L, value.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testAtomicReference
    public void testAtomicReference() throws Exception
    {
        AtomicReference<long[]> value = MAPPER.readValue("[1,2]",
                new com.fasterxml.jackson.core.type.TypeReference<AtomicReference<long[]>>() { });
        Object ob = value.get();
        assertNotNull(ob);
        assertEquals(long[].class, ob.getClass());
        long[] longs = (long[]) ob;
        assertNotNull(longs);
        assertEquals(2, longs.length);
        assertEquals(1, longs[0]);
        assertEquals(2, longs[1]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testAbsentExclusion
    public void testAbsentExclusion() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new SimpleWrapper(null)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testSerPropInclusionAlways
    public void testSerPropInclusionAlways() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.ALWAYS);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testSerPropInclusionNonNull
    public void testSerPropInclusionNonNull() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_NULL);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testSerPropInclusionNonAbsent
    public void testSerPropInclusionNonAbsent() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_ABSENT);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testSerPropInclusionNonEmpty
    public void testSerPropInclusionNonEmpty() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_EMPTY);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testPolymorphicAtomicReference
    public void testPolymorphicAtomicReference() throws Exception
    {
        RefWrapper input = new RefWrapper(13);
        String json = MAPPER.writeValueAsString(input);
        
        RefWrapper result = MAPPER.readValue(json, RefWrapper.class);
        assertNotNull(result.w);
        Object ob = result.w.get();
        assertEquals(Impl.class, ob.getClass());
        assertEquals(13, ((Impl) ob).value);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testFilteringOfAtomicReference
    public void testFilteringOfAtomicReference() throws Exception
    {
        SimpleWrapper input = new SimpleWrapper(null);
        ObjectMapper mapper = MAPPER;

        
        assertEquals(aposToQuotes("{'value':null}"), mapper.writeValueAsString(input));

        
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude
                .Include.NON_NULL);
        assertEquals(aposToQuotes("{'value':null}"), mapper.writeValueAsString(input));

        
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude
                .Include.NON_EMPTY);
        assertEquals("{}", mapper.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testTypeRefinement
    public void testTypeRefinement() throws Exception
    {
        RefiningWrapper input = new RefiningWrapper();
        BigDecimal bd = new BigDecimal("0.25");
        input.value = new AtomicReference<Serializable>(bd);
        String json = MAPPER.writeValueAsString(input);

        
        RefiningWrapper result = MAPPER.readValue(json, RefiningWrapper.class);
        assertNotNull(result.value);
        Object ob = result.value.get();
        assertEquals(BigDecimal.class, ob.getClass());
        assertEquals(bd, ob);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testDeserializeWithContentAs
    public void testDeserializeWithContentAs() throws Exception
    {
        AtomicRefReadWrapper result = MAPPER.readValue(aposToQuotes("{'value':'abc'}"),
                AtomicRefReadWrapper.class);
         Object v = result.value.get();
         assertNotNull(v);
         assertEquals(WrappedString.class, v.getClass());
         assertEquals("abc", ((WrappedString)v).value);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testWithUnwrapping
    public void testWithUnwrapping() throws Exception
    {
         String jsonExp = aposToQuotes("{'XX.name':'Bob'}");
         String jsonAct = MAPPER.writeValueAsString(new UnwrappingRefParent());
         assertEquals(jsonExp, jsonAct);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testWithCustomDeserializer
    public void testWithCustomDeserializer() throws Exception
    {
        LCStringWrapper w = MAPPER.readValue(aposToQuotes("{'value':'FoobaR'}"),
                LCStringWrapper.class);
        assertEquals("foobar", w.value.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testEmpty1256
    public void testEmpty1256() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        String json = mapper.writeValueAsString(new Issue1256Bean());
        assertEquals("{}", json);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testNullValueHandling
    public void testNullValueHandling() throws Exception
    {
        AtomicReference<Double> inputData = new AtomicReference<Double>();
        String json = MAPPER.writeValueAsString(inputData);
        AtomicReference<Double> readData = (AtomicReference<Double>) MAPPER.readValue(json, AtomicReference.class);
        assertNotNull(readData);
        assertNull(readData.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesDeserTest::testNullWithinNested
    public void testNullWithinNested() throws Exception
    {
        final ObjectReader r = MAPPER.readerFor(MyBean2303.class);
        MyBean2303 intRef = r.readValue(" {\"refRef\": 2 } ");
        assertNotNull(intRef.refRef);
        assertNotNull(intRef.refRef.get());
        assertEquals(intRef.refRef.get().get(), new Integer(2));

        MyBean2303 nullRef = r.readValue(" {\"refRef\": null } ");
        assertNotNull(nullRef.refRef);
        assertNotNull(nullRef.refRef.get());
        assertNull(nullRef.refRef.get().get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKCollectionsDeserTest::testSingletonCollections
    public void testSingletonCollections() throws Exception
    {
        final TypeReference<?> xbeanListType = new TypeReference<List<XBean>>() { };

        String json = MAPPER.writeValueAsString(Collections.singleton(new XBean(3)));
        Collection<XBean> result = MAPPER.readValue(json, xbeanListType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.iterator().next().x);

        json = MAPPER.writeValueAsString(Collections.singletonList(new XBean(28)));
        result = MAPPER.readValue(json, xbeanListType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(28, result.iterator().next().x);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKCollectionsDeserTest::testUnmodifiableSet
    public void testUnmodifiableSet() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Set<String> theSet = Collections.unmodifiableSet(Collections.singleton("a"));
        String json = mapper.writeValueAsString(theSet);

        assertEquals("[\"java.util.Collections$UnmodifiableSet\",[\"a\"]]", json);

        
         
         
         
         
        
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testNaN
    public void testNaN() throws Exception
    {
        Float result = MAPPER.readValue(" \"NaN\"", Float.class);
        assertEquals(Float.valueOf(Float.NaN), result);

        Double d = MAPPER.readValue(" \"NaN\"", Double.class);
        assertEquals(Double.valueOf(Double.NaN), d);

        Number num = MAPPER.readValue(" \"NaN\"", Number.class);
        assertEquals(Double.valueOf(Double.NaN), num);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDoubleInf
    public void testDoubleInf() throws Exception
    {
        Double result = MAPPER.readValue(" \""+Double.POSITIVE_INFINITY+"\"", Double.class);
        assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), result);

        result = MAPPER.readValue(" \""+Double.NEGATIVE_INFINITY+"\"", Double.class);
        assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testEmptyAsNumber
    public void testEmptyAsNumber() throws Exception
    {
        assertNull(MAPPER.readValue(quote(""), Byte.class));
        assertNull(MAPPER.readValue(quote(""), Short.class));
        assertNull(MAPPER.readValue(quote(""), Character.class));
        assertNull(MAPPER.readValue(quote(""), Integer.class));
        assertNull(MAPPER.readValue(quote(""), Long.class));
        assertNull(MAPPER.readValue(quote(""), Float.class));
        assertNull(MAPPER.readValue(quote(""), Double.class));

        assertNull(MAPPER.readValue(quote(""), BigInteger.class));
        assertNull(MAPPER.readValue(quote(""), BigDecimal.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testTextualNullAsNumber
    public void testTextualNullAsNumber() throws Exception
    {
        final String NULL_JSON = quote("null");
        assertNull(MAPPER.readValue(NULL_JSON, Byte.class));
        assertNull(MAPPER.readValue(NULL_JSON, Short.class));
        

        assertNull(MAPPER.readValue(NULL_JSON, Integer.class));
        assertNull(MAPPER.readValue(NULL_JSON, Long.class));
        assertNull(MAPPER.readValue(NULL_JSON, Float.class));
        assertNull(MAPPER.readValue(NULL_JSON, Double.class));

        assertEquals(Byte.valueOf((byte) 0), MAPPER.readValue(NULL_JSON, Byte.TYPE));
        assertEquals(Short.valueOf((short) 0), MAPPER.readValue(NULL_JSON, Short.TYPE));
        

        assertEquals(Integer.valueOf(0), MAPPER.readValue(NULL_JSON, Integer.TYPE));
        assertEquals(Long.valueOf(0L), MAPPER.readValue(NULL_JSON, Long.TYPE));
        assertEquals(Float.valueOf(0f), MAPPER.readValue(NULL_JSON, Float.TYPE));
        assertEquals(Double.valueOf(0d), MAPPER.readValue(NULL_JSON, Double.TYPE));
        
        assertNull(MAPPER.readValue(NULL_JSON, BigInteger.class));
        assertNull(MAPPER.readValue(NULL_JSON, BigDecimal.class));

        
        try {
            MAPPER.readerFor(Integer.TYPE).with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .readValue(NULL_JSON);
            fail("Should not have passed");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce String \"null\"");
        }

        ObjectMapper noCoerceMapper = new ObjectMapper();
        noCoerceMapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        try {
            noCoerceMapper.readValue(NULL_JSON, Integer.TYPE);
            fail("Should not have passed");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce String \"null\"");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDeserializeDecimalHappyPath
    public void testDeserializeDecimalHappyPath() throws Exception {
        String json = "{\"defaultValue\": { \"value\": 123 } }";
        MyBeanHolder result = MAPPER.readValue(json, MyBeanHolder.class);
        assertEquals(BigDecimal.valueOf(123), result.defaultValue.value.decimal);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDeserializeDecimalProperException
    public void testDeserializeDecimalProperException() throws Exception {
        String json = "{\"defaultValue\": { \"value\": \"123\" } }";
        try {
            MAPPER.readValue(json, MyBeanHolder.class);
            fail("should have raised exception");
        } catch (JsonProcessingException e) {
            verifyException(e, "not numeric");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDeserializeDecimalProperExceptionWhenIdSet
    public void testDeserializeDecimalProperExceptionWhenIdSet() throws Exception {
        String json = "{\"id\": 5, \"defaultValue\": { \"value\": \"123\" } }";
        try {
            MyBeanHolder result = MAPPER.readValue(json, MyBeanHolder.class);
            fail("should have raised exception instead value was set to " + result.defaultValue.value.decimal.toString());
        } catch (JsonProcessingException e) {
            verifyException(e, "not numeric");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testScientificNotationAsStringForNumber
    public void testScientificNotationAsStringForNumber() throws Exception
    {
        Object ob = MAPPER.readValue("\"3E-8\"", Number.class);
        assertEquals(Double.class, ob.getClass());
        ob = MAPPER.readValue("\"3e-8\"", Number.class);
        assertEquals(Double.class, ob.getClass());
        ob = MAPPER.readValue("\"300000000\"", Number.class);
        assertEquals(Integer.class, ob.getClass());
        ob = MAPPER.readValue("\"123456789012\"", Number.class);
        assertEquals(Long.class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testIntAsNumber
    public void testIntAsNumber() throws Exception
    {
        
        Number result = MAPPER.readValue(" 123 ", Number.class);
        assertEquals(Integer.valueOf(123), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testLongAsNumber
    public void testLongAsNumber() throws Exception
    {
        
        long exp = 1234567890123L;
        Number result = MAPPER.readValue(String.valueOf(exp), Number.class);
        assertEquals(Long.valueOf(exp), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testBigIntAsNumber
    public void testBigIntAsNumber() throws Exception
    {
        
        BigInteger biggie = new BigInteger("1234567890123456789012345678901234567890");
        Number result = MAPPER.readValue(biggie.toString(), Number.class);
        assertEquals(BigInteger.class, biggie.getClass());
        assertEquals(biggie, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testIntTypeOverride
    public void testIntTypeOverride() throws Exception
    {
        
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

        BigInteger exp = BigInteger.valueOf(123L);

        
        Number result = r.forType(Number.class).readValue(" 123 ");
        assertEquals(BigInteger.class, result.getClass());
        assertEquals(exp, result);

        
         r.forType(Object.class).readValue("123");
        assertEquals(BigInteger.class, result.getClass());
        assertEquals(exp, result);

        
        JsonNode node = r.readTree("  123");
        assertTrue(node.isBigInteger());
        assertEquals(123, node.asInt());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDoubleAsNumber
    public void testDoubleAsNumber() throws Exception
    {
        Number result = MAPPER.readValue(new StringReader(" 1.0 "), Number.class);
        assertEquals(Double.valueOf(1.0), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testFpTypeOverrideSimple
    public void testFpTypeOverrideSimple() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        BigDecimal dec = new BigDecimal("0.1");

        
        Number result = r.forType(Number.class).readValue(dec.toString());
        assertEquals(BigDecimal.class, result.getClass());
        assertEquals(dec, result);

        
        Object value = r.forType(Object.class).readValue(dec.toString());
        assertEquals(BigDecimal.class, result.getClass());
        assertEquals(dec, value);

        JsonNode node = r.readTree(dec.toString());
        assertTrue(node.isBigDecimal());
        assertEquals(dec.doubleValue(), node.asDouble());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testFpTypeOverrideStructured
    public void testFpTypeOverrideStructured() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        BigDecimal dec = new BigDecimal("-19.37");
        
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) r.forType(List.class).readValue("[ "+dec.toString()+" ]");
        assertEquals(1, list.size());
        Object val = list.get(0);
        assertEquals(BigDecimal.class, val.getClass());
        assertEquals(dec, val);

        
        Map<?,?> map = r.forType(Map.class).readValue("{ \"a\" : "+dec.toString()+" }");
        assertEquals(1, map.size());
        val = map.get("a");
        assertEquals(BigDecimal.class, val.getClass());
        assertEquals(dec, val);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testForceIntsToLongs
    public void testForceIntsToLongs() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_LONG_FOR_INTS);

        Object ob = r.forType(Object.class).readValue("42");
        assertEquals(Long.class, ob.getClass());
        assertEquals(Long.valueOf(42L), ob);

        Number n = r.forType(Number.class).readValue("42");
        assertEquals(Long.class, n.getClass());
        assertEquals(Long.valueOf(42L), n);

        
        JsonNode node = r.readTree("42");
        if (!node.isLong()) {
            fail("Expected LongNode, got: "+node.getClass().getName());
        }
        assertEquals(42, node.asInt());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testBooleanPrimitive
    public void testBooleanPrimitive() throws Exception
    {
        
        BooleanBean result = MAPPER.readValue("{\"v\":true}", BooleanBean.class);
        assertTrue(result._v);
        result = MAPPER.readValue("{\"v\":null}", BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        result = MAPPER.readValue("{\"v\":1}", BooleanBean.class);
        assertNotNull(result);
        assertTrue(result._v);

        
        boolean[] array = MAPPER.readValue("[ null, false ]", boolean[].class);
        assertNotNull(array);
        assertEquals(2, array.length);
        assertFalse(array[0]);
        assertFalse(array[1]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testBooleanWrapper
    public void testBooleanWrapper() throws Exception
    {
        Boolean result = MAPPER.readValue("true", Boolean.class);
        assertEquals(Boolean.TRUE, result);
        result = MAPPER.readValue("false", Boolean.class);
        assertEquals(Boolean.FALSE, result);

        
        result = MAPPER.readValue("0", Boolean.class);
        assertEquals(Boolean.FALSE, result);
        result = MAPPER.readValue("1", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testLongToBoolean
    public void testLongToBoolean() throws Exception
    {
        long value = 1L + Integer.MAX_VALUE;
        BooleanWrapper b = MAPPER.readValue("{\"primitive\" : "+value+", \"wrapper\":"+value+", \"ctor\":"+value+"}",
                BooleanWrapper.class);
        assertEquals(Boolean.TRUE, b.wrapper);
        assertTrue(b.primitive);
        assertEquals(Boolean.TRUE, b.ctor);

        
        b = MAPPER.readValue("{\"primitive\" : 0 , \"wrapper\":0, \"ctor\":0}",
                BooleanWrapper.class);
        assertEquals(Boolean.FALSE, b.wrapper);
        assertFalse(b.primitive);
        assertEquals(Boolean.FALSE, b.ctor);

        boolean[] boo = MAPPER.readValue("[ 0, 15, \"\", \"false\", \"True\" ]",
                boolean[].class);
        assertEquals(5, boo.length);
        assertFalse(boo[0]);
        assertTrue(boo[1]);
        assertFalse(boo[2]);
        assertFalse(boo[3]);
        assertTrue(boo[4]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testByteWrapper
    public void testByteWrapper() throws Exception
    {
        Byte result = MAPPER.readValue("   -42\t", Byte.class);
        assertEquals(Byte.valueOf((byte)-42), result);

        
        result = MAPPER.readValue(" \"-12\"", Byte.class);
        assertEquals(Byte.valueOf((byte)-12), result);

        result = MAPPER.readValue(" 39.07", Byte.class);
        assertEquals(Byte.valueOf((byte)39), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testShortWrapper
    public void testShortWrapper() throws Exception
    {
        Short result = MAPPER.readValue("37", Short.class);
        assertEquals(Short.valueOf((short)37), result);

        
        result = MAPPER.readValue(" \"-1009\"", Short.class);
        assertEquals(Short.valueOf((short)-1009), result);

        result = MAPPER.readValue("-12.9", Short.class);
        assertEquals(Short.valueOf((short)-12), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testCharacterWrapper
    public void testCharacterWrapper() throws Exception
    {
        
        Character result = MAPPER.readValue("\"a\"", Character.class);
        assertEquals(Character.valueOf('a'), result);

        
        result = MAPPER.readValue(" "+((int) 'X'), Character.class);
        assertEquals(Character.valueOf('X'), result);
        
        final CharacterWrapperBean wrapper = MAPPER.readValue("{\"v\":null}", CharacterWrapperBean.class);
        assertNotNull(wrapper);
        assertNull(wrapper.getV());

        try {
            MAPPER.readerFor(CharacterBean.class)
                .with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .readValue("{\"v\":null}");
            fail("Attempting to deserialize a 'null' JSON reference into a 'char' property did not throw an exception");
        } catch (MismatchedInputException e) {
            verifyException(e, "cannot map `null`");
        }
        final CharacterBean charBean = MAPPER.readerFor(CharacterBean.class)
                .without(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .readValue("{\"v\":null}");
        assertNotNull(wrapper);
        assertEquals('\u0000', charBean.getV());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testIntWrapper
    public void testIntWrapper() throws Exception
    {
        Integer result = MAPPER.readValue("   -42\t", Integer.class);
        assertEquals(Integer.valueOf(-42), result);

        
        result = MAPPER.readValue(" \"-1200\"", Integer.class);
        assertEquals(Integer.valueOf(-1200), result);

        result = MAPPER.readValue(" 39.07", Integer.class);
        assertEquals(Integer.valueOf(39), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testIntPrimitive
    public void testIntPrimitive() throws Exception
    {
        
        IntBean result = MAPPER.readValue("{\"v\":3}", IntBean.class);
        assertEquals(3, result._v);

        result = MAPPER.readValue("{\"v\":null}", IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        
        int[] array = MAPPER.readValue("[ null ]", int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("{\"v\":[3]}", IntBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (MismatchedInputException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue("{\"v\":[3]}", IntBean.class);
        assertEquals(3, result._v);
        
        result = mapper.readValue("[{\"v\":[3]}]", IntBean.class);
        assertEquals(3, result._v);
        
        try {
            mapper.readValue("[{\"v\":[3,3]}]", IntBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (MismatchedInputException exp) {
            
        }
        
        result = mapper.readValue("{\"v\":[null]}", IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        array = mapper.readValue("[ [ null ] ]", int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testLongWrapper
    public void testLongWrapper() throws Exception
    {
        Long result = MAPPER.readValue("12345678901", Long.class);
        assertEquals(Long.valueOf(12345678901L), result);

        
        result = MAPPER.readValue(" \"-9876\"", Long.class);
        assertEquals(Long.valueOf(-9876), result);

        result = MAPPER.readValue("1918.3", Long.class);
        assertEquals(Long.valueOf(1918), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testLongPrimitive
    public void testLongPrimitive() throws Exception
    {
        
        LongBean result = MAPPER.readValue("{\"v\":3}", LongBean.class);
        assertEquals(3, result._v);
        result = MAPPER.readValue("{\"v\":null}", LongBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        
        long[] array = MAPPER.readValue("[ null ]", long[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("{\"v\":[3]}", LongBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (MismatchedInputException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue("{\"v\":[3]}", LongBean.class);
        assertEquals(3, result._v);
        
        result = mapper.readValue("[{\"v\":[3]}]", LongBean.class);
        assertEquals(3, result._v);
        
        try {
            mapper.readValue("[{\"v\":[3,3]}]", LongBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (MismatchedInputException exp) {
            
        }
        
        result = mapper.readValue("{\"v\":[null]}", LongBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        array = mapper.readValue("[ [ null ] ]", long[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testIntWithOverride
    public void testIntWithOverride() throws Exception
    {
        IntBean2 result = MAPPER.readValue("{\"v\":8}", IntBean2.class);
        assertEquals(9, result._v);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoublePrimitive
    public void testDoublePrimitive() throws Exception
    {
        
        
        final double value = 0.016;
        DoubleBean result = MAPPER.readValue("{\"v\":"+value+"}", DoubleBean.class);
        assertEquals(value, result._v);
        
        result = MAPPER.readValue("{\"v\":null}", DoubleBean.class);
        assertNotNull(result);
        assertEquals(0.0, result._v);

        
        double[] array = MAPPER.readValue("[ null ]", double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0.0, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testFloatWrapper
    public void testFloatWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Float exp = Float.valueOf(str);
            Float result;

            if (NAN_STRING != str) {
                
                result = MAPPER.readValue(str, Float.class);
                assertEquals(exp, result);
            }

            
            result = MAPPER.readValue(" \""+str+"\"", Float.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoubleWrapper
    public void testDoubleWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Double exp = Double.valueOf(str);
            Double result;

            
            if (NAN_STRING != str) {
                result = MAPPER.readValue(str, Double.class);
               assertEquals(exp, result);
            }
            
            result = MAPPER.readValue(" \""+str+"\"", Double.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoubleAsArray
    public void testDoubleAsArray() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        final double value = 0.016;
        try {
            mapper.readValue("{\"v\":[" + value + "]}", DoubleBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        DoubleBean result = mapper.readValue("{\"v\":[" + value + "]}",
                DoubleBean.class);
        assertEquals(value, result._v);
        
        result = mapper.readValue("[{\"v\":[" + value + "]}]", DoubleBean.class);
        assertEquals(value, result._v);
        
        try {
            mapper.readValue("[{\"v\":[" + value + "," + value + "]}]", DoubleBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (MismatchedInputException exp) {
            
        }
        
        result = mapper.readValue("{\"v\":[null]}", DoubleBean.class);
        assertNotNull(result);
        assertEquals(0d, result._v);

        double[] array = mapper.readValue("[ [ null ] ]", double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0d, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoublePrimitiveNonNumeric
    public void testDoublePrimitiveNonNumeric() throws Exception
    {
        
        
        double value = Double.POSITIVE_INFINITY;
        DoubleBean result = MAPPER.readValue("{\"v\":\""+value+"\"}", DoubleBean.class);
        assertEquals(value, result._v);
        
        
        double[] array = MAPPER.readValue("[ \"Infinity\" ]", double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Double.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testFloatPrimitiveNonNumeric
    public void testFloatPrimitiveNonNumeric() throws Exception
    {
        
        float value = Float.POSITIVE_INFINITY;
        FloatBean result = MAPPER.readValue("{\"v\":\""+value+"\"}", FloatBean.class);
        assertEquals(value, result._v);
        
        
        float[] array = MAPPER.readValue("[ \"Infinity\" ]", float[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Float.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyToNullCoercionForPrimitives
    public void testEmptyToNullCoercionForPrimitives() throws Exception {
        _testEmptyToNullCoercion(int.class, Integer.valueOf(0));
        _testEmptyToNullCoercion(long.class, Long.valueOf(0));
        _testEmptyToNullCoercion(double.class, Double.valueOf(0.0));
        _testEmptyToNullCoercion(float.class, Float.valueOf(0.0f));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testBase64Variants
    public void testBase64Variants() throws Exception
    {
        final byte[] INPUT = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890X".getBytes("UTF-8");
        
        
        Assert.assertArrayEquals(INPUT, MAPPER.readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="),
                byte[].class));
        ObjectReader reader = MAPPER.readerFor(byte[].class);
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MIME_NO_LINEFEEDS).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="
        )));

        
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MIME).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1\\ndnd4eXoxMjM0NTY3ODkwWA=="
        )));
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MODIFIED_FOR_URL).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA"
        )));
        
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.PEM).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamts\\nbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="
        )));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testSequenceOfInts
    public void testSequenceOfInts() throws Exception
    {
        final int NR_OF_INTS = 100;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NR_OF_INTS; ++i) {
            sb.append(" ");
            sb.append(i);
        }
        JsonParser jp = MAPPER.getFactory().createParser(sb.toString());
        for (int i = 0; i < NR_OF_INTS; ++i) {
            Integer result = MAPPER.readValue(jp, Integer.class);
            assertEquals(Integer.valueOf(i), result);
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyStringForWrappers
    public void testEmptyStringForWrappers() throws IOException
    {
        WrappersBean bean;

        bean = MAPPER.readValue("{\"booleanValue\":\"\"}", WrappersBean.class);
        assertNull(bean.booleanValue);
        bean = MAPPER.readValue("{\"byteValue\":\"\"}", WrappersBean.class);
        assertNull(bean.byteValue);

        
        bean = MAPPER.readValue("{\"charValue\":\"\"}", WrappersBean.class);
        assertNull(bean.charValue);

        bean = MAPPER.readValue("{\"shortValue\":\"\"}", WrappersBean.class);
        assertNull(bean.shortValue);
        bean = MAPPER.readValue("{\"intValue\":\"\"}", WrappersBean.class);
        assertNull(bean.intValue);
        bean = MAPPER.readValue("{\"longValue\":\"\"}", WrappersBean.class);
        assertNull(bean.longValue);
        bean = MAPPER.readValue("{\"floatValue\":\"\"}", WrappersBean.class);
        assertNull(bean.floatValue);
        bean = MAPPER.readValue("{\"doubleValue\":\"\"}", WrappersBean.class);
        assertNull(bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyStringForPrimitives
    public void testEmptyStringForPrimitives() throws IOException
    {
        PrimitivesBean bean;
        bean = MAPPER.readValue("{\"booleanValue\":\"\"}", PrimitivesBean.class);
        assertFalse(bean.booleanValue);
        bean = MAPPER.readValue("{\"byteValue\":\"\"}", PrimitivesBean.class);
        assertEquals((byte) 0, bean.byteValue);
        bean = MAPPER.readValue("{\"charValue\":\"\"}", PrimitivesBean.class);
        assertEquals((char) 0, bean.charValue);
        bean = MAPPER.readValue("{\"shortValue\":\"\"}", PrimitivesBean.class);
        assertEquals((short) 0, bean.shortValue);
        bean = MAPPER.readValue("{\"intValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0, bean.intValue);
        bean = MAPPER.readValue("{\"longValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0L, bean.longValue);
        bean = MAPPER.readValue("{\"floatValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0f, bean.floatValue);
        bean = MAPPER.readValue("{\"doubleValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0, bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyStringFailForPrimitives
    public void testEmptyStringFailForPrimitives() throws IOException
    {
        _verifyEmptyStringFailForPrimitives("booleanValue");
        _verifyEmptyStringFailForPrimitives("byteValue");
        _verifyEmptyStringFailForPrimitives("charValue");
        _verifyEmptyStringFailForPrimitives("shortValue");
        _verifyEmptyStringFailForPrimitives("intValue");
        _verifyEmptyStringFailForPrimitives("longValue");
        _verifyEmptyStringFailForPrimitives("floatValue");
        _verifyEmptyStringFailForPrimitives("doubleValue");
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testNullForPrimitives
    public void testNullForPrimitives() throws IOException
    {
        
        PrimitivesBean bean = MAPPER.readValue(
                "{\"intValue\":null, \"booleanValue\":null, \"doubleValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.intValue);
        assertEquals(false, bean.booleanValue);
        assertEquals(0.0, bean.doubleValue);

        bean = MAPPER.readValue("{\"byteValue\":null, \"longValue\":null, \"floatValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals((byte) 0, bean.byteValue);
        assertEquals(0L, bean.longValue);
        assertEquals(0.0f, bean.floatValue);

        
        final ObjectReader reader = MAPPER
                .readerFor(PrimitivesBean.class)
                .with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        
        try {
            reader.readValue("{\"booleanValue\":null}");
            fail("Expected failure for boolean + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type boolean");
            verifyPath(e, "booleanValue");
        }
        
        try {
            reader.readValue("{\"byteValue\":null}");
            fail("Expected failure for byte + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type byte");
            verifyPath(e, "byteValue");
        }
        try {
            reader.readValue("{\"charValue\":null}");
            fail("Expected failure for char + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type char");
            verifyPath(e, "charValue");
        }
        try {
            reader.readValue("{\"shortValue\":null}");
            fail("Expected failure for short + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type short");
            verifyPath(e, "shortValue");
        }
        try {
            reader.readValue("{\"intValue\":null}");
            fail("Expected failure for int + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type int");
            verifyPath(e, "intValue");
        }
        try {
            reader.readValue("{\"longValue\":null}");
            fail("Expected failure for long + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type long");
            verifyPath(e, "longValue");
        }

        
        try {
            reader.readValue("{\"floatValue\":null}");
            fail("Expected failure for float + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type float");
            verifyPath(e, "floatValue");
        }
        try {
            reader.readValue("{\"doubleValue\":null}");
            fail("Expected failure for double + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type double");
            verifyPath(e, "doubleValue");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testNullForPrimitivesViaCreator
    public void testNullForPrimitivesViaCreator() throws IOException
    {
        try {
             MAPPER
                    .readerFor(PrimitiveCreatorBean.class)
                    .with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                    .readValue(aposToQuotes("{'a': null}"));
            fail("Expected failure for `int` and `null`");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type int");
            verifyPath(e, "a");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testNullForPrimitiveArrays
    public void testNullForPrimitiveArrays() throws IOException
    {
        _testNullForPrimitiveArrays(boolean[].class, Boolean.FALSE);
        _testNullForPrimitiveArrays(byte[].class, Byte.valueOf((byte) 0));
        _testNullForPrimitiveArrays(char[].class, Character.valueOf((char) 0), false);
        _testNullForPrimitiveArrays(short[].class, Short.valueOf((short)0));
        _testNullForPrimitiveArrays(int[].class, Integer.valueOf(0));
        _testNullForPrimitiveArrays(long[].class, Long.valueOf(0L));
        _testNullForPrimitiveArrays(float[].class, Float.valueOf(0f));
        _testNullForPrimitiveArrays(double[].class, Double.valueOf(0d));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testVoidDeser
    public void testVoidDeser() throws Exception
    {
        VoidBean bean = MAPPER.readValue(aposToQuotes("{'value' : 123 }"),
                VoidBean.class);
        assertNull(bean.value);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testInvalidStringCoercionFail
    public void testInvalidStringCoercionFail() throws IOException
    {
        _testInvalidStringCoercionFail(boolean[].class);
        _testInvalidStringCoercionFail(byte[].class);

        

        _testInvalidStringCoercionFail(short[].class);
        _testInvalidStringCoercionFail(int[].class);
        _testInvalidStringCoercionFail(long[].class);
        _testInvalidStringCoercionFail(float[].class);
        _testInvalidStringCoercionFail(double[].class);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testByteBuffer
    public void testByteBuffer() throws Exception
    {
        byte[] INPUT = new byte[] { 1, 3, 9, -1, 6 };
        String exp = MAPPER.writeValueAsString(INPUT);
        ByteBuffer result = MAPPER.readValue(exp,  ByteBuffer.class); 
        assertNotNull(result);
        assertEquals(INPUT.length, result.remaining());
        for (int i = 0; i < INPUT.length; ++i) {
            assertEquals(INPUT[i], result.get());
        }
        assertEquals(0, result.remaining());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testCharset
    public void testCharset() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        assertSame(UTF8, MAPPER.readValue(quote("UTF-8"), Charset.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testClass
    public void testClass() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        assertSame(String.class, mapper.readValue(quote("java.lang.String"), Class.class));

        
        assertSame(Boolean.TYPE, mapper.readValue(quote("boolean"), Class.class));
        assertSame(Byte.TYPE, mapper.readValue(quote("byte"), Class.class));
        assertSame(Short.TYPE, mapper.readValue(quote("short"), Class.class));
        assertSame(Character.TYPE, mapper.readValue(quote("char"), Class.class));
        assertSame(Integer.TYPE, mapper.readValue(quote("int"), Class.class));
        assertSame(Long.TYPE, mapper.readValue(quote("long"), Class.class));
        assertSame(Float.TYPE, mapper.readValue(quote("float"), Class.class));
        assertSame(Double.TYPE, mapper.readValue(quote("double"), Class.class));
        assertSame(Void.TYPE, mapper.readValue(quote("void"), Class.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testClassWithParams
    public void testClassWithParams() throws IOException
    {
        String json = MAPPER.writeValueAsString(new ParamClassBean("Foobar"));

        ParamClassBean result = MAPPER.readValue(json, ParamClassBean.class);
        assertEquals("Foobar", result.name);
        assertSame(String.class, result.clazz);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testCurrency
    public void testCurrency() throws IOException
    {
        Currency usd = Currency.getInstance("USD");
        assertEquals(usd, new ObjectMapper().readValue(quote("USD"), Currency.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testFile
    public void testFile() throws Exception
    {
        
        File src = new File("/test").getAbsoluteFile();
        String abs = src.getAbsolutePath();

        
        String json = MAPPER.writeValueAsString(abs);
        File result = MAPPER.readValue(json, File.class);
        assertEquals(abs, result.getAbsolutePath());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testLocale
    public void testLocale() throws IOException
    {
        assertEquals(new Locale("en"), MAPPER.readValue(quote("en"), Locale.class));
        assertEquals(new Locale("es", "ES"), MAPPER.readValue(quote("es_ES"), Locale.class));
        assertEquals(new Locale("FI", "fi", "savo"),
                MAPPER.readValue(quote("fi_FI_savo"), Locale.class));
        assertEquals(new Locale("en", "US"),
                MAPPER.readValue(quote("en-US"), Locale.class));

        
        Locale loc = MAPPER.readValue(quote(""), Locale.class);
        assertSame(Locale.ROOT, loc);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testCharSequence
    public void testCharSequence() throws IOException
    {
        CharSequence cs = MAPPER.readValue("\"abc\"", CharSequence.class);
        assertEquals(String.class, cs.getClass());
        assertEquals("abc", cs.toString());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testInetAddress
    public void testInetAddress() throws IOException
    {
        InetAddress address = MAPPER.readValue(quote("127.0.0.1"), InetAddress.class);
        assertEquals("127.0.0.1", address.getHostAddress());

        
        final String HOST = "google.com";
        address = MAPPER.readValue(quote(HOST), InetAddress.class);
        assertEquals(HOST, address.getHostName());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testInetSocketAddress
    public void testInetSocketAddress() throws IOException
    {
        InetSocketAddress address = MAPPER.readValue(quote("127.0.0.1"), InetSocketAddress.class);
        assertEquals("127.0.0.1", address.getAddress().getHostAddress());

        InetSocketAddress ip6 = MAPPER.readValue(
                quote("2001:db8:85a3:8d3:1319:8a2e:370:7348"), InetSocketAddress.class);
        assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ip6.getAddress().getHostAddress());

        InetSocketAddress ip6port = MAPPER.readValue(
                quote("[2001:db8:85a3:8d3:1319:8a2e:370:7348]:443"), InetSocketAddress.class);
        assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ip6port.getAddress().getHostAddress());
        assertEquals(443, ip6port.getPort());

        
        final String HOST = "www.google.com";
        address = MAPPER.readValue(quote(HOST), InetSocketAddress.class);
        assertEquals(HOST, address.getHostName());

        final String HOST_AND_PORT = HOST+":80";
        address = MAPPER.readValue(quote(HOST_AND_PORT), InetSocketAddress.class);
        assertEquals(HOST, address.getHostName());
        assertEquals(80, address.getPort());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testRegexps
    public void testRegexps() throws IOException
    {
        final String PATTERN_STR = "abc:\\s?(\\d+)";
        Pattern exp = Pattern.compile(PATTERN_STR);
        
        String json = MAPPER.writeValueAsString(exp);
        Pattern result = MAPPER.readValue(json, Pattern.class);
        assertEquals(exp.pattern(), result.pattern());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testStackTraceElement
    public void testStackTraceElement() throws Exception
    {
        StackTraceElement elem = null;
        try {
            throw new IllegalStateException();
        } catch (Exception e) {
            elem = e.getStackTrace()[0];
        }
        String json = MAPPER.writeValueAsString(elem);
        StackTraceElement back = MAPPER.readValue(json, StackTraceElement.class);
        
        assertEquals("testStackTraceElement", back.getMethodName());
        assertEquals(elem.getLineNumber(), back.getLineNumber());
        assertEquals(elem.getClassName(), back.getClassName());
        assertEquals(elem.isNativeMethod(), back.isNativeMethod());
        assertTrue(back.getClassName().endsWith("JDKStringLikeTypesTest"));
        assertFalse(back.isNativeMethod());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testStackTraceElementWithCustom
    public void testStackTraceElementWithCustom() throws Exception
    {
        
        StackTraceBean bean = MAPPER.readValue(aposToQuotes("{'Location':'foobar'}"),
                StackTraceBean.class);
        assertNotNull(bean);
        assertNotNull(bean.location);
        assertEquals(StackTraceBean.NUM, bean.location.getLineNumber());

        
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StackTraceElement.class, new MyStackTraceElementDeserializer());
        mapper.registerModule(module);
        
        StackTraceElement elem = mapper.readValue("123", StackTraceElement.class);
        assertNotNull(elem);
        assertEquals(StackTraceBean.NUM, elem.getLineNumber());
 
        
        
        IOException ioe = mapper.readValue(aposToQuotes("{'stackTrace':[ 123, 456 ]}"),
                IOException.class);
        assertNotNull(ioe);
        StackTraceElement[] traces = ioe.getStackTrace();
        assertNotNull(traces);
        assertEquals(2, traces.length);
        assertEquals(StackTraceBean.NUM, traces[0].getLineNumber());
        assertEquals(StackTraceBean.NUM, traces[1].getLineNumber());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testStringBuilder
    public void testStringBuilder() throws Exception
    {
        StringBuilder sb = MAPPER.readValue(quote("abc"), StringBuilder.class);
        assertEquals("abc", sb.toString());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testURI
    public void testURI() throws Exception
    {
        final ObjectReader reader = MAPPER.readerFor(URI.class);
        final URI value = new URI("http://foo.com");
        assertEquals(value, reader.readValue("\""+value.toString()+"\""));

        
        URI result = reader.readValue(quote(""));
        assertNotNull(result);
        assertEquals(URI.create(""), result);
        
        
        try {
            result = reader.readValue(quote("a b"));
            fail("Should not accept malformed URI, instead got: "+result);
        } catch (InvalidFormatException e) {
            verifyException(e, "not a valid textual representation");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testURL
    public void testURL() throws Exception
    {
        URL exp = new URL("http://foo.com");
        assertEquals(exp, MAPPER.readValue("\""+exp.toString()+"\"", URL.class));

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(exp);
        assertSame(exp, MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();

        
        try {
            URL result = MAPPER.readValue(quote("a b"), URL.class);
            fail("Should not accept malformed URI, instead got: "+result);
        } catch (InvalidFormatException e) {
            verifyException(e, "not a valid textual representation");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testUUID
    public void testUUID() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        
        final String NULL_UUID = "00000000-0000-0000-0000-000000000000";
        
        for (String value : new String[] {
                "76e6d183-5f68-4afa-b94a-922c1fdb83f8",
                "540a88d1-e2d8-4fb1-9396-9212280d0a7f",
                "2c9e441d-1cd0-472d-9bab-69838f877574",
                "591b2869-146e-41d7-8048-e8131f1fdec5",
                "82994ac2-7b23-49f2-8cc5-e24cf6ed77be",
                "00000007-0000-0000-0000-000000000000"
        }) {
            
            mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
            
            UUID uuid = UUID.fromString(value);
            assertEquals(uuid,
                    mapper.readValue(quote(value), UUID.class));
        }
        
        
        final String TEMPL = NULL_UUID;
        final String chars = "123456789abcdefABCDEF";

        for (int i = 0; i < chars.length(); ++i) {
            String value = TEMPL.replace('0', chars.charAt(i));
            assertEquals(UUID.fromString(value).toString(),
                    mapper.readValue(quote(value), UUID.class).toString());
        }

        
        String base64 = Base64Variants.getDefaultVariant().encode(new byte[16]);
        assertEquals(UUID.fromString(NULL_UUID),
                mapper.readValue(quote(base64), UUID.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testUUIDInvalid
    public void testUUIDInvalid() throws Exception
    {
        
        try {
            MAPPER.readValue(quote("abcde"), UUID.class);
            fail("Should fail on invalid UUID string");
        } catch (InvalidFormatException e) {
            verifyException(e, "UUID has to be represented by standard");
        }
        try {
            MAPPER.readValue(quote("76e6d183-5f68-4afa-b94a-922c1fdb83fx"), UUID.class);
            fail("Should fail on invalid UUID string");
        } catch (InvalidFormatException e) {
            verifyException(e, "non-hex character 'x'");
        }
        
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testUUIDAux
    public void testUUIDAux() throws Exception
    {
        
        final UUID value = UUID.fromString("76e6d183-5f68-4afa-b94a-922c1fdb83f8");

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), UUID.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(value);
        assertSame(value, MAPPER.readValue(buf.asParser(), UUID.class));

        
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeLong(value.getMostSignificantBits());
        out.writeLong(value.getLeastSignificantBits());
        byte[] data = bytes.toByteArray();
        assertEquals(16, data.length);
        
        buf.writeObject(data);

        UUID value2 = MAPPER.readValue(buf.asParser(), UUID.class);
        
        assertEquals(value, value2);
        buf.close();
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testBigUntypedMap
    public void testBigUntypedMap() throws Exception
    {
        Map<String,Object> map = new LinkedHashMap<String,Object>();
        for (int i = 0; i < 1100; ++i) {
            if ((i & 1) == 0) {
                map.put(String.valueOf(i), Integer.valueOf(i));
            } else {
                Map<String,Object> map2 = new LinkedHashMap<String,Object>();
                map2.put("x", Integer.valueOf(i));
                map.put(String.valueOf(i), map2);
            }
        }
        String json = MAPPER.writeValueAsString(map);
        Object bound = MAPPER.readValue(json, Object.class);
        assertEquals(map, bound);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testUntypedMap2
    public void testUntypedMap2() throws Exception
    {
        
        String JSON = "{ \"a\" : \"x\" }";

        @SuppressWarnings("unchecked")
        HashMap<String,Object> result =  MAPPER.readValue(JSON, HashMap.class);
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);

        assertEquals(1, result.size());

        assertEquals("x", result.get("a"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testUntypedMap3
    public void testUntypedMap3() throws Exception
    {
        String JSON = "{\"a\":[{\"a\":\"b\"},\"value\"]}";
        Map<?,?> result = MAPPER.readValue(JSON, Map.class);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(1, result.size());
        Object ob = result.get("a");
        assertNotNull(ob);
        Collection<?> list = (Collection<?>)ob;
        assertEquals(2, list.size());

        JSON = "{ \"var1\":\"val1\", \"var2\":\"val2\", "
            +"\"subvars\": ["
            +" {  \"subvar1\" : \"subvar2\", \"x\" : \"y\" }, "
            +" { \"a\":1 } ]"
            +" }"
            ;
        result = MAPPER.readValue(JSON, Map.class);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(3, result.size());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testSpecialMap
    public void testSpecialMap() throws IOException
    {
       final ObjectWrapperMap map = MAPPER.readValue(UNTYPED_MAP_JSON, ObjectWrapperMap.class);
       assertNotNull(map);
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testGenericMap
    public void testGenericMap() throws IOException
    {
        final Map<String, ObjectWrapper> map = MAPPER.readValue
            (UNTYPED_MAP_JSON,
             new TypeReference<Map<String, ObjectWrapper>>() { });
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        Map<?,?> result = m.readValue(quote(""), Map.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testExactStringIntMap
    public void testExactStringIntMap() throws Exception
    {
        
        String JSON = "{ \"foo\" : 13, \"bar\" : -39, \n \"\" : 0 }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<HashMap<String,Integer>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(3, result.size());

        assertEquals(Integer.valueOf(13), result.get("foo"));
        assertEquals(Integer.valueOf(-39), result.get("bar"));
        assertEquals(Integer.valueOf(0), result.get(""));
        assertNull(result.get("foobar"));
        assertNull(result.get(" "));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testIntBooleanMap
    public void testIntBooleanMap() throws Exception
    {
        
        String JSON = "{ \"1\" : true, \"-1\" : false }";
        Map<?,Object> result = MAPPER.readValue
            (JSON, new TypeReference<HashMap<Integer,Object>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals(Boolean.TRUE, result.get(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, result.get(Integer.valueOf(-1)));
        assertNull(result.get("foobar"));
        assertNull(result.get(0));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testExactStringStringMap
    public void testExactStringStringMap() throws Exception
    {
        
        String JSON = "{ \"a\" : \"b\" }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<TreeMap<String,String>>() { });

        assertNotNull(result);
        assertEquals(TreeMap.class, result.getClass());
        assertEquals(1, result.size());

        assertEquals("b", result.get("a"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testGenericStringIntMap
    public void testGenericStringIntMap() throws Exception
    {
        
        String JSON = "{ \"a\" : 1, \"b\" : 2, \"c\" : -99 }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<Map<String,Integer>>() { });
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(3, result.size());

        assertEquals(Integer.valueOf(-99), result.get("c"));
        assertEquals(Integer.valueOf(2), result.get("b"));
        assertEquals(Integer.valueOf(1), result.get("a"));

        assertNull(result.get(""));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testAbstractMapDefault
    public void testAbstractMapDefault() throws Exception
    {
        final AbstractMapWrapper result = MAPPER.readValue("{\"values\":{\"foo\":42}}",
                AbstractMapWrapper.class);
        assertNotNull(result);
        assertEquals(LinkedHashMap.class, result.values.getClass());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testEnumMap
    public void testEnumMap() throws Exception
    {
        String JSON = "{ \"KEY1\" : \"\", \"WHATEVER\" : null }";

        
        EnumMap<Key,String> result = MAPPER.readValue
            (JSON, new TypeReference<EnumMap<Key,String>>() { });

        assertNotNull(result);
        assertEquals(EnumMap.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals("", result.get(Key.KEY1));
        
        assertTrue(result.containsKey(Key.WHATEVER));
        assertNull(result.get(Key.WHATEVER));

        
        assertFalse(result.containsKey(Key.KEY2));
        assertNull(result.get(Key.KEY2));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testMapWithEnums
    public void testMapWithEnums() throws Exception
    {
        String JSON = "{ \"KEY2\" : \"WHATEVER\" }";

        
        Map<Enum<?>,Enum<?>> result = MAPPER.readValue
            (JSON, new TypeReference<Map<Key,Key>>() { });

        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(1, result.size());

        assertEquals(Key.WHATEVER, result.get(Key.KEY2));
        assertNull(result.get(Key.WHATEVER));
        assertNull(result.get(Key.KEY1));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testEnumPolymorphicSerializationTest
    public void testEnumPolymorphicSerializationTest() throws Exception 
    {
        ObjectMapper mapper = new ObjectMapper();
        List<ITestType> testTypesList = new ArrayList<ITestType>();
        testTypesList.add(ConcreteType.ONE);
        testTypesList.add(ConcreteType.TWO);
        ListContainer listContainer = new ListContainer();
        listContainer.testTypes = testTypesList;
        String json = mapper.writeValueAsString(listContainer);
        listContainer = mapper.readValue(json, ListContainer.class);
        EnumMapContainer enumMapContainer = new EnumMapContainer();
        EnumMap<KeyEnum,ITestType> testTypesMap = new EnumMap<KeyEnum,ITestType>(KeyEnum.class);
        testTypesMap.put(KeyEnum.A, ConcreteType.ONE);
        testTypesMap.put(KeyEnum.B, ConcreteType.TWO);
        enumMapContainer.testTypes = testTypesMap;
        
        json = mapper.writeValueAsString(enumMapContainer);
        enumMapContainer = mapper.readValue(json, EnumMapContainer.class);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testDateMap
    public void testDateMap() throws Exception
    {
    	 Date date1=new Date(123456000L);
    	 DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
         
    	 String JSON = "{ \""+  fmt.format(date1)+"\" : \"\", \""+new Date(0).getTime()+"\" : null }";
    	 HashMap<Date,String> result=  MAPPER.readValue
    	            (JSON, new TypeReference<HashMap<Date,String>>() { });
    	 
    	 assertNotNull(result);
    	 assertEquals(HashMap.class, result.getClass());
    	 assertEquals(2, result.size());
    	 
    	 assertTrue(result.containsKey(date1));
    	 assertEquals("", result.get(new Date(123456000L)));

    	 assertTrue(result.containsKey(new Date(0)));
    	 assertNull(result.get(new Date(0)));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testCalendarMap
    public void testCalendarMap() throws Exception
    {
        
        TimeZone tz = MAPPER.getSerializationConfig().getTimeZone();        
        Calendar c = Calendar.getInstance(tz);

        c.setTimeInMillis(123456000L);
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        String JSON = "{ \""+fmt.format(c.getTime())+"\" : \"\", \""+new Date(0).getTime()+"\" : null }";
        HashMap<Calendar,String> result = MAPPER.readValue
                (JSON, new TypeReference<HashMap<Calendar,String>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(2, result.size());

        assertTrue(result.containsKey(c));
        assertEquals("", result.get(c));
        c.setTimeInMillis(0);
        assertTrue(result.containsKey(c));
        assertNull(result.get(c));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testUUIDKeyMap
    public void testUUIDKeyMap() throws Exception
    {
         UUID key = UUID.nameUUIDFromBytes("foobar".getBytes("UTF-8"));
         String JSON = "{ \""+key+"\":4}";
         Map<UUID,Object> result = MAPPER.readValue(JSON, new TypeReference<Map<UUID,Object>>() { });
         assertNotNull(result);
         assertEquals(1, result.size());
         Object ob = result.keySet().iterator().next();
         assertNotNull(ob);
         assertEquals(UUID.class, ob.getClass());
         assertEquals(key, ob);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testLocaleKeyMap
    public void testLocaleKeyMap() throws Exception {
        Locale key = Locale.CHINA;
        String JSON = "{ \"" + key + "\":4}";
        Map<Locale, Object> result = MAPPER.readValue(JSON, new TypeReference<Map<Locale, Object>>() {
        });
        assertNotNull(result);
        assertEquals(1, result.size());
        Object ob = result.keySet().iterator().next();
        assertNotNull(ob);
        assertEquals(Locale.class, ob.getClass());
        assertEquals(key, ob);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testCurrencyKeyMap
    public void testCurrencyKeyMap() throws Exception {
        Currency key = Currency.getInstance("USD");
        String JSON = "{ \"" + key + "\":4}";
        Map<Currency, Object> result = MAPPER.readValue(JSON, new TypeReference<Map<Currency, Object>>() {
        });
        assertNotNull(result);
        assertEquals(1, result.size());
        Object ob = result.keySet().iterator().next();
        assertNotNull(ob);
        assertEquals(Currency.class, ob.getClass());
        assertEquals(key, ob);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testKeyWithCreator
    public void testKeyWithCreator() throws Exception
    {
        
        KeyType key = MAPPER.readValue(quote("abc"), KeyType.class);
        assertEquals("abc", key.value);

        Map<KeyType,Integer> map = MAPPER.readValue("{\"foo\":3}", new TypeReference<Map<KeyType,Integer>>() {} );
        assertEquals(1, map.size());
        key = map.keySet().iterator().next();
        assertEquals("foo", key.value);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testClassKeyMap
    public void testClassKeyMap() throws Exception {
        ClassStringMap map = MAPPER.readValue(aposToQuotes("{'java.lang.String':'foo'}"),
                ClassStringMap.class);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("foo", map.get(String.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testcharSequenceKeyMap
    public void testcharSequenceKeyMap() throws Exception {
        String JSON = aposToQuotes("{'a':'b'}");
        Map<CharSequence,String> result = MAPPER.readValue(JSON, new TypeReference<Map<CharSequence,String>>() { });
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("b", result.get("a"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testMapWithDeserializer
    public void testMapWithDeserializer() throws Exception
    {
        CustomMap result = MAPPER.readValue(quote("xyz"), CustomMap.class);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("x"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testMapError
    public void testMapError() throws Exception
    {
        try {
            Object result = MAPPER.readValue("[ 1, 2 ]", 
                    new TypeReference<Map<String,String>>() { });
            fail("Expected an exception, but got result value: "+result);
        } catch (JsonMappingException jex) {
            verifyException(jex, "START_ARRAY");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testNoCtorMap
    public void testNoCtorMap() throws Exception
    {
        try {
            BrokenMap result = MAPPER.readValue("{ \"a\" : 3 }", BrokenMap.class);
            
            assertNull(result);
        } catch (JsonMappingException e) {
            
            verifyException(e, "no default constructor found");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializerCachingTest::testCachedSerialize
    public void testCachedSerialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = aposToQuotes("{'data':{'1st':'onedata','2nd':'twodata'}}");

        
        NonAnnotatedMapHolderClass ignored = mapper.readValue(json, NonAnnotatedMapHolderClass.class);
        assertTrue(ignored.data.containsKey("1st"));
        assertTrue(ignored.data.containsKey("2nd"));

        
        MapHolder model2 = mapper.readValue(json, MapHolder.class);
        if (!model2.data.containsKey("1st (CUSTOM)")
            || !model2.data.containsKey("2nd (CUSTOM)")) {
            fail("Not using custom key deserializer for input: "+json+"; resulted in: "+model2.data);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testBooleanMapKeyDeserialization
    public void testBooleanMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Boolean, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'true':'foobar'}}"), type);
                
        assertEquals(1, result.map.size());
        Assert.assertEquals(Boolean.TRUE, result.map.entrySet().iterator().next().getKey());

        result = MAPPER.readValue(aposToQuotes("{'map':{'false':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Boolean.FALSE, result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testByteMapKeyDeserialization
    public void testByteMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Byte, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'13':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Byte.valueOf((byte) 13), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testShortMapKeyDeserialization
    public void testShortMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Short, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'13':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Short.valueOf((short) 13), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testIntegerMapKeyDeserialization
    public void testIntegerMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Integer, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'-3':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Integer.valueOf(-3), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testLongMapKeyDeserialization
    public void testLongMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Long, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'42':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Long.valueOf(42), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testFloatMapKeyDeserialization
    public void testFloatMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Float, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'3.5':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Float.valueOf(3.5f), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testDoubleMapKeyDeserialization
    public void testDoubleMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Double, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'0.25':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Double.valueOf(0.25), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testDeserializeKeyViaFactory
    public void testDeserializeKeyViaFactory() throws Exception
    {
        Map<FullName, Double> map =
            MAPPER.readValue("{\"first.last\": 42}",
                    new TypeReference<Map<FullName, Double>>() { });
        Map.Entry<FullName, Double> entry = map.entrySet().iterator().next();
        FullName key = entry.getKey();
        assertEquals(key._firstname, "first");
        assertEquals(key._lastname, "last");
        assertEquals(entry.getValue().doubleValue(), 42, 0);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testByteArrayMapKeyDeserialization
    public void testByteArrayMapKeyDeserialization() throws Exception
    {
        byte[] binary = new byte[] { 1, 2, 4, 8, 16, 33, 79 };
        String encoded = Base64Variants.MIME.encode(binary);

        MapWrapper<byte[], String> result = MAPPER.readValue(
                aposToQuotes("{'map':{'"+encoded+"':'foobar'}}"),
                new TypeReference<MapWrapper<byte[], String>>() { });
        assertEquals(1, result.map.size());
        Map.Entry<byte[],String> entry = result.map.entrySet().iterator().next();
        assertEquals("foobar", entry.getValue());
        byte[] key = entry.getKey();
        Assert.assertArrayEquals(binary, key);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testMapEntrySimpleTypes
    public void testMapEntrySimpleTypes() throws Exception
    {
        List<Map.Entry<String,Long>> stuff = MAPPER.readValue(aposToQuotes("[{'a':15},{'b':42}]"),
                new TypeReference<List<Map.Entry<String,Long>>>() { });
        assertNotNull(stuff);
        assertEquals(2, stuff.size());
        assertNotNull(stuff.get(1));
        assertEquals("b", stuff.get(1).getKey());
        assertEquals(Long.valueOf(42), stuff.get(1).getValue());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testMapEntryWithStringBean
    public void testMapEntryWithStringBean() throws Exception
    {
        List<Map.Entry<Integer,StringWrapper>> stuff = MAPPER.readValue(aposToQuotes("[{'28':'Foo'},{'13':'Bar'}]"),
                new TypeReference<List<Map.Entry<Integer,StringWrapper>>>() { });
        assertNotNull(stuff);
        assertEquals(2, stuff.size());
        assertNotNull(stuff.get(1));
        assertEquals(Integer.valueOf(13), stuff.get(1).getKey());
        
        StringWrapper sw = stuff.get(1).getValue();
        assertEquals("Bar", sw.str);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testMapEntryFail
    public void testMapEntryFail() throws Exception
    {
        try {
             MAPPER.readValue(aposToQuotes("[{'28':'Foo','13':'Bar'}]"),
                    new TypeReference<List<Map.Entry<Integer,StringWrapper>>>() { });
            fail("Should not have passed");
        } catch (Exception e) {
            verifyException(e, "more than one entry in JSON");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testReadProperties
    public void testReadProperties() throws Exception
    {
        Properties props = MAPPER.readValue(aposToQuotes("{'a':'foo', 'b':123, 'c':true}"),
                Properties.class);
        assertEquals(3, props.size());
        assertEquals("foo", props.getProperty("a"));
        assertEquals("123", props.getProperty("b"));
        assertEquals("true", props.getProperty("c"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testSingletonMapRoundtrip
    public void testSingletonMapRoundtrip() throws Exception
    {
        final TypeReference<?> type = new TypeReference<Map<String,IntWrapper>>() { };

        String json = MAPPER.writeValueAsString(Collections.singletonMap("value", new IntWrapper(5)));
        Map<String,IntWrapper> result = MAPPER.readValue(json, type);
        assertNotNull(result);
        assertEquals(1, result.size());
        IntWrapper w = result.get("value");
        assertNotNull(w);
        assertEquals(5, w.i);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testSampleDoc
    public void testSampleDoc() throws Exception
    {
        final String JSON = SAMPLE_DOC_JSON_SPEC;

        
        Object root = MAPPER.readValue(JSON, Object.class);

        assertType(root, Map.class);
        Map<?,?> rootMap = (Map<?,?>) root;
        assertEquals(1, rootMap.size());
        Map.Entry<?,?> rootEntry =  rootMap.entrySet().iterator().next();
        assertEquals("Image", rootEntry.getKey());
        Object image = rootEntry.getValue();
        assertType(image, Map.class);
        Map<?,?> imageMap = (Map<?,?>) image;
        assertEquals(5, imageMap.size());

        Object value = imageMap.get("Width");
        assertType(value, Integer.class);
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_WIDTH), value);

        value = imageMap.get("Height");
        assertType(value, Integer.class);
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_HEIGHT), value);

        assertEquals(SAMPLE_SPEC_VALUE_TITLE, imageMap.get("Title"));

        
        value = imageMap.get("Thumbnail");
        assertType(value, Map.class);
        Map<?,?> tnMap = (Map<?,?>) value;
        assertEquals(3, tnMap.size());

        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_HEIGHT), tnMap.get("Height"));
        
        assertEquals(SAMPLE_SPEC_VALUE_TN_WIDTH, tnMap.get("Width"));
        assertEquals(SAMPLE_SPEC_VALUE_TN_URL, tnMap.get("Url"));

        
        value = imageMap.get("IDs");
        assertType(value, List.class);
        List<Object> ids = (List<Object>) value;
        assertEquals(4, ids.size());
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID1), ids.get(0));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID2), ids.get(1));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID3), ids.get(2));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID4), ids.get(3));

        
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedMap
    public void testUntypedMap() throws Exception
    {
        
        String JSON = "{ \"foo\" : \"bar\", \"crazy\" : true, \"null\" : null }";

        
        @SuppressWarnings("unchecked")
        Map<String,Object> result = (Map<String,Object>)MAPPER.readValue(JSON, Object.class);
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);

        assertEquals(3, result.size());

        assertEquals("bar", result.get("foo"));
        assertEquals(Boolean.TRUE, result.get("crazy"));
        assertNull(result.get("null"));

        
        assertNull(result.get("bar"));
        assertNull(result.get(3));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testNestedUntypes
    public void testNestedUntypes() throws IOException
    {
        
        Object root = MAPPER.readValue(aposToQuotes("{'a':3,'b':[1,2]}"),
                Object.class);
        assertTrue(root instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) root;
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(3), map.get("a"));
        Object ob = map.get("b");
        assertTrue(ob instanceof List<?>);
        List<?> l = (List<?>) ob;
        assertEquals(2, l.size());
        assertEquals(Integer.valueOf(2), l.get(1));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testObjectSerializeWithLong
    public void testObjectSerializeWithLong() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT, As.PROPERTY);
        final long VALUE = 1337800584532L;

        String serialized = "{\"timestamp\":"+VALUE+"}";
        
        JsonNode deserialized = mapper.readTree(serialized);
        assertEquals(VALUE, deserialized.get("timestamp").asLong());
        
        Map<?,?> deserMap = mapper.readValue(serialized, Map.class);
        Number n = (Number) deserMap.get("timestamp");
        assertNotNull(n);
        assertSame(Long.class, n.getClass());
        assertEquals(Long.valueOf(VALUE), n);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithCustomScalarDesers
    public void testUntypedWithCustomScalarDesers() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(String.class, new UCStringDeserializer());
        m.addDeserializer(Number.class, new CustomNumberDeserializer(13));
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        Object ob = mapper.readValue("{\"a\":\"b\", \"nr\":1 }", Object.class);
        assertTrue(ob instanceof Map);
        Object value = ((Map<?,?>) ob).get("a");
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals("B", value);

        value = ((Map<?,?>) ob).get("nr");
        assertNotNull(value);
        assertTrue(value instanceof Number);
        assertEquals(Integer.valueOf(13), value);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testNonVanilla
    public void testNonVanilla() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(String.class, new UCStringDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        List<?> l = (List<?>) mapper.readValue("[ true, false, 7, 0.5, \"foo\"]", Object.class);
        assertEquals(5, l.size());
        assertEquals(Boolean.TRUE, l.get(0));
        assertEquals(Boolean.FALSE, l.get(1));
        assertEquals(Integer.valueOf(7), l.get(2));
        assertEquals(Double.valueOf(0.5), l.get(3));
        assertEquals("FOO", l.get(4));

        l = (List<?>) mapper.readValue("[ {}, [] ]", Object.class);
        assertEquals(2, l.size());
        assertTrue(l.get(0) instanceof Map<?,?>);
        assertTrue(l.get(1) instanceof List<?>);

        ObjectReader rDefault = mapper.readerFor(WrappedPolymorphicUntyped.class);
        ObjectReader rAlt = rDefault
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS,
                        DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        WrappedPolymorphicUntyped w;

        w = rDefault.readValue(aposToQuotes("{'value':10}"));
        assertEquals(Integer.valueOf(10), w.value);
        w = rAlt.readValue(aposToQuotes("{'value':10}"));
        assertEquals(BigInteger.TEN, w.value);

        w = rDefault.readValue(aposToQuotes("{'value':5.0}"));
        assertEquals(Double.valueOf(5.0), w.value);
        w = rAlt.readValue(aposToQuotes("{'value':5.0}"));
        assertEquals(new BigDecimal("5.0"), w.value);

        StringBuilder sb = new StringBuilder(100).append("[0");
        for (int i = 1; i < 100; ++i) {
            sb.append(", ").append(i);
        }
        sb.append("]");
        final String INT_ARRAY_JSON = sb.toString();

        
        Object ob = mapper.readerFor(Object.class)
                .with(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
                .readValue(INT_ARRAY_JSON);
        assertTrue(ob instanceof Object[]);
        Object[] obs = (Object[]) ob;
        for (int i = 0; i < 100; ++i) {
            assertEquals(Integer.valueOf(i), obs[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithListDeser
    public void testUntypedWithListDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(List.class, new ListDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("[1, 2, true]", Object.class);
        assertTrue(ob instanceof List<?>);
        List<?> l = (List<?>) ob;
        assertEquals(3, l.size());
        assertEquals("X1", l.get(0));
        assertEquals("X2", l.get(1));
        assertEquals("Xtrue", l.get(2));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithMapDeser
    public void testUntypedWithMapDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(Map.class, new YMapDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("{\"a\":true}", Object.class);
        assertTrue(ob instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) ob;
        assertEquals(1, map.size());
        assertEquals("Ytrue", map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testNestedUntyped989
    public void testNestedUntyped989() throws IOException
    {
        DelegatingUntyped pojo;
        ObjectReader r = MAPPER.readerFor(DelegatingUntyped.class);

        pojo = r.readValue("[]");
        assertTrue(pojo.value instanceof List);
        pojo = r.readValue("[{}]");
        assertTrue(pojo.value instanceof List);
        
        pojo = r.readValue("{}");
        assertTrue(pojo.value instanceof Map);
        pojo = r.readValue("{\"a\":[]}");
        assertTrue(pojo.value instanceof Map);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithJsonArrays
    public void testUntypedWithJsonArrays() throws Exception
    {
        
        Object ob = MAPPER.readValue("[1]", Object.class);
        assertTrue(ob instanceof List<?>);

        
        MAPPER.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        ob = MAPPER.readValue("[1]", Object.class);
        assertEquals(Object[].class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedIntAsLong
    public void testUntypedIntAsLong() throws Exception
    {
        final String JSON = aposToQuotes("{'value':3}");
        WrappedUntyped1460 w = MAPPER.readerFor(WrappedUntyped1460.class)
                .readValue(JSON);
        assertEquals(Integer.valueOf(3), w.value);

        w = MAPPER.readerFor(WrappedUntyped1460.class)
                .with(DeserializationFeature.USE_LONG_FOR_INTS)
                .readValue(JSON);
        assertEquals(Long.valueOf(3), w.value);
    }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testEmptyList
   public void testEmptyList() throws Exception {
       _verifyCollection(Collections.emptyList());
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testEmptySet
   public void testEmptySet() throws Exception {
       _verifyCollection(Collections.emptySet());
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testEmptyMap
   public void testEmptyMap() throws Exception {
       _verifyMap(Collections.emptyMap());
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testSingletonList
   public void testSingletonList() throws Exception {
       _verifyCollection(Collections.singletonList(Arrays.asList("TheOne")));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testSingletonSet
   public void testSingletonSet() throws Exception {
       _verifyCollection(Collections.singleton(Arrays.asList("TheOne")));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testSingletonMap
   public void testSingletonMap() throws Exception {
       _verifyMap(Collections.singletonMap("foo", "bar"));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testUnmodifiableList
   public void testUnmodifiableList() throws Exception {
       _verifyCollection(Collections.unmodifiableList(Arrays.asList("first", "second")));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testUnmodifiableListFromLinkedList
   public void testUnmodifiableListFromLinkedList() throws Exception {
       final List<String> input = new LinkedList<>();
       input.add("first");
       input.add("second");

       
       
       Collection<?> act = _writeReadCollection(Collections.unmodifiableList(input));
       assertEquals(input, act);

       
       assertEquals(Collections.unmodifiableList(new ArrayList<>(input)).getClass(), act.getClass());
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testUnmodifiableSet
   public void testUnmodifiableSet() throws Exception
   {
       Set<String> input = new LinkedHashSet<>(Arrays.asList("first", "second"));
       _verifyCollection(Collections.unmodifiableSet(input));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testUnmodifiableMap
   public void testUnmodifiableMap() throws Exception
   {
       Map<String,String> input = new LinkedHashMap<>();
       input.put("a", "b");
       input.put("c", "d");
       _verifyMap(Collections.unmodifiableMap(input));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testArraysAsList
   public void testArraysAsList() throws Exception
   {
       
       
       List<String> input = Arrays.asList("a", "bc", "def");
       String json = DEFAULT_MAPPER.writeValueAsString(input);
       List<?> result = DEFAULT_MAPPER.readValue(json, List.class);
       assertEquals(input, result);
   }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testObjectArrayMerging
    public void testObjectArrayMerging() throws Exception
    {
        MergedX<Object[]> input = new MergedX<Object[]>(new Object[] {
                "foo"
        });
        final JavaType type = MAPPER.getTypeFactory().constructType(new TypeReference<MergedX<Object[]>>() {});
        MergedX<Object[]> result = MAPPER.readerFor(type)
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['bar']}"));
        assertSame(input, result);
        assertEquals(2, result.value.length);
        assertEquals("foo", result.value[0]);
        assertEquals("bar", result.value[1]);

        
        result = MAPPER.readerFor(type)
                .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':'zap'}"));
        assertSame(input, result);
        assertEquals(3, result.value.length);
        assertEquals("foo", result.value[0]);
        assertEquals("bar", result.value[1]);
        assertEquals("zap", result.value[2]);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testStringArrayMerging
    public void testStringArrayMerging() throws Exception
    {
        MergedX<String[]> input = new MergedX<String[]>(new String[] { "foo" });
        MergedX<String[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<String[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['bar']}"));
        assertSame(input, result);
        assertEquals(2, result.value.length);
        assertEquals("foo", result.value[0]);
        assertEquals("bar", result.value[1]);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testBooleanArrayMerging
    public void testBooleanArrayMerging() throws Exception
    {
        MergedX<boolean[]> input = new MergedX<boolean[]>(new boolean[] { true, false });
        MergedX<boolean[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<boolean[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[true]}"));
        assertSame(input, result);
        assertEquals(3, result.value.length);
        Assert.assertArrayEquals(new boolean[] { true, false, true }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testByteArrayMerging
    public void testByteArrayMerging() throws Exception
    {
        MergedX<byte[]> input = new MergedX<byte[]>(new byte[] { 1, 2 });
        MergedX<byte[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<byte[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6.0, null]}"));
        assertSame(input, result);
        assertEquals(5, result.value.length);
        Assert.assertArrayEquals(new byte[] { 1, 2, 4, 6, 0 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testShortArrayMerging
    public void testShortArrayMerging() throws Exception
    {
        MergedX<short[]> input = new MergedX<short[]>(new short[] { 1, 2 });
        MergedX<short[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<short[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6]}"));
        assertSame(input, result);
        assertEquals(4, result.value.length);
        Assert.assertArrayEquals(new short[] { 1, 2, 4, 6 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testCharArrayMerging
    public void testCharArrayMerging() throws Exception
    {
        MergedX<char[]> input = new MergedX<char[]>(new char[] { 'a', 'b' });
        MergedX<char[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<char[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['c']}"));
        assertSame(input, result);
        Assert.assertArrayEquals(new char[] { 'a', 'b', 'c' }, result.value);

        
        input = new MergedX<char[]>(new char[] { });
        result = MAPPER
                .readerFor(new TypeReference<MergedX<char[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['c']}"));
        assertSame(input, result);
        Assert.assertArrayEquals(new char[] { 'c' }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testIntArrayMerging
    public void testIntArrayMerging() throws Exception
    {
        MergedX<int[]> input = new MergedX<int[]>(new int[] { 1, 2 });
        MergedX<int[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<int[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6]}"));
        assertSame(input, result);
        assertEquals(4, result.value.length);
        Assert.assertArrayEquals(new int[] { 1, 2, 4, 6 }, result.value);

        
        input = new MergedX<int[]>(new int[] { 3, 4, 6 });
        result = MAPPER
                .readerFor(new TypeReference<MergedX<int[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[ ]}"));
        assertSame(input, result);
        Assert.assertArrayEquals(new int[] { 3, 4, 6 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testLongArrayMerging
    public void testLongArrayMerging() throws Exception
    {
        MergedX<long[]> input = new MergedX<long[]>(new long[] { 1, 2 });
        MergedX<long[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<long[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6]}"));
        assertSame(input, result);
        assertEquals(4, result.value.length);
        Assert.assertArrayEquals(new long[] { 1, 2, 4, 6 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testCollectionMerging
    public void testCollectionMerging() throws Exception
    {
        CollectionWrapper w = MAPPER.readValue(aposToQuotes("{'bag':['b']}"), CollectionWrapper.class);
        assertEquals(2, w.bag.size());
        assertTrue(w.bag.contains("a"));
        assertTrue(w.bag.contains("b"));
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testListMerging
    public void testListMerging() throws Exception
    {
        MergedList w = MAPPER.readValue(aposToQuotes("{'values':['x']}"), MergedList.class);
        assertEquals(2, w.values.size());
        assertTrue(w.values.contains("a"));
        assertTrue(w.values.contains("x"));
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testGenericListMerging
    public void testGenericListMerging() throws Exception
    {
        Collection<String> l = new ArrayList<>();
        l.add("foo");
        MergedX<Collection<String>> input = new MergedX<Collection<String>>(l);

        MergedX<Collection<String>> result = MAPPER
                .readerFor(new TypeReference<MergedX<Collection<String>>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['bar']}"));
        assertSame(input, result);
        assertEquals(2, result.value.size());
        Iterator<String> it = result.value.iterator();
        assertEquals("foo", it.next());
        assertEquals("bar", it.next());
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testEnumSetMerging
    public void testEnumSetMerging() throws Exception
    {
        MergedEnumSet result = MAPPER.readValue(aposToQuotes("{'abc':['A']}"), MergedEnumSet.class);
        assertEquals(2, result.abc.size());
        assertTrue(result.abc.contains(ABC.B)); 
        assertTrue(result.abc.contains(ABC.A)); 
    }

// com.fasterxml.jackson.databind.deser.merge.MapMerge1844Test::testMap1844
    public void testMap1844() throws Exception
    {
        final ObjectMapper mapper = newObjectMapper();
        mapper.setDefaultMergeable(true);

        final String f1 = aposToQuotes(
"{ 'key1' : {\n"
+"  '1': 1, '2': 2, '3': 3\n"
+"}, 'key2': {\n"
+"  '1': 1, '2': 2, '3': 3\n"
+"} }"
);
        final String f2 = aposToQuotes(
"{ 'key1' : {\n"
+"  '1': 2, '2': 3, '4': 5\n"
+"}, 'key2': {\n"
+"  '1': 2, '2': 3, '4': 5\n"
+"} }"
);
        TestMap1844 testMap = mapper.readerFor(TestMap1844.class).readValue(f1);
        testMap = mapper.readerForUpdating(testMap).readValue(f2);

        assertEquals(Integer.valueOf(2), testMap.getMapStringInteger().get("1"));
        assertEquals(Integer.valueOf(3), testMap.getMapStringInteger().get("2"));
        assertEquals(Integer.valueOf(3), testMap.getMapStringInteger().get("3"));
        assertEquals(Integer.valueOf(5), testMap.getMapStringInteger().get("4"));

        assertEquals(Integer.valueOf(2), testMap.getMapIntegerInteger().get(1));
        assertEquals(Integer.valueOf(3), testMap.getMapIntegerInteger().get(2));
        assertEquals(Integer.valueOf(3), testMap.getMapIntegerInteger().get(3));
        assertEquals(Integer.valueOf(5), testMap.getMapIntegerInteger().get(4));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testShallowMapMerging
    public void testShallowMapMerging() throws Exception
    {
        final String JSON = aposToQuotes("{'values':{'c':'y','d':null}}");
        MergedMap v = MAPPER.readValue(JSON, MergedMap.class);
        assertEquals(3, v.values.size());
        assertEquals("y", v.values.get("c"));
        assertEquals("x", v.values.get("a"));
        assertNull(v.values.get("d"));

        
        v = MAPPER_SKIP_NULLS.readValue(JSON, MergedMap.class);
        assertEquals(2, v.values.size());
        assertEquals("y", v.values.get("c"));
        assertEquals("x", v.values.get("a"));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testShallowNonStringMerging
    public void testShallowNonStringMerging() throws Exception
    {
        final String JSON = aposToQuotes("{'values':{'72':'b','666':null}}");
        MergedIntMap v = MAPPER.readValue(JSON , MergedIntMap.class);
        assertEquals(3, v.values.size());
        assertEquals("a", v.values.get(Integer.valueOf(13)));
        assertEquals("b", v.values.get(Integer.valueOf(72)));
        assertNull(v.values.get(Integer.valueOf(666)));

        v = MAPPER_SKIP_NULLS.readValue(JSON , MergedIntMap.class);
        assertEquals(2, v.values.size());
        assertEquals("a", v.values.get(Integer.valueOf(13)));
        assertEquals("b", v.values.get(Integer.valueOf(72)));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testDeeperMapMerging
    public void testDeeperMapMerging() throws Exception
    {
        
        MergedMap base = new MergedMap("name", "foobar");
        Map<String,Object> props = new LinkedHashMap<>();
        props.put("default", "yes");
        props.put("x", "abc");
        Map<String,Object> innerProps = new LinkedHashMap<>();
        innerProps.put("z", Integer.valueOf(13));
        props.put("extra", innerProps);
        base.values.put("props", props);

        
        MergedMap v = MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("{'values':{'props':{'x':'xyz','y' : '...','extra':{ 'ab' : true}}}}"));
        assertEquals(2, v.values.size());
        assertEquals("foobar", v.values.get("name"));
        assertNotNull(v.values.get("props"));
        props = (Map<String,Object>) v.values.get("props");
        assertEquals(4, props.size());
        assertEquals("yes", props.get("default"));
        assertEquals("xyz", props.get("x"));
        assertEquals("...", props.get("y"));
        assertNotNull(props.get("extra"));
        innerProps = (Map<String,Object>) props.get("extra");
        assertEquals(2, innerProps.size());
        assertEquals(Integer.valueOf(13), innerProps.get("z"));
        assertEquals(Boolean.TRUE, innerProps.get("ab"));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testMapMergingWithArray
    public void testMapMergingWithArray() throws Exception
    {
        
        MergedMap base = new MergedMap("name", "foobar");
        Map<String,Object> props = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        names.add("foo");
        props.put("names", names);
        base.values.put("props", props);
        props.put("extra", "misc");

        
        MergedMap v = MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("{'values':{'props':{'names': [ 'bar' ] }}}"));
        assertEquals(2, v.values.size());
        assertEquals("foobar", v.values.get("name"));
        assertNotNull(v.values.get("props"));
        props = (Map<String,Object>) v.values.get("props");
        assertEquals(2, props.size());
        assertEquals("misc", props.get("extra"));
        assertNotNull(props.get("names"));
        names = (List<String>) props.get("names");
        assertEquals(2, names.size());
        assertEquals("foo", names.get(0));
        assertEquals("bar", names.get(1));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testDefaultDeepMapMerge
    public void testDefaultDeepMapMerge() throws Exception
    {
        
        HashMap<String,Object> input = new HashMap<>();
        input.put("list", new ArrayList<>(Arrays.asList("a")));

        Map<?,?> resultMap = MAPPER.readerForUpdating(input)
                .readValue(aposToQuotes("{'list':['b']}"));

        List<?> resultList = (List<?>) resultMap.get("list");
        assertEquals(Arrays.asList("a", "b"), resultList);
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testDisabledMergeViaGlobal
    public void testDisabledMergeViaGlobal() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        
        mapper.setDefaultMergeable(false);

        HashMap<String,Object> input = new HashMap<>();
        input.put("list", new ArrayList<>(Arrays.asList("a")));

        Map<?,?> resultMap = mapper.readerForUpdating(input)
                .readValue(aposToQuotes("{'list':['b']}"));

        List<?> resultList = (List<?>) resultMap.get("list");

        assertEquals(Arrays.asList("b"), resultList);
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testDisabledMergeByType
    public void testDisabledMergeByType() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        
        mapper.configOverride(Object.class)
            .setMergeable(false);

        HashMap<String,Object> input = new HashMap<>();
        input.put("list", new ArrayList<>(Arrays.asList("a")));

        Map<?,?> resultMap = mapper.readerForUpdating(input)
                .readValue(aposToQuotes("{'list':['b']}"));
        List<?> resultList = (List<?>) resultMap.get("list");
        assertEquals(Arrays.asList("b"), resultList);

        
        

        mapper = newObjectMapper();
        mapper.setDefaultMergeable(false);
        mapper.configOverride(Object.class)
            .setMergeable(true);

        input = new HashMap<>();
        input.put("list", new ArrayList<>(Arrays.asList("x")));

        resultMap = mapper.readerForUpdating(input)
                .readValue(aposToQuotes("{'list':['y']}"));
        resultList = (List<?>) resultMap.get("list");
        assertEquals(Arrays.asList("x", "y"), resultList);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testBeanMergingWithNullDefault
    public void testBeanMergingWithNullDefault() throws Exception
    {
        
        ConfigDefault config = MAPPER.readerForUpdating(new ConfigDefault(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNull(config.loc);

        

        
        
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(AB.class)
            .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));
        config = mapper.readerForUpdating(new ConfigDefault(137, -3))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config.loc);
        assertEquals(137, config.loc.a);
        assertEquals(-3, config.loc.b);

        
        mapper = newObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));
        config = mapper.readerForUpdating(new ConfigDefault(12, 34))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config.loc);
        assertEquals(12, config.loc.a);
        assertEquals(34, config.loc.b);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testBeanMergingWithNullSkip
    public void testBeanMergingWithNullSkip() throws Exception
    {
        ConfigSkipNull config = MAPPER.readerForUpdating(new ConfigSkipNull(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNotNull(config.loc);
        assertEquals(5, config.loc.a);
        assertEquals(7, config.loc.b);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testBeanMergingWithNullSet
    public void testBeanMergingWithNullSet() throws Exception
    {
        ConfigAllowNullOverwrite config = MAPPER.readerForUpdating(new ConfigAllowNullOverwrite(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNull(config.loc);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testSetterlessMergingWithNull
    public void testSetterlessMergingWithNull() throws Exception
    {
        NoSetterConfig input = new NoSetterConfig();
        NoSetterConfig result = MAPPER.readerForUpdating(input)
                .readValue(aposToQuotes("{'value':null}"));
        assertNotNull(result.getValue());
        assertEquals(2, result.getValue().a);
        assertEquals(3, result.getValue().b);
        assertSame(input, result);
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testObjectNodeUpdateValue
    public void testObjectNodeUpdateValue() throws Exception
    {
        ObjectNode base = MAPPER.createObjectNode();
        base.put("first", "foo");
        assertSame(base,
                MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("{'second':'bar', 'third':5, 'fourth':true}")));
        assertEquals(4, base.size());
        assertEquals("bar", base.path("second").asText());
        assertEquals("foo", base.path("first").asText());
        assertEquals(5, base.path("third").asInt());
        assertTrue(base.path("fourth").asBoolean());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testObjectNodeMerge
    public void testObjectNodeMerge() throws Exception
    {
        ObjectNodeWrapper w = MAPPER.readValue(aposToQuotes("{'props':{'stuff':'xyz'}}"),
                ObjectNodeWrapper.class);
        assertEquals(2, w.props.size());
        assertEquals("enabled", w.props.path("default").asText());
        assertEquals("xyz", w.props.path("stuff").asText());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testObjectDeepUpdate
    public void testObjectDeepUpdate() throws Exception
    {
        ObjectNode base = MAPPER.createObjectNode();
        ObjectNode props = base.putObject("props");
        props.put("base", 123);
        props.put("value", 456);
        ArrayNode a = props.putArray("array");
        a.add(true);
        base.putNull("misc");
        assertSame(base,
                MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes(
                        "{'props':{'value':true, 'extra':25.5, 'array' : [ 3 ]}}")));
        assertEquals(2, base.size());
        ObjectNode resultProps = (ObjectNode) base.get("props");
        assertEquals(4, resultProps.size());
        
        assertEquals(123, resultProps.path("base").asInt());
        assertTrue(resultProps.path("value").asBoolean());
        assertEquals(25.5, resultProps.path("extra").asDouble());
        JsonNode n = resultProps.get("array");
        assertEquals(ArrayNode.class, n.getClass());
        assertEquals(2, n.size());
        assertEquals(3, n.get(1).asInt());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testArrayNodeUpdateValue
    public void testArrayNodeUpdateValue() throws Exception
    {
        ArrayNode base = MAPPER.createArrayNode();
        base.add("first");
        assertSame(base,
                MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("['second',false,null]")));
        assertEquals(4, base.size());
        assertEquals("first", base.path(0).asText());
        assertEquals("second", base.path(1).asText());
        assertFalse(base.path(2).asBoolean());
        assertTrue(base.path(3).isNull());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testArrayNodeMerge
    public void testArrayNodeMerge() throws Exception
    {
        ArrayNodeWrapper w = MAPPER.readValue(aposToQuotes("{'list':[456,true,{},  [], 'foo']}"),
                ArrayNodeWrapper.class);
        assertEquals(6, w.list.size());
        assertEquals(123, w.list.get(0).asInt());
        assertEquals(456, w.list.get(1).asInt());
        assertTrue(w.list.get(2).asBoolean());
        JsonNode n = w.list.get(3);
        assertTrue(n.isObject());
        assertEquals(0, n.size());
        n = w.list.get(4);
        assertTrue(n.isArray());
        assertEquals(0, n.size());
        assertEquals("foo", w.list.get(5).asText());
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingViaProp
    public void testBeanMergingViaProp() throws Exception
    {
        Config config = MAPPER.readValue(aposToQuotes("{'loc':{'b':3}}"), Config.class);
        assertEquals(1, config.loc.a);
        assertEquals(3, config.loc.b);

        config = MAPPER.readerForUpdating(new Config(5, 7))
                .readValue(aposToQuotes("{'loc':{'b':2}}"));
        assertEquals(5, config.loc.a);
        assertEquals(2, config.loc.b);
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingViaType
    public void testBeanMergingViaType() throws Exception
    {
        
        NonMergeConfig config = MAPPER.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(0, config.loc.b); 

        
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(AB.class).setMergeable(true);
        config = mapper.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(2, config.loc.b); 
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingViaGlobal
    public void testBeanMergingViaGlobal() throws Exception
    {
        
        ObjectMapper mapper = newObjectMapper()
                .setDefaultMergeable(true);
        NonMergeConfig config = mapper.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(2, config.loc.b); 

        
        FiveMinuteUser user0 = new FiveMinuteUser("Bob", "Bush", true, FiveMinuteUser.Gender.MALE,
                new byte[] { 1, 2, 3, 4, 5 });
        FiveMinuteUser user = mapper.readerFor(FiveMinuteUser.class)
                .withValueToUpdate(user0)
                .readValue(aposToQuotes("{'name':{'last':'Brown'}}"));
        assertEquals("Bob", user.getName().getFirst());
        assertEquals("Brown", user.getName().getLast());
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingWithoutSetter
    public void testBeanMergingWithoutSetter() throws Exception
    {
        NoSetterConfig config = MAPPER.readValue(aposToQuotes("{'value':{'b':99}}"),
                NoSetterConfig.class);
        assertEquals(99, config._value.b);
        assertEquals(1, config._value.a);
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanAsArrayMerging
    public void testBeanAsArrayMerging() throws Exception
    {
        ABAsArray input = new ABAsArray();
        input.a = 4;
        input.b = 6;

        assertSame(input, MAPPER.readerForUpdating(input)
                .readValue("[1, 3]"));
        assertEquals(1, input.a);
        assertEquals(3, input.b);

        
        assertSame(input, MAPPER.readerForUpdating(input)
                .readValue("[9]"));
        assertEquals(9, input.a);
        assertEquals(3, input.b);

        
        try {
            MAPPER.readerForUpdating(input)
                .readValue("[9, 8, 14]");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "expected at most 2 properties");
        }

        try {
            MAPPER.readerForUpdating(input)
                .readValue("\"blob\"");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize");
            verifyException(e, "from non-Array representation");
        }
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testReferenceMerging
    public void testReferenceMerging() throws Exception
    {
        MergedReference result = MAPPER.readValue(aposToQuotes("{'value':'override'}"),
                MergedReference.class);
        assertEquals("override", result.value.get());
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testInvalidPropertyMerge
    public void testInvalidPropertyMerge() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
                .disable(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE);
        
        try {
            mapper.readValue("{\"value\":3}", CantMergeInts.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "cannot be merged");
        }
    }

// com.fasterxml.jackson.databind.deser.merge.UpdateValueTest::testValueUpdateWithCreator
    public void testValueUpdateWithCreator() throws Exception
    {
        Bean bean = new Bean("abc", "def");
        MAPPER.readerFor(Bean.class).withValueToUpdate(bean).readValue("{\"a\":\"ghi\",\"b\":\"jkl\"}");
        assertEquals("ghi", bean.getA());
        assertEquals("jkl", bean.getB());
    }

// com.fasterxml.jackson.databind.deser.merge.UpdateValueTest::testValueUpdateOther
    public void testValueUpdateOther() throws Exception
    {
        Bean bean = new Bean("abc", "def");
        ObjectReader r = MAPPER.readerFor(Bean.class).withValueToUpdate(bean);
        
        r = r.withValueToUpdate(null);
        
        Bean result = r.readValue(aposToQuotes("{'a':'x'}"));
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.exc.BasicExceptionTest::testBadDefinition
    public void testBadDefinition() throws Exception
    {
        JavaType t = TypeFactory.defaultInstance().constructType(String.class);
        JsonParser p = JSON_F.createParser("[]");
        InvalidDefinitionException e = new InvalidDefinitionException(p,
               "Testing", t);
        assertEquals("Testing", e.getOriginalMessage());
        assertEquals(String.class, e.getType().getRawClass());
        assertNull(e.getBeanDescription());
        assertNull(e.getProperty());
        assertSame(p, e.getProcessor());
        p.close();

        
        BeanDescription beanDef = MAPPER.getSerializationConfig().introspectClassAnnotations(getClass());
        e = InvalidDefinitionException.from(p, "Testing",
                beanDef, (BeanPropertyDefinition) null);
        assertEquals(beanDef.getType(), e.getType());
        assertNotNull(e);
        
        
        JsonGenerator g = JSON_F.createGenerator(new StringWriter());
        e = new InvalidDefinitionException(p,
                "Testing", t);
        assertEquals("Testing", e.getOriginalMessage());
        assertEquals(String.class, e.getType().getRawClass());

        
        e = InvalidDefinitionException.from(g, "Testing",
                beanDef, (BeanPropertyDefinition) null);
        assertEquals(beanDef.getType(), e.getType());
        assertNotNull(e);
        
        g.close();
    }

// com.fasterxml.jackson.databind.exc.BasicExceptionTest::testInvalidFormat
    public void testInvalidFormat() throws Exception
    {
        
        InvalidFormatException e = new InvalidFormatException("Testing", Boolean.TRUE,
                String.class);
        assertSame(Boolean.TRUE, e.getValue());
        assertNull(e.getProcessor());
        assertNotNull(e);

        e = new InvalidFormatException("Testing", JsonLocation.NA,
                Boolean.TRUE, String.class);
        assertSame(Boolean.TRUE, e.getValue());
        assertNull(e.getProcessor());
        assertNotNull(e);
    }

// com.fasterxml.jackson.databind.exc.BasicExceptionTest::testIgnoredProperty
    public void testIgnoredProperty() throws Exception
    {
        
        JsonParser p = JSON_F.createParser("{ }");
        IgnoredPropertyException e = IgnoredPropertyException.from(p,
                this, 
                "testProp", Collections.<Object>singletonList("x"));
        assertNotNull(e);

        e = IgnoredPropertyException.from(p,
                getClass(),
                "testProp", null);
        assertNotNull(e);
        assertNull(e.getKnownPropertyIds());
        p.close();

        
        try {
            IgnoredPropertyException.from(p, null,
                    "testProp", Collections.<Object>singletonList("x"));
            fail("Should not pass");
        } catch (NullPointerException e2) {
        }
    }

// com.fasterxml.jackson.databind.exc.BasicExceptionTest::testUnrecognizedProperty
    public void testUnrecognizedProperty() throws Exception
    {
        JsonParser p = JSON_F.createParser("{ }");
        UnrecognizedPropertyException e = UnrecognizedPropertyException.from(p, this,
                "testProp", Collections.<Object>singletonList("y"));
        assertNotNull(e);
        assertEquals(getClass(), e.getReferringClass());
        Collection<Object> ids = e.getKnownPropertyIds();
        assertNotNull(ids);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("y"));

        e = UnrecognizedPropertyException.from(p, getClass(),
                "testProp", Collections.<Object>singletonList("y"));

        assertEquals(getClass(), e.getReferringClass());
        p.close();
    }

// com.fasterxml.jackson.databind.exc.BasicExceptionTest::testLocationAddition
    public void testLocationAddition() throws Exception
    {
        try {
             MAPPER.readValue("{\"value\":\"foo\"}",
                    new TypeReference<Map<ABC, Integer>>() { });
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            String msg = e.getMessage();
            String[] str = msg.split(" at \\[");
            if (str.length != 2) {
                fail("Should only get one 'at [' marker, got "+(str.length-1)+", source: "+msg);
            }
        }
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testHandlingOfUnrecognized
    public void testHandlingOfUnrecognized() throws Exception
    {
        UnrecognizedPropertyException exc = null;
        try {
            MAPPER.readValue("{\"bar\":3}", Bean.class);
        } catch (UnrecognizedPropertyException e) {
            exc = e;
        }
        if (exc == null) {
            fail("Should have failed binding");
        }
        assertEquals("bar", exc.getPropertyName());
        assertEquals(Bean.class, exc.getReferringClass());
        
        verifyException(exc, "propX");
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionWithEmpty
    public void testExceptionWithEmpty() throws Exception
    {
        try {
            Object result = MAPPER.readValue("    ", Object.class);
            fail("Expected an exception, but got result value: "+result);
        } catch (Exception e) {
            verifyException(e, MismatchedInputException.class, "No content");
        }
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionWithIncomplete
    public void testExceptionWithIncomplete()
        throws Exception
    {
        BrokenStringReader r = new BrokenStringReader("[ 1, ", "TEST");
        JsonParser p = MAPPER.getFactory().createParser(r);
        try {
            @SuppressWarnings("unused")
            Object ob = MAPPER.readValue(p, Object.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionWithEOF
    public void testExceptionWithEOF() throws Exception
    {
        JsonParser p = MAPPER.getFactory().createParser("  3");

        Integer I = MAPPER.readValue(p, Integer.class);
        assertEquals(3, I.intValue());

        
        try {
            I = MAPPER.readValue(p, Integer.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, MismatchedInputException.class, "No content");
        }
        
        JsonToken t = p.getCurrentToken();
        if (t != null) {
            fail("Expected current token to be null after end-of-stream, was: "+t);
        }
        p.close();
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionForNoCreators
    public void testExceptionForNoCreators() throws Exception
    {
        try {
            NoCreatorsBean b = MAPPER.readValue("{}", NoCreatorsBean.class);
            fail("Should not succeed, got: "+b);
        } catch (JsonMappingException e) {
            verifyException(e, InvalidDefinitionException.class, "no Creators");
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testIOException
    public void testIOException() throws IOException
    {
        IOException ioe = new IOException("TEST");
        String json = MAPPER.writeValueAsString(ioe);
        IOException result = MAPPER.readValue(json, IOException.class);
        assertEquals(ioe.getMessage(), result.getMessage());
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testWithCreator
    public void testWithCreator() throws IOException
    {
        final String MSG = "the message";
        String json = MAPPER.writeValueAsString(new MyException(MSG, 3));

        MyException result = MAPPER.readValue(json, MyException.class);
        assertEquals(MSG, result.getMessage());
        assertEquals(3, result.value);
        assertEquals(1, result.stuff.size());
        assertEquals(result.getFoo(), result.stuff.get("foo"));
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testWithNullMessage
    public void testWithNullMessage() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(new IOException((String) null));
        IOException result = mapper.readValue(json, IOException.class);
        assertNotNull(result);
        assertNull(result.getMessage());
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testNoArgsException
    public void testNoArgsException() throws IOException
    {
        MyNoArgException exc = MAPPER.readValue("{}", MyNoArgException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testJDK7SuppressionProperty
    public void testJDK7SuppressionProperty() throws IOException
    {
        Exception exc = MAPPER.readValue("{\"suppressed\":[]}", IOException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testSingleValueArrayDeserialization
    public void testSingleValueArrayDeserialization() {}

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testSingleValueArrayDeserializationException
    public void testSingleValueArrayDeserializationException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        final IOException exp;
        try {
            throw new IOException("testing");
        } catch (IOException internal) {
            exp = internal;
        }
        final String value = "[" + mapper.writeValueAsString(exp) + "]";
        
        try {
            mapper.readValue(value, IOException.class);
            fail("Exception not thrown when attempting to deserialize an IOException wrapped in a single value array with UNWRAP_SINGLE_VALUE_ARRAYS disabled");
        } catch (JsonMappingException exp2) {
            verifyException(exp2, "out of START_ARRAY");
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testLineNumberAsString
    public void testLineNumberAsString() throws IOException
    {
        Exception exc = MAPPER.readValue(aposToQuotes(
                "{'message':'Test',\n'stackTrace': "
                +"[ { 'lineNumber':'50' } ] }"
        ), IOException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testNullAsMessage
    public void testNullAsMessage() throws IOException
    {
        Exception exc = MAPPER.readValue(aposToQuotes(
                "{'message':null, 'localizedMessage':null }"
        ), IOException.class);
        assertNotNull(exc);
        assertNull(exc.getMessage());
        assertNull(exc.getLocalizedMessage());
    }

// com.fasterxml.jackson.databind.exc.ExceptionPathTest::testReferenceChainForInnerClass
    public void testReferenceChainForInnerClass() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Outer());
        try {
            MAPPER.readValue(json, Outer.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            JsonMappingException.Reference reference = e.getPath().get(0);
            assertEquals(getClass().getName()+"$Outer[\"inner\"]",
                    reference.toString());
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testSimple
    public void testSimple() throws Exception
    {
        String TEST = "test exception";
        Map<String,Object> result = writeAndMap(MAPPER, new Exception(TEST));
        
        Object ob = result.get("suppressed");
        if (ob != null) {
            assertEquals(5, result.size());
        } else {
            assertEquals(4, result.size());
        }

        assertEquals(TEST, result.get("message"));
        assertNull(result.get("cause"));
        assertEquals(TEST, result.get("localizedMessage"));

        
        Object traces = result.get("stackTrace");
        if (!(traces instanceof List<?>)) {
            fail("Expected a List for exception member 'stackTrace', got: "+traces);
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testSimpleOther
    public void testSimpleOther() throws Exception
    {
        JsonParser p = MAPPER.getFactory().createParser("{ }");
        InvalidFormatException exc = InvalidFormatException.from(p, "Test", getClass(), String.class);
        String json = MAPPER.writeValueAsString(exc);
        p.close();
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testIgnorals
    public void testIgnorals() throws Exception
    {
        ExceptionWithIgnoral input = new ExceptionWithIgnoral("foobar");
        input.initCause(new IOException("surprise!"));

        
        String json = MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(input);

        Map<String,Object> result = MAPPER.readValue(json, Map.class);
        assertEquals("foobar", result.get("message"));

        assertNull(result.get("bogus1"));
        assertNotNull(result.get("bogus2"));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(ExceptionWithIgnoral.class)
            .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("bogus2"));
        String json2 = mapper
                .writeValueAsString(new ExceptionWithIgnoral("foobar"));

        Map<String,Object> result2 = mapper.readValue(json2, Map.class);
        assertNull(result2.get("bogus1"));
        assertNull(result2.get("bogus2"));

        
        ExceptionWithIgnoral output = mapper.readValue(json2, ExceptionWithIgnoral.class);
        assertNotNull(output);
        assertEquals("foobar", output.getMessage());
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testJsonMappingExceptionSerialization
    public void testJsonMappingExceptionSerialization() throws IOException {
        Exception e = null;
        
        try {
            MAPPER.readValue( "{ \"val\": \"foo\" }", NoSerdeConstructor.class );
            fail("Should not pass");
        } catch (JsonMappingException e0) {
            verifyException(e0, "cannot deserialize from Object");
            e = e0;
        }
        
        String json = MAPPER.writeValueAsString(e);
        JsonNode root = MAPPER.readTree(json);
        String msg = root.path("message").asText();
        String MATCH = "cannot construct instance";
        if (!msg.toLowerCase().contains(MATCH)) {
            fail("Exception should contain '"+MATCH+"', does not: '"+msg+"'");
        }
    }

// com.fasterxml.jackson.databind.exc.StackTraceElementTest::testCustomStackTraceDeser
    public void testCustomStackTraceDeser() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        String json = mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(new ErrorObject(new Exception("exception message")));

        ErrorObject result = mapper.readValue(json, ErrorObject.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.exc.TestExceptionHandlingWithDefaultDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";
        final String THIS = getClass().getName();

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals(THIS+"$Foo[\"bar\"]->"+THIS+"$Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionHandlingWithJsonCreatorDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";
        final String THIS = getClass().getName();

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals(THIS+"$Foo[\"bar\"]->"+THIS+"$Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionsDuringWriting::testCatchAndRethrow
    public void testCatchAndRethrow()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test-exceptions", Version.unknownVersion());
        module.addSerializer(Bean.class, new SerializerWithErrors());
        mapper.registerModule(module);
        try {
            StringWriter sw = new StringWriter();
            
            Bean[] b = { new Bean() };
            List<Bean[]> l = new ArrayList<Bean[]>();
            l.add(b);
            mapper.writeValue(sw, l);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            
            verifyException(e, "test string");
            Throwable root = e.getCause();
            assertNotNull(root);

            if (!(root instanceof IllegalArgumentException)) {
                fail("Wrapped exception not IAE, but "+root.getClass());
            }
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionsDuringWriting::testExceptionWithSimpleMapper
    public void testExceptionWithSimpleMapper()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            BrokenStringWriter sw = new BrokenStringWriter("TEST");
            mapper.writeValue(sw, createLongObject());
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionsDuringWriting::testExceptionWithMapperAndGenerator
    public void testExceptionWithMapperAndGenerator()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory f = new MappingJsonFactory();
        BrokenStringWriter sw = new BrokenStringWriter("TEST");
        JsonGenerator jg = f.createGenerator(sw);

        try {
            mapper.writeValue(jg, createLongObject());
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionsDuringWriting::testExceptionWithGeneratorMapping
    public void testExceptionWithGeneratorMapping()
        throws Exception
    {
        JsonFactory f = new MappingJsonFactory();
        JsonGenerator jg = f.createGenerator(new BrokenStringWriter("TEST"));
        try {
            jg.writeObject(createLongObject());
            fail("Should have gotten an exception");
        } catch (Exception e) {
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testQNameSer
    public void testQNameSer() throws Exception
    {
        QName qn = new QName("http://abc", "tag", "prefix");
        assertEquals(quote(qn.toString()), serializeAsString(qn));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDurationSer
    public void testDurationSer() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        
        Duration dur = dtf.newDurationDayTime(false, 15, 19, 58, 1);
        assertEquals(quote(dur.toString()), serializeAsString(dur));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testXMLGregorianCalendarSerAndDeser
    public void testXMLGregorianCalendarSerAndDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        XMLGregorianCalendar cal = dtf.newXMLGregorianCalendar
            (1974, 10, 10, 18, 15, 17, 123, 0);
        
        ObjectMapper mapper = new ObjectMapper();
        long timestamp = cal.toGregorianCalendar().getTimeInMillis();
        String numStr = String.valueOf(timestamp);
        assertEquals(numStr, mapper.writeValueAsString(cal));

        
        XMLGregorianCalendar calOut = mapper.readValue(numStr, XMLGregorianCalendar.class);
        assertNotNull(calOut);
        assertEquals(timestamp, calOut.toGregorianCalendar().getTimeInMillis());

        
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        String exp = cal.toXMLFormat();
        String act = mapper.writeValueAsString(cal);
        act = act.substring(1, act.length() - 1); 
        exp = removeZ(exp);
        act = removeZ(act);
        assertEquals(exp, act);
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDeserializerLoading
    public void testDeserializerLoading()
    {
        CoreXMLDeserializers sers = new CoreXMLDeserializers();
        TypeFactory f = TypeFactory.defaultInstance();
        sers.findBeanDeserializer(f.constructType(Duration.class), null, null);
        sers.findBeanDeserializer(f.constructType(XMLGregorianCalendar.class), null, null);
        sers.findBeanDeserializer(f.constructType(QName.class), null, null);
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testQNameDeser
    public void testQNameDeser() throws Exception
    {
        QName qn = new QName("http://abc", "tag", "prefix");
        String qstr = qn.toString();
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("Should deserialize to equal QName (exp serialization: '"+qstr+"')",
                     qn, mapper.readValue(quote(qstr), QName.class));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testCalendarDeser
    public void testCalendarDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        XMLGregorianCalendar cal = dtf.newXMLGregorianCalendar
            (1974, 10, 10, 18, 15, 17, 123, 0);
        String exp = cal.toXMLFormat();
        assertEquals("Should deserialize to equal XMLGregorianCalendar ('"+exp+"')", cal,
                new ObjectMapper().readValue(quote(exp), XMLGregorianCalendar.class));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDurationDeser
    public void testDurationDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        
        Duration dur = dtf.newDurationDayTime(true, 27, 5, 15, 59);
        String exp = dur.toString();
        assertEquals("Should deserialize to equal Duration ('"+exp+"')", dur,
                new ObjectMapper().readValue(quote(exp), Duration.class));
    }

// com.fasterxml.jackson.databind.ext.TestDOM::testSerializeSimpleNonNS
    public void testSerializeSimpleNonNS() throws Exception
    {
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse
            (new InputSource(new StringReader(SIMPLE_XML)));
        assertNotNull(doc);
        
        String outputRaw = MAPPER.writeValueAsString(doc);
        
        String output = MAPPER.readValue(outputRaw, String.class);
        
        assertEquals(SIMPLE_XML, normalizeOutput(output));
    }

// com.fasterxml.jackson.databind.ext.TestDOM::testDeserializeNonNS
    public void testDeserializeNonNS() throws Exception
    {
        for (int i = 0; i < 2; ++i) {
            Document doc;

            if (i == 0) {
                
                doc = MAPPER.readValue(quote(SIMPLE_XML), Document.class);
            } else {
                
                Node node = MAPPER.readValue(quote(SIMPLE_XML), Node.class);
                doc = (Document) node;
            }
            Element root = doc.getDocumentElement();
            assertNotNull(root);
            
            assertEquals("root", root.getTagName());
            assertEquals("3", root.getAttribute("attr"));
            assertEquals(1, root.getAttributes().getLength());
            NodeList nodes = root.getChildNodes();
            assertEquals(2, nodes.getLength());
            Element leaf = (Element) nodes.item(0);
            assertEquals("leaf", leaf.getTagName());
            assertEquals(0, leaf.getAttributes().getLength());
            
            ProcessingInstruction pi = (ProcessingInstruction) nodes.item(1);
            assertEquals("proc", pi.getTarget());
            assertEquals("instr", pi.getData());
        }
    }

// com.fasterxml.jackson.databind.ext.TestDOM::testDeserializeNS
    public void testDeserializeNS() throws Exception
    {
        Document doc = MAPPER.readValue(quote(SIMPLE_XML_NS), Document.class);
        Element root = doc.getDocumentElement();
        assertNotNull(root);
        assertEquals("root", root.getTagName());
        
        String uri = root.getNamespaceURI();
        assertTrue((uri == null) || "".equals(uri));
        
        assertEquals(0, root.getChildNodes().getLength());
        
        assertEquals(2, root.getAttributes().getLength());
        assertEquals("abc", root.getAttributeNS("http://foo", "attr"));
    }

// com.fasterxml.jackson.databind.ext.TestJava6Types::test16Types
    public void test16Types() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        Deque<?> dq = mapper.readValue("[1]", Deque.class);
        assertNotNull(dq);
        assertEquals(1, dq.size());
        assertTrue(dq instanceof Deque<?>);

        NavigableSet<?> ns = mapper.readValue("[ true ]", NavigableSet.class);
        assertEquals(1, ns.size());
        assertTrue(ns instanceof NavigableSet<?>);
    }

// com.fasterxml.jackson.databind.ext.TestJava7Types::testPathRoundtrip
    public void testPathRoundtrip() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        Path input = Paths.get("/tmp", "foo.txt");

        String json = mapper.writeValueAsString(input);
        assertNotNull(json);

        Path p = mapper.readValue(json, Path.class);
        assertNotNull(p);
        
        assertEquals(input.toUri(), p.toUri());
        assertEquals(input, p);
    }

// com.fasterxml.jackson.databind.ext.TestJava7Types::testPolymorphicPath
    public void testPolymorphicPath() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        Path input = Paths.get("/tmp", "foo.txt");

        String json = mapper.writeValueAsString(new Object[] { input });

        Object[] obs = mapper.readValue(json, Object[].class);
        assertEquals(1, obs.length);
        Object ob = obs[0];
        assertTrue(ob instanceof Path);

        assertEquals(input.toString(), ob.toString());
    }

// com.fasterxml.jackson.databind.filter.ProblemHandler1767Test::testPrimitivePropertyWithHandler
    public void testPrimitivePropertyWithHandler() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new IntHandler());
        TestBean result = mapper.readValue(aposToQuotes("{'a': 'not-a-number'}"), TestBean.class);
        assertNotNull(result);
        assertEquals(1, result.a);
    }

// com.fasterxml.jackson.databind.format.BooleanFormatTest::testShapeViaDefaults
    public void testShapeViaDefaults() throws Exception
    {
        assertEquals(aposToQuotes("{'b':true}"),
                MAPPER.writeValueAsString(new BooleanWrapper(true)));
        ObjectMapper m = newObjectMapper();
        m.configOverride(Boolean.class)
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER));
        assertEquals(aposToQuotes("{'b':1}"),
                m.writeValueAsString(new BooleanWrapper(true)));
    }

// com.fasterxml.jackson.databind.format.BooleanFormatTest::testShapeOnProperty
    public void testShapeOnProperty() throws Exception
    {
        assertEquals(aposToQuotes("{'b1':1,'b2':0,'b3':true}"),
                MAPPER.writeValueAsString(new BeanWithBoolean(true, false, true)));
    }

// com.fasterxml.jackson.databind.format.CollectionFormatShapeTest::testListAsObjectRoundtrip
    public void testListAsObjectRoundtrip() throws Exception
    {
        
        CollectionAsPOJO list = new CollectionAsPOJO();
        list.add("a");
        list.add("b");
        String json = MAPPER.writeValueAsString(list);
        assertEquals("{\"size\":2,\"values\":[\"a\",\"b\"]}", json);

        
        CollectionAsPOJO result = MAPPER.readValue(json, CollectionAsPOJO.class);
        assertEquals(2, result.size());
    }
