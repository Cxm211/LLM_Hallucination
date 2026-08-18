// buggy code
    public Object deserializeSetAndReturn(JsonParser p,
    		DeserializationContext ctxt, Object instance) throws IOException
    {
        Object id = _valueDeserializer.deserialize(p, ctxt);
        /* 02-Apr-2015, tatu: Actually, as per [databind#742], let it be;
         *  missing or null id is needed for some cases, such as cases where id
         *  will be generated externally, at a later point, and is not available
         *  quite yet. Typical use case is with DB inserts.
         */
        // note: no null checks (unlike usually); deserializer should fail if one found
        if (id == null) {
            return null;
        }
        ReadableObjectId roid = ctxt.findObjectId(id, _objectIdReader.generator, _objectIdReader.resolver);
        roid.bindItem(instance);
        // also: may need to set a property value as well
        SettableBeanProperty idProp = _objectIdReader.idProperty;
        if (idProp != null) {
            return idProp.setAndReturn(instance, id);
        }
        return instance;
    }

// relevant test
// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testISO8601PartialMilliseconds
    public void testISO8601PartialMilliseconds() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        
        inputStr = "2014-10-03T18:00:00.6-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(600, c.get(Calendar.MILLISECOND));

        inputStr = "2014-10-03T18:00:00.61-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(18 + 5, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(610, c.get(Calendar.MILLISECOND));

        inputStr = "1997-07-16T19:20:30.45+01:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20:30.45+0100";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20:30.45+01";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testISO8601MissingSeconds
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601NoTimezone
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601NoMilliseconds
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601JustDate
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateSql
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCalendar
    public void testCalendar() throws Exception
    {
        
        java.util.Calendar value = Calendar.getInstance();
        long l = 12345678L;
        value.setTimeInMillis(l);

        
        Calendar result = MAPPER.readValue(""+l, Calendar.class);
        assertEquals(l, result.getTimeInMillis());

        
        String dateStr = dateToString(new Date(l));
        result = MAPPER.readValue(quote(dateStr), Calendar.class);

        
        assertEquals(l, result.getTimeInMillis());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustom
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDatesWithEmptyStrings
    public void testDatesWithEmptyStrings() throws Exception
    {
        assertNull(MAPPER.readValue(quote(""), java.util.Date.class));
        assertNull(MAPPER.readValue(quote(""), java.util.Calendar.class));
        assertNull(MAPPER.readValue(quote(""), java.sql.Date.class));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::test8601DateTimeNoMilliSecs
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testTimeZone
    public void testTimeZone() throws Exception
    {
        TimeZone result = MAPPER.readValue(quote("PST"), TimeZone.class);
        assertEquals("PST", result.getID());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustomDateWithAnnotation
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustomCalendarWithAnnotation
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustomCalendarWithTimeZone
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

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testISO8601Directly
    public void testISO8601Directly() throws Exception
    {
        final String TIME_STR = "2015-01-21T08:56:13.533+0000";
        Date d = MAPPER.readValue(quote(TIME_STR), Date.class);
        assertNotNull(d);

        ISO8601DateFormat f = new ISO8601DateFormat();
        Date d2 = f.parse(TIME_STR);
        assertNotNull(d2);
        assertEquals(d.getTime(), d2.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testInvalidFormat
    public void testInvalidFormat() throws Exception
    {
        try {
            MAPPER.readValue(quote("foobar"), Date.class);
            fail("Should have failed with an exception");
        } catch (InvalidFormatException e) {
            verifyException(e, "Can not construct instance");
            assertEquals("foobar", e.getValue());
            assertEquals(Date.class, e.getTargetType());
        } catch (Exception e) {
            fail("Wrong type of exception ("+e.getClass().getName()+"), should get "
                    +InvalidFormatException.class.getName());
        }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testSimple
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
        } catch (JsonMappingException jex) {
            verifyException(jex, "value not one of declared");
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testComplexEnum
    public void testComplexEnum() throws Exception
    {
        String json = MAPPER.writeValueAsString(TimeUnit.SECONDS);
        assertEquals(quote("SECONDS"), json);
        TimeUnit result = MAPPER.readValue(json, TimeUnit.class);
        assertSame(TimeUnit.SECONDS, result);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testAnnotated
    public void testAnnotated() throws Exception
    {
        AnnotatedTestEnum e = MAPPER.readValue("\"JACKSON\"", AnnotatedTestEnum.class);
        
        assertEquals(AnnotatedTestEnum.OK, e);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumMaps
    public void testEnumMaps() throws Exception
    {
        EnumMap<TestEnum,String> value = MAPPER.readValue("{\"OK\":\"value\"}",
                new TypeReference<EnumMap<TestEnum,String>>() { });
        assertEquals("value", value.get(TestEnum.OK));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testSubclassedEnums
    public void testSubclassedEnums() throws Exception
    {
        EnumWithSubClass value = MAPPER.readValue("\"A\"", EnumWithSubClass.class);
        assertEquals(EnumWithSubClass.A, value);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testCreatorEnums
    public void testCreatorEnums() throws Exception {
        EnumWithCreator value = MAPPER.readValue("\"enumA\"", EnumWithCreator.class);
        assertEquals(EnumWithCreator.A, value);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testCreatorEnumsFromBigDecimal
    public void testCreatorEnumsFromBigDecimal() throws Exception {
        EnumWithBDCreator value = MAPPER.readValue("\"8.0\"", EnumWithBDCreator.class);
        assertEquals(EnumWithBDCreator.E8, value);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testToStringEnums
    public void testToStringEnums() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        LowerCaseEnum value = m.readValue("\"c\"", LowerCaseEnum.class);
        assertEquals(LowerCaseEnum.C, value);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testToStringEnumMaps
    public void testToStringEnumMaps() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        EnumMap<LowerCaseEnum,String> value = m.readValue("{\"a\":\"value\"}",
                new TypeReference<EnumMap<LowerCaseEnum,String>>() { });
        assertEquals("value", value.get(LowerCaseEnum.A));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testNumbersToEnums
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
        } catch (JsonMappingException e) {
            verifyException(e, "Not allowed to deserialize Enum value out of JSON number");
        }

        
        try {
            value = r.readValue(quote("1"));
            fail("Expected an error");
        } catch (JsonMappingException e) {
            verifyException(e, "Not allowed to deserialize Enum value out of JSON number");
        }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumsWithIndex
    public void testEnumsWithIndex() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        String json = m.writeValueAsString(TestEnum.RULES);
        assertEquals(String.valueOf(TestEnum.RULES.ordinal()), json);
        TestEnum result = m.readValue(json, TestEnum.class);
        assertSame(TestEnum.RULES, result);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumsWithJsonValue
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

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumWithCreatorEnumMaps
    public void testEnumWithCreatorEnumMaps() throws Exception {
          EnumMap<EnumWithCreator,String> value = MAPPER.readValue("{\"enumA\":\"value\"}",
                  new TypeReference<EnumMap<EnumWithCreator,String>>() {});
          assertEquals("value", value.get(EnumWithCreator.A));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumWithCreatorMaps
    public void testEnumWithCreatorMaps() throws Exception {
          java.util.HashMap<EnumWithCreator,String> value = MAPPER.readValue("{\"enumA\":\"value\"}",
                  new TypeReference<java.util.HashMap<EnumWithCreator,String>>() {});
          assertEquals("value", value.get(EnumWithCreator.A));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumWithCreatorEnumSets
    public void testEnumWithCreatorEnumSets() throws Exception {
          EnumSet<EnumWithCreator> value = MAPPER.readValue("[\"enumA\"]",
                  new TypeReference<EnumSet<EnumWithCreator>>() {});
          assertTrue(value.contains(EnumWithCreator.A));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testAllowUnknownEnumValuesReadAsNull
    public void testAllowUnknownEnumValuesReadAsNull() throws Exception
    {
        
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        assertNull(reader.forType(TestEnum.class).readValue("\"NO-SUCH-VALUE\""));
        assertNull(reader.forType(TestEnum.class).readValue(" 4343 "));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testAllowUnknownEnumValuesForEnumSets
    public void testAllowUnknownEnumValuesForEnumSets() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        EnumSet<TestEnum> result = reader.forType(new TypeReference<EnumSet<TestEnum>>() { })
                .readValue("[\"NO-SUCH-VALUE\"]");
        assertEquals(0, result.size());
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testAllowUnknownEnumValuesAsMapKeysReadAsNull
    public void testAllowUnknownEnumValuesAsMapKeysReadAsNull() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        ClassWithEnumMapKey result = reader.forType(ClassWithEnumMapKey.class)
                .readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}");
        assertTrue(result.map.containsKey(null));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled
    public void testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled() throws Exception
    {
        assertFalse(MAPPER.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL));
         try {
             MAPPER.readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}", ClassWithEnumMapKey.class);
             fail("Expected an exception for bogus enum value...");
         } catch (JsonMappingException jex) {
             verifyException(jex, "Can not construct Map key");
         }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumsFromInts
    public void testEnumsFromInts() throws Exception
    {
        Object ob = MAPPER.readValue("1 ", TestEnumFor834.class);
        assertEquals(TestEnumFor834.class, ob.getClass());
        assertSame(TestEnumFor834.ENUM_A, ob);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumsWithEmpty
    public void testEnumsWithEmpty() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
       TestEnum result = mapper.readValue("\"\"", TestEnum.class);
       assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testGenericEnumDeserialization
    public void testGenericEnumDeserialization() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       SimpleModule module = new SimpleModule("foobar");
       module.addDeserializer(Enum.class, new LcEnumDeserializer());
       mapper.registerModule(module);
       
       assertEquals(TestEnum.JACKSON, mapper.readValue(quote("jackson"), TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testExceptionFromCreator
    public void testExceptionFromCreator() throws Exception
    {
        try {
             MAPPER.readValue(quote("xyz"), TestEnum324.class);
            fail("Should throw exception");
        } catch (JsonMappingException e) {
            verifyException(e, "foobar");
        }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testUnwrappedEnum
    public void testUnwrappedEnum() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        assertEquals(TestEnum.JACKSON, mapper.readValue("[" + quote("JACKSON") + "]", TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testUnwrappedEnumException
    public void testUnwrappedEnumException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            assertEquals(TestEnum.JACKSON, mapper.readValue("[" + quote("JACKSON") + "]", TestEnum.class));
            fail("Exception was not thrown on deserializing a single array element of type enum");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testIndexAsString
    public void testIndexAsString() throws Exception
    {
        
        TestEnum en = MAPPER.readValue("2", TestEnum.class);
        assertSame(TestEnum.values()[2], en);

        
        en = MAPPER.readValue(quote("1"), TestEnum.class);
        assertSame(TestEnum.values()[1], en);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testDeserializerForCreatorWithEnumMaps
    public void testDeserializerForCreatorWithEnumMaps() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new DelegatingDeserializersModule());
        EnumMap<EnumWithCreator,String> value = mapper.readValue("{\"enumA\":\"value\"}",
            new TypeReference<EnumMap<EnumWithCreator,String>>() {});
        assertEquals("value", value.get(EnumWithCreator.A));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumWithJsonPropertyRename
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

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testIOException
    public void testIOException() throws IOException
    {
        IOException ioe = new IOException("TEST");
        String json = MAPPER.writeValueAsString(ioe);
        IOException result = MAPPER.readValue(json, IOException.class);
        assertEquals(ioe.getMessage(), result.getMessage());
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testWithCreator
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

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testWithNullMessage
    public void testWithNullMessage() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(new IOException((String) null));
        IOException result = mapper.readValue(json, IOException.class);
        assertNotNull(result);
        assertNull(result.getMessage());
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testNoArgsException
    public void testNoArgsException() throws IOException
    {
        MyNoArgException exc = MAPPER.readValue("{}", MyNoArgException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testJDK7SuppressionProperty
    public void testJDK7SuppressionProperty() throws IOException
    {
        Exception exc = MAPPER.readValue("{\"suppressed\":[]}", IOException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testSingleValueArrayDeserialization
    public void testSingleValueArrayDeserialization() {}

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testSingleValueArrayDeserializationException
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
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testHandlingOfUnrecognized
    public void testHandlingOfUnrecognized() throws Exception
    {
        UnrecognizedPropertyException exc = null;
        try {
            new ObjectMapper().readValue("{\"bar\":3}", Bean.class);
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

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testExceptionWithEmpty
    public void testExceptionWithEmpty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object result = mapper.readValue("    ", Object.class);
            fail("Expected an exception, but got result value: "+result);
        } catch (Exception e) {
            verifyException(e, JsonMappingException.class, "No content");
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testExceptionWithIncomplete
    public void testExceptionWithIncomplete()
        throws Exception
    {
        BrokenStringReader r = new BrokenStringReader("[ 1, ", "TEST");
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(r);
        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unused")
            Object ob = mapper.readValue(jp, Object.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testExceptionWithEOF
    public void testExceptionWithEOF()
        throws Exception
    {
        StringReader r = new StringReader("  3");
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(r);
        ObjectMapper mapper = new ObjectMapper();

        Integer I = mapper.readValue(jp, Integer.class);
        assertEquals(3, I.intValue());

        
        try {
            I = mapper.readValue(jp, Integer.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, JsonMappingException.class, "No content");
        }
        
        JsonToken t = jp.getCurrentToken();
        if (t != null) {
            fail("Expected current token to be null after end-of-stream, was: "+t);
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandlingWithDefaultDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals("com.fasterxml.jackson.databind.deser.Foo[\"bar\"]->com.fasterxml.jackson.databind.deser.Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandlingWithJsonCreatorDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals("com.fasterxml.jackson.databind.deser.Foo[\"bar\"]->com.fasterxml.jackson.databind.deser.Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testSimpleAutoDetect
    public void testSimpleAutoDetect() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        SimpleFieldBean result = m.readValue("{ \"x\" : -13 }",
                                           SimpleFieldBean.class);
        assertEquals(-13, result.x);
        assertEquals(0, result.y);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testSimpleAnnotation
    public void testSimpleAnnotation() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        SimpleFieldBean2 bean = m.readValue("{ \"values\" : [ \"x\", \"y\" ] }",
                SimpleFieldBean2.class);
        String[] values = bean.values;
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("x", values[0]);
        assertEquals("y", values[1]);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testNoAutoDetect
    public void testNoAutoDetect() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        NoAutoDetectBean bean = m.readValue("{ \"z\" : 7 }",
                                            NoAutoDetectBean.class);
        assertEquals(7, bean._z);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testTypeAnnotation
    public void testTypeAnnotation() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        AbstractWrapper w = m.readValue("{ \"value\" : \"abc\" }",
                                        AbstractWrapper.class);
        Abstract bean = w.value;
        assertNotNull(bean);
        assertEquals(Concrete.class, bean.getClass());
        assertEquals("abc", ((Concrete)bean).value);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testFailureDueToDups
    public void testFailureDueToDups() throws Exception
    {
        try {
            writeAndMap(new ObjectMapper(), new DupFieldBean());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing property");
        }
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testFailureDueToDups2
    public void testFailureDueToDups2() throws Exception
    {
        try {
            writeAndMap(new ObjectMapper(), new DupFieldBean2());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing property");
        }
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testOkFieldOverride
    public void testOkFieldOverride() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        OkDupFieldBean result = m.readValue("{ \"x\" : 1, \"y\" : 2 }",
                OkDupFieldBean.class);
        assertEquals(1, result.myX);
        assertEquals(2, result.y);
    }

// com.fasterxml.jackson.databind.deser.TestGenericCollectionDeser::testListSubClass
    public void testListSubClass() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ListSubClass result = mapper.readValue("[ \"123\" ]", ListSubClass.class);
        assertEquals(1, result.size());
        Object value = result.get(0);
        assertEquals(StringWrapper.class, value.getClass());
        StringWrapper bw = (StringWrapper) value;
        assertEquals("123", bw.str);
    }

// com.fasterxml.jackson.databind.deser.TestGenericCollectionDeser::testAnnotatedLStringist
    public void testAnnotatedLStringist() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedStringList result = mapper.readValue("[ \"...\" ]", AnnotatedStringList.class);
        assertEquals(1, result.size());
        Object ob = result.get(0);
        assertEquals(StringWrapper.class, ob.getClass());
        assertEquals("...", ((StringWrapper) ob).str);
    }

// com.fasterxml.jackson.databind.deser.TestGenericCollectionDeser::testAnnotatedBooleanList
    public void testAnnotatedBooleanList() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedBooleanList result = mapper.readValue("[ false ]", AnnotatedBooleanList.class);
        assertEquals(1, result.size());
        Object ob = result.get(0);
        assertEquals(BooleanWrapper.class, ob.getClass());
        assertFalse(((BooleanWrapper) ob).b);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testMapSubClass
    public void testMapSubClass() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        MapSubClass result = mapper.readValue
            ("{\"a\":true }", MapSubClass.class);
        assertEquals(1, result.size());
        Object value = result.get("a");
        assertEquals(BooleanWrapper.class, value.getClass());
        BooleanWrapper bw = (BooleanWrapper) value;
        assertEquals(Boolean.TRUE, bw.b);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testMapWrapper
    public void testMapWrapper() throws Exception
    {
        StringMap value = new ObjectMapper().readValue
            ("{\"entries\":{\"a\":9} }", StringMap.class);
        assertNotNull(value.getEntries());
        assertEquals(1, value.getEntries().size());
        assertEquals(Long.valueOf(9), value.getEntries().get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testIntermediateTypes
    public void testIntermediateTypes() throws Exception
    {
        StringStringWrapperMap result = new ObjectMapper().readValue
            ("{\"a\":\"b\"}", StringStringWrapperMap.class);
        assertEquals(1, result.size());
        Object value = result.get("a");
        assertNotNull(value);
        assertEquals(value.getClass(), StringWrapper.class);
        assertEquals("b", ((StringWrapper) value).str);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testAnnotatedMap
    public void testAnnotatedMap() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMap result = mapper.readValue
            ("{\"a\":true }", AnnotatedMap.class);
        assertEquals(1, result.size());
        Map.Entry<Object,Object> en = result.entrySet().iterator().next();
        assertEquals(StringWrapper.class, en.getKey().getClass());
        assertEquals(BooleanWrapper.class, en.getValue().getClass());
        assertEquals("a", ((StringWrapper) en.getKey()).str);
        assertEquals(Boolean.TRUE, ((BooleanWrapper) en.getValue()).b);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testKeyViaCtor
    public void testKeyViaCtor() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<KeyTypeCtor,Integer> map = mapper.readValue("{\"a\":123}",
                TypeFactory.defaultInstance().constructMapType(HashMap.class, KeyTypeCtor.class, Integer.class));
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123), entry.getValue());
        Object key = entry.getKey();
        assertEquals(KeyTypeCtor.class, key.getClass());
        assertEquals("a", ((KeyTypeCtor) key).value);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testKeyViaFactory
    public void testKeyViaFactory() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<KeyTypeCtor,Integer> map = mapper.readValue("{\"a\":123}",
                TypeFactory.defaultInstance().constructMapType(HashMap.class, KeyTypeFactory.class, Integer.class));
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123), entry.getValue());
        Object key = entry.getKey();
        assertEquals(KeyTypeFactory.class, key.getClass());
        assertEquals("a", ((KeyTypeFactory) key).value);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testSimpleNumberBean
    public void testSimpleNumberBean() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        NumberBean result = mapper.readValue("{\"number\":17}", NumberBean.class);
        assertEquals(17, result._number);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testGenericWrapper
    public void testGenericWrapper() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Wrapper<SimpleBean> result = mapper.readValue
            ("{\"value\": { \"x\" : 13 } }",
             new TypeReference<Wrapper<SimpleBean>>() { });
        assertNotNull(result);
        assertEquals(Wrapper.class, result.getClass());
        Object contents = result.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testGenericWrapperWithSingleElementArray
    public void testGenericWrapperWithSingleElementArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        Wrapper<SimpleBean> result = mapper.readValue
            ("[{\"value\": [{ \"x\" : 13 }] }]",
             new TypeReference<Wrapper<SimpleBean>>() { });
        assertNotNull(result);
        assertEquals(Wrapper.class, result.getClass());
        Object contents = result.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testMultipleWrappers
    public void testMultipleWrappers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        
        Wrapper<Boolean> result = mapper.readValue
            ("{\"value\": true}", new TypeReference<Wrapper<Boolean>>() { });
        assertEquals(new Wrapper<Boolean>(Boolean.TRUE), result);

        
        Wrapper<String> result2 = mapper.readValue
            ("{\"value\": \"abc\"}", new TypeReference<Wrapper<String>>() { });
        assertEquals(new Wrapper<String>("abc"), result2);

        
        Wrapper<Long> result3 = mapper.readValue
            ("{\"value\": 7}", new TypeReference<Wrapper<Long>>() { });
        assertEquals(new Wrapper<Long>(7L), result3);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testMultipleWrappersSingleValueArray
    public void testMultipleWrappersSingleValueArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        
        Wrapper<Boolean> result = mapper.readValue
            ("[{\"value\": [true]}]", new TypeReference<Wrapper<Boolean>>() { });
        assertEquals(new Wrapper<Boolean>(Boolean.TRUE), result);

        
        Wrapper<String> result2 = mapper.readValue
            ("[{\"value\": [\"abc\"]}]", new TypeReference<Wrapper<String>>() { });
        assertEquals(new Wrapper<String>("abc"), result2);

        
        Wrapper<Long> result3 = mapper.readValue
            ("[{\"value\": [7]}]", new TypeReference<Wrapper<Long>>() { });
        assertEquals(new Wrapper<Long>(7L), result3);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testArrayOfGenericWrappers
    public void testArrayOfGenericWrappers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Wrapper<SimpleBean>[] result = mapper.readValue
            ("[ {\"value\": { \"x\" : 9 } } ]",
             new TypeReference<Wrapper<SimpleBean>[]>() { });
        assertNotNull(result);
        assertEquals(Wrapper[].class, result.getClass());
        assertEquals(1, result.length);
        Wrapper<SimpleBean> elem = result[0];
        Object contents = elem.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(9, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testArrayOfGenericWrappersSingleValueArray
    public void testArrayOfGenericWrappersSingleValueArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        Wrapper<SimpleBean>[] result = mapper.readValue
            ("[ {\"value\": [ { \"x\" : [ 9 ] } ] } ]",
             new TypeReference<Wrapper<SimpleBean>[]>() { });
        assertNotNull(result);
        assertEquals(Wrapper[].class, result.getClass());
        assertEquals(1, result.length);
        Wrapper<SimpleBean> elem = result[0];
        Object contents = elem.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(9, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testIgnoredType
    public void testIgnoredType() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        NonIgnoredType bean = mapper.readValue("{\"value\":13}", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(13, bean.value);

        
        bean = mapper.readValue("{ \"ignored\":[1,2,{}], \"value\":9 }", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(9, bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testSingleWithMixins
    public void testSingleWithMixins() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Person.class, PersonMixin.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        PersonWrapper input = new PersonWrapper();
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value\":1}", json);
    }

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testListWithMixins
    public void testListWithMixins() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Person.class, PersonMixin.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("Bob"));
        String json = mapper.writeValueAsString(persons);
        assertEquals("[{\"name\":\"Bob\"}]", json);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testSimple
    public void testSimple() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std()
            .addValue(String.class, "stuffValue")
            .addValue("myId", "xyz")
            .addValue(Long.TYPE, Long.valueOf(37))
            );
        InjectedBean bean = mapper.readValue("{\"value\":3}", InjectedBean.class);
        assertEquals(3, bean.value);
        assertEquals("stuffValue", bean.stuff);
        assertEquals("xyz", bean.otherStuff);
        assertEquals(37L, bean.third);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testWithCtors
    public void testWithCtors() throws Exception
    {
        CtorBean bean = MAPPER.readerFor(CtorBean.class)
            .with(new InjectableValues.Std()
                .addValue(String.class, "Bubba"))
            .readValue("{\"age\":55}");
        assertEquals(55, bean.age);
        assertEquals("Bubba", bean.name);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testTwoInjectablesViaCreator
    public void testTwoInjectablesViaCreator() throws Exception
    {
        CtorBean2 bean = MAPPER.readerFor(CtorBean2.class)
                .with(new InjectableValues.Std()
                    .addValue(String.class, "Bob")
                    .addValue("number", Integer.valueOf(13))
                ).readValue("{ }");
        assertEquals(Integer.valueOf(13), bean.age);
        assertEquals("Bob", bean.name);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testInvalidDup
    public void testInvalidDup() throws Exception
    {
        try {
            MAPPER.readValue("{}", BadBean1.class);
        } catch (Exception e) {
            verifyException(e, "Duplicate injectable value");
        }
        try {
            MAPPER.readValue("{}", BadBean2.class);
        } catch (Exception e) {
            verifyException(e, "Duplicate injectable value");
        }
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testIssueGH471
    public void testIssueGH471() throws Exception
    {
        final Object constructorInjected = "constructorInjected";
        final Object methodInjected = "methodInjected";
        final Object fieldInjected = "fieldInjected";

        ObjectMapper mapper = new ObjectMapper()
                        .setInjectableValues(new InjectableValues.Std()
                                .addValue("constructor_injected", constructorInjected)
                                .addValue("method_injected", methodInjected)
                                .addValue("field_injected", fieldInjected));

        IssueGH471Bean bean = mapper.readValue("{\"x\":13,\"constructor_value\":\"constructor\",\"method_value\":\"method\",\"field_value\":\"field\"}",
                IssueGH471Bean.class);

        
        assertSame(constructorInjected, bean.constructorInjected);
        assertSame(methodInjected, bean.methodInjected);
        assertSame(fieldInjected, bean.fieldInjected);

        
        assertEquals("constructor", bean.constructorValue);
        assertEquals("method", bean.methodValue);
        assertEquals("field", bean.fieldValue);

        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testTransientField
    public void testTransientField() throws Exception
    {
        TransientBean bean = MAPPER.readerFor(TransientBean.class)
                .with(new InjectableValues.Std()
                        .addValue("transient", "Injected!"))
                .readValue("{\"value\":28}");
        assertEquals(28, bean.value);
        assertEquals("Injected!", bean.injected);
    }

// com.fasterxml.jackson.databind.deser.TestInnerClass::testSimpleNonStaticInner
    public void testSimpleNonStaticInner() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        Dog input = new Dog("Smurf", true);
        String json = mapper.writeValueAsString(input);
        Dog output = mapper.readValue(json, Dog.class);
        assertEquals("Smurf", output.name);
        assertNotNull(output.brain);
        assertTrue(output.brain.isThinking);
        
        assertEquals("Smurf", output.brain.parentName());
        output.name = "Foo";
        assertEquals("Foo", output.brain.parentName());
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAtomicBoolean
    public void testAtomicBoolean() throws Exception
    {
        AtomicBoolean b = MAPPER.readValue("true", AtomicBoolean.class);
        assertTrue(b.get());
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAtomicInt
    public void testAtomicInt() throws Exception
    {
        AtomicInteger value = MAPPER.readValue("13", AtomicInteger.class);
        assertEquals(13, value.get());
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAtomicLong
    public void testAtomicLong() throws Exception
    {
        AtomicLong value = MAPPER.readValue("12345678901", AtomicLong.class);
        assertEquals(12345678901L, value.get());
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAtomicReference
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

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testAbsentExclusion
    public void testAbsentExclusion() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new SimpleWrapper(null)));
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testPolymorphicAtomicReference
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

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testFilteringOfAtomicReference
    public void testFilteringOfAtomicReference() throws Exception
    {
        SimpleWrapper input = new SimpleWrapper(null);
        ObjectMapper mapper = MAPPER;

        
        assertEquals("{\"value\":null}", mapper.writeValueAsString(input));

        
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude
                .Include.NON_NULL);
        assertEquals("{\"value\":null}", mapper.writeValueAsString(input));

        
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude
                .Include.NON_EMPTY);
        assertEquals("{}", mapper.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testTypeRefinement
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

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testDeserializeWithContentAs
    public void testDeserializeWithContentAs() throws Exception
    {
        AtomicRefReadWrapper result = MAPPER.readValue(aposToQuotes("{'value':'abc'}"),
                AtomicRefReadWrapper.class);
         Object v = result.value.get();
         assertNotNull(v);
         assertEquals(WrappedString.class, v.getClass());
         assertEquals("abc", ((WrappedString)v).value);
    }

// com.fasterxml.jackson.databind.deser.TestJDKAtomicTypes::testWithUnwrapping
    public void testWithUnwrapping() throws Exception
    {
         String jsonExp = aposToQuotes("{'XX.name':'Bob'}");
         String jsonAct = MAPPER.writeValueAsString(new UnwrappingRefParent());
         assertEquals(jsonExp, jsonAct);
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testJsonLocation
    public void testJsonLocation() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        JsonLocation loc = new JsonLocation("whatever",  -1, -1, 100, 13);
        
        String ser = serializeAsString(m, loc);
        JsonLocation result = m.readValue(ser, JsonLocation.class);
        assertNotNull(result);
        assertEquals(loc.getSourceRef(), result.getSourceRef());
        assertEquals(loc.getByteOffset(), result.getByteOffset());
        assertEquals(loc.getCharOffset(), result.getCharOffset());
        assertEquals(loc.getColumnNr(), result.getColumnNr());
        assertEquals(loc.getLineNr(), result.getLineNr());
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testJsonLocationProps
    public void testJsonLocationProps()
    {
        JsonLocation loc = new JsonLocation(null,  -1, -1, 100, 13);
        assertTrue(loc.equals(loc));
        assertFalse(loc.equals(null));
        assertFalse(loc.equals("abx"));

        
        loc.hashCode();
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testTokenBufferWithSample
    public void testTokenBufferWithSample() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        TokenBuffer result = m.readValue(SAMPLE_DOC_JSON_SPEC, TokenBuffer.class);
        verifyJsonSpecSampleDoc(result.asParser(), true);
        result.close();
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testTokenBufferWithSequence
    public void testTokenBufferWithSequence() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        JsonParser jp = createParserUsingReader("[ 32, [ 1 ], \"abc\", { \"a\" : true } ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        TokenBuffer buf = m.readValue(jp, TokenBuffer.class);

        
        JsonParser bufParser = buf.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, bufParser.nextToken());
        assertEquals(32, bufParser.getIntValue());
        assertNull(bufParser.nextToken());

        
        buf = m.readValue(jp, TokenBuffer.class);
        bufParser = buf.asParser();
        assertToken(JsonToken.START_ARRAY, bufParser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, bufParser.nextToken());
        assertEquals(1, bufParser.getIntValue());
        assertToken(JsonToken.END_ARRAY, bufParser.nextToken());
        assertNull(bufParser.nextToken());

        
        buf = m.readValue(jp, TokenBuffer.class);
        String str = m.readValue(buf.asParser(), String.class);
        assertEquals("abc", str);

        
        buf = m.readValue(jp, TokenBuffer.class);
        Map<?,?> map = m.readValue(buf.asParser(), Map.class);
        assertEquals(1, map.size());
        assertEquals(Boolean.TRUE, map.get("a"));
        
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testJavaType
    public void testJavaType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = TypeFactory.defaultInstance();
        
        String json = mapper.writeValueAsString(tf.constructType(String.class));
        assertEquals(quote(java.lang.String.class.getName()), json);
        
        JavaType t = mapper.readValue(json, JavaType.class);
        assertNotNull(t);
        assertEquals(String.class, t.getRawClass());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testFile
    public void testFile() throws Exception
    {
        
        File src = new File("/test").getAbsoluteFile();
        String abs = src.getAbsolutePath();

        
        String json = MAPPER.writeValueAsString(abs);
        File result = MAPPER.readValue(json, File.class);
        assertEquals(abs, result.getAbsolutePath());

        
        final ObjectMapper mapper2 = new ObjectMapper();
        mapper2.setVisibility(PropertyAccessor.CREATOR, Visibility.NONE);

        result = mapper2.readValue(json, File.class);
        assertEquals(abs, result.getAbsolutePath());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testRegexps
    public void testRegexps() throws IOException
    {
        final String PATTERN_STR = "abc:\\s?(\\d+)";
        Pattern exp = Pattern.compile(PATTERN_STR);
        
        String json = MAPPER.writeValueAsString(exp);
        Pattern result = MAPPER.readValue(json, Pattern.class);
        assertEquals(exp.pattern(), result.pattern());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCurrency
    public void testCurrency() throws IOException
    {
        Currency usd = Currency.getInstance("USD");
        assertEquals(usd, new ObjectMapper().readValue(quote("USD"), Currency.class));
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testLocale
    public void testLocale() throws IOException
    {
        assertEquals(new Locale("en"), MAPPER.readValue(quote("en"), Locale.class));
        assertEquals(new Locale("es", "ES"), MAPPER.readValue(quote("es_ES"), Locale.class));
        assertEquals(new Locale("FI", "fi", "savo"),
                MAPPER.readValue(quote("fi_FI_savo"), Locale.class));
        
        Locale loc = MAPPER.readValue(quote(""), Locale.class);
        assertSame(Locale.ROOT, loc);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testNullForPrimitives
    public void testNullForPrimitives() throws IOException
    {
        
        PrimitivesBean bean = MAPPER.readValue("{\"intValue\":null, \"booleanValue\":null, \"doubleValue\":null}",
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
        
        
        final ObjectMapper mapper2 = new ObjectMapper();
        mapper2.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        
        try {
            mapper2.readValue("{\"booleanValue\":null}", PrimitivesBean.class);
            fail("Expected failure for boolean + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type boolean");
        }
        
        try {
            mapper2.readValue("{\"byteValue\":null}", PrimitivesBean.class);
            fail("Expected failure for byte + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type byte");
        }
        try {
            mapper2.readValue("{\"charValue\":null}", PrimitivesBean.class);
            fail("Expected failure for char + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type char");
        }
        try {
            mapper2.readValue("{\"shortValue\":null}", PrimitivesBean.class);
            fail("Expected failure for short + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type short");
        }
        try {
            mapper2.readValue("{\"intValue\":null}", PrimitivesBean.class);
            fail("Expected failure for int + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type int");
        }
        try {
            mapper2.readValue("{\"longValue\":null}", PrimitivesBean.class);
            fail("Expected failure for long + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type long");
        }

        
        try {
            mapper2.readValue("{\"floatValue\":null}", PrimitivesBean.class);
            fail("Expected failure for float + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type float");
        }
        try {
            mapper2.readValue("{\"doubleValue\":null}", PrimitivesBean.class);
            fail("Expected failure for double + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type double");
        }
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCharSequence
    public void testCharSequence() throws IOException
    {
        CharSequence cs = MAPPER.readValue("\"abc\"", CharSequence.class);
        assertEquals(String.class, cs.getClass());
        assertEquals("abc", cs.toString());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testInetAddress
    public void testInetAddress() throws IOException
    {
        InetAddress address = MAPPER.readValue(quote("127.0.0.1"), InetAddress.class);
        assertEquals("127.0.0.1", address.getHostAddress());

        
        final String HOST = "google.com";
        address = MAPPER.readValue(quote(HOST), InetAddress.class);
        assertEquals(HOST, address.getHostName());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testInetSocketAddress
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testClass
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testClassWithParams
    public void testClassWithParams() throws IOException
    {
        String json = MAPPER.writeValueAsString(new ParamClassBean("Foobar"));

        ParamClassBean result = MAPPER.readValue(json, ParamClassBean.class);
        assertEquals("Foobar", result.name);
        assertSame(String.class, result.clazz);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testEmptyStringForWrappers
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testEmptyStringForPrimitives
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testUntypedWithJsonArrays
    public void testUntypedWithJsonArrays() throws Exception
    {
        
        Object ob = MAPPER.readValue("[1]", Object.class);
        assertTrue(ob instanceof List<?>);

        
        MAPPER.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        ob = MAPPER.readValue("[1]", Object.class);
        assertEquals(Object[].class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testLongToBoolean
    public void testLongToBoolean() throws Exception
    {
        long value = 1L + Integer.MAX_VALUE;
        BooleanBean b = MAPPER.readValue("{\"primitive\" : "+value+", \"wrapper\":"+value+", \"ctor\":"+value+"}",
                    BooleanBean.class);
        assertEquals(Boolean.TRUE, b.wrapper);
        assertTrue(b.primitive);
        assertEquals(Boolean.TRUE, b.ctor);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCharset
    public void testCharset() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        assertSame(UTF8, MAPPER.readValue(quote("UTF-8"), Charset.class));
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testStackTraceElement
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
        assertTrue(back.getClassName().endsWith("TestJdkTypes"));
        assertFalse(back.isNativeMethod());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testByteBuffer
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testStringBuilder
    public void testStringBuilder() throws Exception
    {
        StringBuilder sb = MAPPER.readValue(quote("abc"), StringBuilder.class);
        assertEquals("abc", sb.toString());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testStackTraceElementWithCustom
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testSingleElementArray
    public void testSingleElementArray() throws Exception {
        final int intTest = 932832;
        final double doubleTest = 32.3234;
        final long longTest = 2374237428374293423L;
        final short shortTest = (short) intTest;
        final float floatTest = 84.3743f;
        final byte byteTest = (byte) 43;
        final char charTest = 'c';

        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        final int intValue = mapper.readValue(asArray(intTest), Integer.TYPE);
        assertEquals(intTest, intValue);
        final Integer integerWrapperValue = mapper.readValue(asArray(Integer.valueOf(intTest)), Integer.class);
        assertEquals(Integer.valueOf(intTest), integerWrapperValue);

        final double doubleValue = mapper.readValue(asArray(doubleTest), Double.class);
        assertEquals(doubleTest, doubleValue);
        final Double doubleWrapperValue = mapper.readValue(asArray(Double.valueOf(doubleTest)), Double.class);
        assertEquals(Double.valueOf(doubleTest), doubleWrapperValue);

        final long longValue = mapper.readValue(asArray(longTest), Long.TYPE);
        assertEquals(longTest, longValue);
        final Long longWrapperValue = mapper.readValue(asArray(Long.valueOf(longTest)), Long.class);
        assertEquals(Long.valueOf(longTest), longWrapperValue);

        final short shortValue = mapper.readValue(asArray(shortTest), Short.TYPE);
        assertEquals(shortTest, shortValue);
        final Short shortWrapperValue = mapper.readValue(asArray(Short.valueOf(shortTest)), Short.class);
        assertEquals(Short.valueOf(shortTest), shortWrapperValue);

        final float floatValue = mapper.readValue(asArray(floatTest), Float.TYPE);
        assertEquals(floatTest, floatValue);
        final Float floatWrapperValue = mapper.readValue(asArray(Float.valueOf(floatTest)), Float.class);
        assertEquals(Float.valueOf(floatTest), floatWrapperValue);

        final byte byteValue = mapper.readValue(asArray(byteTest), Byte.TYPE);
        assertEquals(byteTest, byteValue);
        final Byte byteWrapperValue = mapper.readValue(asArray(Byte.valueOf(byteTest)), Byte.class);
        assertEquals(Byte.valueOf(byteTest), byteWrapperValue);

        final char charValue = mapper.readValue(asArray(quote(String.valueOf(charTest))), Character.TYPE);
        assertEquals(charTest, charValue);
        final Character charWrapperValue = mapper.readValue(asArray(quote(String.valueOf(charTest))), Character.class);
        assertEquals(Character.valueOf(charTest), charWrapperValue);

        final boolean booleanTrueValue = mapper.readValue(asArray(true), Boolean.TYPE);
        assertTrue(booleanTrueValue);

        final boolean booleanFalseValue = mapper.readValue(asArray(false), Boolean.TYPE);
        assertFalse(booleanFalseValue);

        final Boolean booleanWrapperTrueValue = mapper.readValue(asArray(Boolean.valueOf(true)), Boolean.class);
        assertEquals(Boolean.TRUE, booleanWrapperTrueValue);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testSingleElementArrayException
    public void testSingleElementArrayException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("[42]", Integer.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42]", Integer.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[42.273]", Double.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42.2723]", Double.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[42342342342342]", Long.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42342342342342342]", Long.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[42]", Short.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42]", Short.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[327.2323]", Float.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[82.81902]", Float.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[22]", Byte.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[22]", Byte.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("['d']", Character.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("['d']", Character.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[true]", Boolean.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[true]", Boolean.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testMultiValueArrayException
    public void testMultiValueArrayException() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
            mapper.readValue("[42,42]", Integer.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42,42]", Integer.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[42.273,42.273]", Double.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42.2723,42.273]", Double.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[42342342342342,42342342342342]", Long.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42342342342342342,42342342342342]", Long.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[42,42]", Short.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42,42]", Short.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[327.2323,327.2323]", Float.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[82.81902,327.2323]", Float.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[22,23]", Byte.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[22,23]", Byte.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue(asArray(quote("c") + ","  + quote("d")), Character.class);
            
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue(asArray(quote("c") + ","  + quote("d")), Character.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[true,false]", Boolean.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[true,false]", Boolean.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUntypedMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testBigUntypedMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUntypedMap2
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUntypedMap3
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testSpecialMap
    public void testSpecialMap() throws IOException
    {
       final ObjectWrapperMap map = MAPPER.readValue(UNTYPED_MAP_JSON, ObjectWrapperMap.class);
       assertNotNull(map);
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testGenericMap
    public void testGenericMap() throws IOException
    {
        final Map<String, ObjectWrapper> map = MAPPER.readValue
            (UNTYPED_MAP_JSON,
             new TypeReference<Map<String, ObjectWrapper>>() { });
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        Map<?,?> result = m.readValue(quote(""), Map.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testExactStringIntMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testIntBooleanMap
    public void testIntBooleanMap() throws Exception
    {
        
        String JSON = "{ \"1\" : true, \"-1\" : false }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<HashMap<Integer,Boolean>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals(Boolean.TRUE, result.get(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, result.get(Integer.valueOf(-1)));
        assertNull(result.get("foobar"));
        assertNull(result.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testExactStringStringMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testGenericStringIntMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapFromEmptyArray
    public void testMapFromEmptyArray() throws Exception
    {
        final String JSON = "  [\n]";
        assertFalse(MAPPER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(JSON, Map.class);
            fail("Should not accept Empty Array for Map by default");
        } catch (JsonProcessingException e) {
            verifyException(e, "START_ARRAY token");
        }
        
        ObjectReader r = MAPPER.readerFor(Map.class)
                .with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        Map<?,?> result = r.readValue(JSON);
        assertNull(result);

        EnumMap<?,?> result2 = r.forType(new TypeReference<EnumMap<Key,String>>() { })
                .readValue(JSON);
        assertNull(result2);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testEnumMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapWithEnums
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testEnumPolymorphicSerializationTest
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testDateMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testCalendarMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUUIDKeyMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testLocaleKeyMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testCurrencyKeyMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testKeyWithCreator
    public void testKeyWithCreator() throws Exception
    {
        
        KeyType key = MAPPER.readValue(quote("abc"), KeyType.class);
        assertEquals("abc", key.value);

        Map<KeyType,Integer> map = MAPPER.readValue("{\"foo\":3}", new TypeReference<Map<KeyType,Integer>>() {} );
        assertEquals(1, map.size());
        key = map.keySet().iterator().next();
        assertEquals("foo", key.value);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testClassKeyMap
    public void testClassKeyMap() throws Exception {
        ClassStringMap map = MAPPER.readValue(aposToQuotes("{'java.lang.String':'foo'}"),
                ClassStringMap.class);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("foo", map.get(String.class));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapWithDeserializer
    public void testMapWithDeserializer() throws Exception
    {
        CustomMap result = MAPPER.readValue(quote("xyz"), CustomMap.class);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("x"));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapEntrySimpleTypes
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapEntryWithStringBean
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapEntryFail
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testReadProperties
    public void testReadProperties() throws Exception
    {
        Properties props = MAPPER.readValue(aposToQuotes("{'a':'foo', 'b':123, 'c':true}"),
                Properties.class);
        assertEquals(3, props.size());
        assertEquals("foo", props.getProperty("a"));
        assertEquals("123", props.getProperty("b"));
        assertEquals("true", props.getProperty("c"));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testSingletonMapRoundtrip
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapError
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testNoCtorMap
    public void testNoCtorMap() throws Exception
    {
        try {
            BrokenMap result = MAPPER.readValue("{ \"a\" : 3 }", BrokenMap.class);
            
            assertNull(result);
        } catch (JsonMappingException e) {
            
            verifyException(e, "no default constructor found");
        }
    }

// com.fasterxml.jackson.databind.deser.TestObjectOrArrayDeserialization::testObjectCase
    public void testObjectCase() throws Exception {
        ArrayOrObject arrayOrObject = new ObjectMapper().readValue("{}", ArrayOrObject.class);
        assertNull("expected objects field to be null", arrayOrObject.objects);
        assertNotNull("expected object field not to be null", arrayOrObject.object);
    }

// com.fasterxml.jackson.databind.deser.TestObjectOrArrayDeserialization::testEmptyArrayCase
    public void testEmptyArrayCase() throws Exception {
        ArrayOrObject arrayOrObject = new ObjectMapper().readValue("[]", ArrayOrObject.class);
        assertNotNull("expected objects field not to be null", arrayOrObject.objects);
        assertTrue("expected objects field to be an empty list", arrayOrObject.objects.isEmpty());
        assertNull("expected object field to be null", arrayOrObject.object);
    }

// com.fasterxml.jackson.databind.deser.TestObjectOrArrayDeserialization::testNotEmptyArrayCase
    public void testNotEmptyArrayCase() throws Exception {
        ArrayOrObject arrayOrObject = new ObjectMapper().readValue("[{}, {}]", ArrayOrObject.class);
        assertNotNull("expected objects field not to be null", arrayOrObject.objects);
        assertEquals("expected objects field to have size 2", 2, arrayOrObject.objects.size());
        assertNull("expected object field to be null", arrayOrObject.object);
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testSpecialization
    public void testSpecialization() throws Exception
    {
        ArrayListBean bean = MAPPER.readValue
            ("{\"list\":[\"a\",\"b\",\"c\"]}", ArrayListBean.class);
        assertNotNull(bean.list);
        assertEquals(3, bean.list.size());
        assertEquals(ArrayList.class, bean.list.getClass());
        assertEquals("a", bean.list.get(0));
        assertEquals("b", bean.list.get(1));
        assertEquals("c", bean.list.get(2));
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testOverride
    public void testOverride() throws Exception
    {
        WasNumberBean bean = MAPPER.readValue
            ("{\"value\" : \"abc\"}", WasNumberBean.class);
        assertNotNull(bean);
        assertEquals("abc", bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testConflictResolution
    public void testConflictResolution() throws Exception
    {
        Overloaded739 bean = MAPPER.readValue
                ("{\"value\":\"abc\"}", Overloaded739.class);
        assertNotNull(bean);
        assertEquals("abc", bean._value);
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testSetterConflict
    public void testSetterConflict() throws Exception
    {
    	try {    		
    	MAPPER.readValue("{ }", ConflictBean.class);
    	} catch (Exception e) {
    	    verifyException(e, "Conflicting setter definitions");
    	}
    }

// com.fasterxml.jackson.databind.deser.TestPolymorphicDeserialization676::testDeSerFail
    public void testDeSerFail() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        MapContainer deserMapBad = createDeSerMapContainer(originMap, mapper);
        assertEquals(originMap, deserMapBad);
        assertEquals(originMap,
                mapper.readValue(mapper.writeValueAsString(originMap), MapContainer.class));
    }

// com.fasterxml.jackson.databind.deser.TestPolymorphicDeserialization676::testDeSerCorrect
    public void testDeSerCorrect() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", 1);
        
        assertEquals(new MapContainer(map),
                mapper.readValue(mapper.writeValueAsString(new MapContainer(map)),
                        MapContainer.class));

        MapContainer deserMapGood = createDeSerMapContainer(originMap, mapper);

        assertEquals(originMap, deserMapGood);
        assertEquals(new Date(TIMESTAMP), deserMapGood.map.get("DateValue"));

        assertEquals(originMap, mapper.readValue(mapper.writeValueAsString(originMap), MapContainer.class));
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessCollectionOk
    public void testSimpleSetterlessCollectionOk()
        throws Exception
    {
        CollectionBean result = new ObjectMapper().readValue
            ("{\"values\":[ \"abc\", \"def\" ]}", CollectionBean.class);
        List<String> l = result._values;
        assertEquals(2, l.size());
        assertEquals("abc", l.get(0));
        assertEquals("def", l.get(1));
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessCollectionFailure
    public void testSimpleSetterlessCollectionFailure()
        throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        assertTrue(m.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS));
        m.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        assertFalse(m.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS));

        
        try {
            m.readValue
                ("{\"values\":[ \"abc\", \"def\" ]}", CollectionBean.class);
            fail("Expected an exception");
        } catch (JsonMappingException e) {
            
            verifyException(e, "Unrecognized field");
        }
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessMapOk
    public void testSimpleSetterlessMapOk()
        throws Exception
    {
        MapBean result = new ObjectMapper().readValue
            ("{\"values\":{ \"a\": 15, \"b\" : -3 }}", MapBean.class);
        Map<String,Integer> m = result._values;
        assertEquals(2, m.size());
        assertEquals(Integer.valueOf(15), m.get("a"));
        assertEquals(Integer.valueOf(-3), m.get("b"));
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessMapFailure
    public void testSimpleSetterlessMapFailure()
        throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        
        try {
            m.readValue
                ("{\"values\":{ \"a\":3 }}", MapBean.class);
            fail("Expected an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field");
        }
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSetterlessPrecedence
    public void testSetterlessPrecedence() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_GETTERS_AS_SETTERS, true);
        Dual value = m.readValue("{\"list\":[1,2,3]}, valueType)", Dual.class);
        assertNotNull(value);
        assertEquals(3, value.values.size());
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBooleanPrimitive
    public void testBooleanPrimitive() throws Exception
    {
        
        BooleanBean result = MAPPER.readValue(new StringReader("{\"v\":true}"), BooleanBean.class);
        assertTrue(result._v);
        
        result = MAPPER.readValue(new StringReader("{\"v\":null}"), BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);

        
        boolean[] array = MAPPER.readValue(new StringReader("[ null ]"), boolean[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertFalse(array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue(new StringReader("{\"v\":[true]}"), BooleanBean.class);
        assertTrue(result._v);
        
        try {
            mapper.readValue(new StringReader("[{\"v\":[true,true]}]"), BooleanBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue(new StringReader("{\"v\":[null]}"), BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        
        result = mapper.readValue(new StringReader("[{\"v\":[null]}]"), BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        
        array = mapper.readValue(new StringReader("[ [ null ] ]"), boolean[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertFalse(array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testIntPrimitive
    public void testIntPrimitive() throws Exception
    {
        
        IntBean result = MAPPER.readValue(new StringReader("{\"v\":3}"), IntBean.class);
        assertEquals(3, result._v);
        
        result = MAPPER.readValue(new StringReader("{\"v\":null}"), IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        
        int[] array = MAPPER.readValue(new StringReader("[ null ]"), int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue(new StringReader("{\"v\":[3]}"), IntBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue(new StringReader("{\"v\":[3]}"), IntBean.class);
        assertEquals(3, result._v);
        
        result = mapper.readValue(new StringReader("[{\"v\":[3]}]"), IntBean.class);
        assertEquals(3, result._v);
        
        try {
            mapper.readValue(new StringReader("[{\"v\":[3,3]}]"), IntBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue(new StringReader("{\"v\":[null]}"), IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        array = mapper.readValue(new StringReader("[ [ null ] ]"), int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testDoublePrimitive
    public void testDoublePrimitive() throws Exception
    {
        
        
        double value = 0.016;
        DoubleBean result = MAPPER.readValue(new StringReader("{\"v\":"+value+"}"), DoubleBean.class);
        assertEquals(value, result._v);
        
        result = MAPPER.readValue(new StringReader("{\"v\":null}"), DoubleBean.class);
        assertNotNull(result);
        assertEquals(0.0, result._v);

        
        double[] array = MAPPER.readValue(new StringReader("[ null ]"), double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0.0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue(new StringReader("{\"v\":[" + value + "]}"), DoubleBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue(new StringReader("{\"v\":[" + value + "]}"), DoubleBean.class);
        assertEquals(value, result._v);
        
        result = mapper.readValue(new StringReader("[{\"v\":[" + value + "]}]"), DoubleBean.class);
        assertEquals(value, result._v);
        
        try {
            mapper.readValue(new StringReader("[{\"v\":[" + value + "," + value + "]}]"), DoubleBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue(new StringReader("{\"v\":[null]}"), DoubleBean.class);
        assertNotNull(result);
        assertEquals(0d, result._v);

        array = mapper.readValue(new StringReader("[ [ null ] ]"), double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0d, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testDoublePrimitiveNonNumeric
    public void testDoublePrimitiveNonNumeric() throws Exception
    {
        
        
        double value = Double.POSITIVE_INFINITY;
        DoubleBean result = MAPPER.readValue(new StringReader("{\"v\":\""+value+"\"}"), DoubleBean.class);
        assertEquals(value, result._v);
        
        
        double[] array = MAPPER.readValue(new StringReader("[ \"Infinity\" ]"), double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Double.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testFloatPrimitiveNonNumeric
    public void testFloatPrimitiveNonNumeric() throws Exception
    {
        
        float value = Float.POSITIVE_INFINITY;
        FloatBean result = MAPPER.readValue(new StringReader("{\"v\":\""+value+"\"}"), FloatBean.class);
        assertEquals(value, result._v);
        
        
        float[] array = MAPPER.readValue(new StringReader("[ \"Infinity\" ]"), float[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Float.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testIntWithOverride
    public void testIntWithOverride() throws Exception
    {
        IntBean2 result = MAPPER.readValue(new StringReader("{\"v\":8}"), IntBean2.class);
        assertEquals(9, result._v);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testEmptyToNullCoercionForPrimitives
    public void testEmptyToNullCoercionForPrimitives() throws Exception {
        _testEmptyToNullCoercion(int.class, Integer.valueOf(0));
        _testEmptyToNullCoercion(long.class, Long.valueOf(0));
        _testEmptyToNullCoercion(double.class, Double.valueOf(0.0));
        _testEmptyToNullCoercion(float.class, Float.valueOf(0.0f));
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBooleanWrapper
    public void testBooleanWrapper() throws Exception
    {
        Boolean result = MAPPER.readValue(new StringReader("true"), Boolean.class);
        assertEquals(Boolean.TRUE, result);
        result = MAPPER.readValue(new StringReader("false"), Boolean.class);
        assertEquals(Boolean.FALSE, result);

        
        result = MAPPER.readValue(new StringReader("0"), Boolean.class);
        assertEquals(Boolean.FALSE, result);
        result = MAPPER.readValue(new StringReader("1"), Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testByteWrapper
    public void testByteWrapper() throws Exception
    {
        Byte result = MAPPER.readValue(new StringReader("   -42\t"), Byte.class);
        assertEquals(Byte.valueOf((byte)-42), result);

        
        result = MAPPER.readValue(new StringReader(" \"-12\""), Byte.class);
        assertEquals(Byte.valueOf((byte)-12), result);

        result = MAPPER.readValue(new StringReader(" 39.07"), Byte.class);
        assertEquals(Byte.valueOf((byte)39), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testShortWrapper
    public void testShortWrapper() throws Exception
    {
        Short result = MAPPER.readValue(new StringReader("37"), Short.class);
        assertEquals(Short.valueOf((short)37), result);

        
        result = MAPPER.readValue(new StringReader(" \"-1009\""), Short.class);
        assertEquals(Short.valueOf((short)-1009), result);

        result = MAPPER.readValue(new StringReader("-12.9"), Short.class);
        assertEquals(Short.valueOf((short)-12), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testCharacterWrapper
    public void testCharacterWrapper() throws Exception
    {
        
        Character result = MAPPER.readValue(new StringReader("\"a\""), Character.class);
        assertEquals(Character.valueOf('a'), result);

        
        result = MAPPER.readValue(new StringReader(" "+((int) 'X')), Character.class);
        assertEquals(Character.valueOf('X'), result);
        
        final CharacterWrapperBean wrapper = MAPPER.readValue(new StringReader("{\"v\":null}"), CharacterWrapperBean.class);
        assertNotNull(wrapper);
        assertNull(wrapper.getV());
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        try {
            mapper.readValue("{\"v\":null}", CharacterBean.class);
            fail("Attempting to deserialize a 'null' JSON reference into a 'char' property did not throw an exception");
        } catch (JsonMappingException exp) {
            
        }

        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);  
        final CharacterBean charBean = MAPPER.readValue(new StringReader("{\"v\":null}"), CharacterBean.class);
        assertNotNull(wrapper);
        assertEquals('\u0000', charBean.getV());
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testIntWrapper
    public void testIntWrapper() throws Exception
    {
        Integer result = MAPPER.readValue(new StringReader("   -42\t"), Integer.class);
        assertEquals(Integer.valueOf(-42), result);

        
        result = MAPPER.readValue(new StringReader(" \"-1200\""), Integer.class);
        assertEquals(Integer.valueOf(-1200), result);

        result = MAPPER.readValue(new StringReader(" 39.07"), Integer.class);
        assertEquals(Integer.valueOf(39), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testLongWrapper
    public void testLongWrapper() throws Exception
    {
        Long result = MAPPER.readValue(new StringReader("12345678901"), Long.class);
        assertEquals(Long.valueOf(12345678901L), result);

        
        result = MAPPER.readValue(new StringReader(" \"-9876\""), Long.class);
        assertEquals(Long.valueOf(-9876), result);

        result = MAPPER.readValue(new StringReader("1918.3"), Long.class);
        assertEquals(Long.valueOf(1918), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testFloatWrapper
    public void testFloatWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Float exp = Float.valueOf(str);
            Float result;

            if (NAN_STRING != str) {
                
                result = MAPPER.readValue(new StringReader(str), Float.class);
                assertEquals(exp, result);
            }

            
            result = MAPPER.readValue(new StringReader(" \""+str+"\""), Float.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testDoubleWrapper
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
            
            result = MAPPER.readValue(new StringReader(" \""+str+"\""), Double.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBase64Variants
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

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testSingleString
    public void testSingleString() throws Exception
    {
        String value = "FOO!";
        String result = MAPPER.readValue(new StringReader("\""+value+"\""), String.class);
        assertEquals(value, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testSingleStringWrapped
    public void testSingleStringWrapped() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        String value = "FOO!";
        try {
            mapper.readValue(new StringReader("[\""+value+"\"]"), String.class);
            fail("Exception not thrown when attempting to unwrap a single value 'String' array into a simple String");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
            mapper.readValue(new StringReader("[\""+value+"\",\""+value+"\"]"), String.class);
            fail("Exception not thrown when attempting to unwrap a single value 'String' array that contained more than one value into a simple String");
        } catch (JsonMappingException exp) {
            
        }
        
        String result = mapper.readValue(new StringReader("[\""+value+"\"]"), String.class);
        assertEquals(value, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testNull
    public void testNull() throws Exception
    {
        
        Object result = MAPPER.readValue("   null", Object.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testClass
    public void testClass() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();        
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);        
        
        Class<?> result = mapper.readValue(quote(String.class.getName()), Class.class);
        assertEquals(String.class, result);
        
        
        try {
            mapper.readValue("[" + quote(String.class.getName()) + "]", Class.class);
            fail("Did not throw exception when UNWRAP_SINGLE_VALUE_ARRAYS feature was disabled and attempted to read a Class array containing one element");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
           mapper.readValue("[" + quote(Object.class.getName()) + "," + quote(Object.class.getName()) +"]", Class.class); 
           fail("Did not throw exception when UNWRAP_SINGLE_VALUE_ARRAYS feature was enabled and attempted to read a Class array containing two elements");
        } catch (JsonMappingException exp) {
            
        }               
        result = mapper.readValue("[" + quote(String.class.getName()) + "]", Class.class);
        assertEquals(String.class, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBigDecimal
    public void testBigDecimal() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        BigDecimal value = new BigDecimal("0.001");
        BigDecimal result = mapper.readValue(value.toString(), BigDecimal.class);
        assertEquals(value, result);
        
        
        try {
            mapper.readValue("[" + value.toString() + "]", BigDecimal.class);
            fail("Exception was not thrown when attempting to read a single value array of BigDecimal when UNWRAP_SINGLE_VALUE_ARRAYS feature is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue("[" + value.toString() + "]", BigDecimal.class);
        assertEquals(value, result);
        
        try {
            mapper.readValue("[" + value.toString() + "," + value.toString() + "]", BigDecimal.class);
            fail("Exception was not thrown when attempting to read a muti value array of BigDecimal when UNWRAP_SINGLE_VALUE_ARRAYS feature is enabled");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBigInteger
    public void testBigInteger() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        BigInteger value = new BigInteger("-1234567890123456789012345567809");
        BigInteger result = mapper.readValue(new StringReader(value.toString()), BigInteger.class);
        assertEquals(value, result);
        
        
        try {
            mapper.readValue("[" + value.toString() + "]", BigInteger.class);
            fail("Exception was not thrown when attempting to read a single value array of BigInteger when UNWRAP_SINGLE_VALUE_ARRAYS feature is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue("[" + value.toString() + "]", BigInteger.class);
        assertEquals(value, result);
        
        try {
            mapper.readValue("[" + value.toString() + "," + value.toString() + "]", BigInteger.class);
            fail("Exception was not thrown when attempting to read a muti value array of BigInteger when UNWRAP_SINGLE_VALUE_ARRAYS feature is enabled");
        } catch (JsonMappingException exp) {
            
        }        
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testUUID
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
            
            try {
                mapper.readValue("[" + quote(value) + "]", UUID.class);
                fail("Exception was not thrown when UNWRAP_SINGLE_VALUE_ARRAYS is disabled and attempted to read a single value array as a single element");
            } catch (JsonMappingException exp) {
                
            }
            
            mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
            
            assertEquals(uuid,
                    mapper.readValue("[" + quote(value) + "]", UUID.class));
            
            try {
                mapper.readValue("[" + quote(value) + "," + quote(value) + "]", UUID.class);
                fail("Exception was not thrown when UNWRAP_SINGLE_VALUE_ARRAYS is enabled and attempted to read a multi value array as a single element");
            } catch (JsonMappingException exp) {
                
            }
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

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testUUIDInvalid
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

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testUUIDAux
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

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testURL
    public void testURL() throws Exception
    {
        URL value = new URL("http://foo.com");
        assertEquals(value, MAPPER.readValue("\""+value.toString()+"\"", URL.class));

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(value);
        assertSame(value, MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testURI
    public void testURI() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        
        URI value = new URI("http://foo.com");
        assertEquals(value, mapper.readValue("\""+value.toString()+"\"", URI.class));
        
        
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {            
            assertEquals(value, mapper.readValue("[\""+value.toString()+"\"]", URI.class));
            fail("Did not throw exception for single value array when UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            assertEquals(value, mapper.readValue("[\""+value.toString()+"\",\""+value.toString()+"\"]", URI.class));
            fail("Did not throw exception for single value array when there were multiple values");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        assertEquals(value, mapper.readValue("[\""+value.toString()+"\"]", URI.class));

        
        value = mapper.readValue(quote(""), URI.class);
        assertNotNull(value);
        assertEquals(URI.create(""), value);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testSequenceOfInts
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

// com.fasterxml.jackson.databind.deser.TestStatics::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        Bean result = m.readValue("{ \"x\":3}", Bean.class);
        assertEquals(3, result._x);
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testSampleDoc
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testNestedUntypes
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testObjectSerializeWithLong
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithCustomScalarDesers
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithListDeser
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithMapDeser
    public void testUntypedWithMapDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(Map.class, new MapDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("{\"a\":true}", Object.class);
        assertTrue(ob instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) ob;
        assertEquals(1, map.size());
        assertEquals("Ytrue", map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testNestedUntyped989
    public void testNestedUntyped989() throws IOException
    {
        Untyped989 pojo;
        ObjectReader r = MAPPER.readerFor(Untyped989.class);

        pojo = r.readValue("[]");
        assertTrue(pojo.value instanceof List);
        pojo = r.readValue("[{}]");
        assertTrue(pojo.value instanceof List);
        
        pojo = r.readValue("{}");
        assertTrue(pojo.value instanceof Map);
        pojo = r.readValue("{\"a\":[]}");
        assertTrue(pojo.value instanceof Map);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideClassValid
    public void testOverrideClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        CollectionHolder result = m.readValue
            ("{ \"strings\" : [ \"test\" ] }", CollectionHolder.class);

        Collection<String> strs = result._strings;
        assertEquals(1, strs.size());
        assertEquals(TreeSet.class, strs.getClass());
        assertEquals("test", strs.iterator().next());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideMapValid
    public void testOverrideMapValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        MapHolder result = m.readValue
            ("{ \"strings\" :  { \"a\" : 3 } }", MapHolder.class);

        Map<String,String> strs = result._data;
        assertEquals(1, strs.size());
        assertEquals(TreeMap.class, strs.getClass());
        String value = strs.get("a");
        assertEquals("3", value);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideArrayClass
    public void testOverrideArrayClass() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ArrayHolder result = m.readValue
            ("{ \"strings\" : [ \"test\" ] }", ArrayHolder.class);

        String[] strs = result._strings;
        assertEquals(1, strs.length);
        assertEquals(String[].class, strs.getClass());
        assertEquals("test", strs[0]);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideClassInvalid
    public void testOverrideClassInvalid() throws Exception
    {
        
        try {
            BrokenCollectionHolder result = new ObjectMapper().readValue
                ("{ \"strings\" : [ ] }", BrokenCollectionHolder.class);
            fail("Expected a failure, but got results: "+result);
        } catch (JsonMappingException jme) {
            verifyException(jme, "not subtype of");
        }
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootInterfaceAs
    public void testRootInterfaceAs() throws Exception
    {
        RootInterface value = new ObjectMapper().readValue("{\"a\":\"abc\" }", RootInterface.class);
        assertTrue(value instanceof RootInterfaceImpl);
        assertEquals("abc", value.getA());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootInterfaceUsing
    public void testRootInterfaceUsing() throws Exception
    {
        RootString value = new ObjectMapper().readValue("\"xxx\"", RootString.class);
        assertTrue(value instanceof RootString);
        assertEquals("xxx", value.contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootListAs
    public void testRootListAs() throws Exception
    {
        RootMap value = new ObjectMapper().readValue("{\"a\":\"b\"}", RootMap.class);
        assertEquals(1, value.size());
        Object v2 = value.get("a");
        assertEquals(RootStringImpl.class, v2.getClass());
        assertEquals("b", ((RootString) v2).contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootMapAs
    public void testRootMapAs() throws Exception
    {
        RootList value = new ObjectMapper().readValue("[ \"c\" ]", RootList.class);
        assertEquals(1, value.size());
        Object v2 = value.get(0);
        assertEquals(RootStringImpl.class, v2.getClass());
        assertEquals("c", ((RootString) v2).contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideKeyClassValid
	public void testOverrideKeyClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapKeyHolder result = m.readValue("{ \"map\" : { \"xxx\" : \"yyy\" } }", MapKeyHolder.class);
        Map<StringWrapper, String> map = (Map<StringWrapper,String>)(Map<?,?>)result._map;
        assertEquals(1, map.size());
        Map.Entry<StringWrapper, String> en = map.entrySet().iterator().next();

        StringWrapper key = en.getKey();
        assertEquals(StringWrapper.class, key.getClass());
        assertEquals("xxx", key._string);
        assertEquals("yyy", en.getValue());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideKeyClassInvalid
    public void testOverrideKeyClassInvalid() throws Exception
    {
        
        try {
            BrokenMapKeyHolder result = new ObjectMapper().readValue
                ("{ \"123\" : \"xxx\" }", BrokenMapKeyHolder.class);
            fail("Expected a failure, but got results: "+result);
        } catch (JsonMappingException jme) {
            verifyException(jme, "not subtype of");
        }
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideContentClassValid
	public void testOverrideContentClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ListContentHolder result = m.readValue("{ \"list\" : [ \"abc\" ] }", ListContentHolder.class);
        List<StringWrapper> list = (List<StringWrapper>)result._list;
        assertEquals(1, list.size());
        Object value = list.get(0);
        assertEquals(StringWrapper.class, value.getClass());
        assertEquals("abc", ((StringWrapper) value)._string);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideArrayContents
    public void testOverrideArrayContents() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ArrayContentHolder result = m.readValue("{ \"data\" : [ 1, 2, 3 ] }",
                                                ArrayContentHolder.class);
        Object[] data = result._data;
        assertEquals(3, data.length);
        assertEquals(Long[].class, data.getClass());
        assertEquals(1L, data[0]);
        assertEquals(2L, data[1]);
        assertEquals(3L, data[2]);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideMapContents
    public void testOverrideMapContents() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapContentHolder result = m.readValue("{ \"map\" : { \"a\" : 9 } }",
                                                MapContentHolder.class);
        Map<Object,Object> map = result._map;
        assertEquals(1, map.size());
        Object ob = map.values().iterator().next();
        assertEquals(Integer.class, ob.getClass());
        assertEquals(Integer.valueOf(9), ob);
    }

// com.fasterxml.jackson.databind.filter.ReadOnlyProperties95Test::testReadOnlyProp
    public void testReadOnlyProp() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String json = m.writeValueAsString(new ReadOnlyBean());
        if (json.indexOf("computed") < 0) {
            fail("Should have property 'computed', didn't: "+json);
        }
        ReadOnlyBean bean = m.readValue(json, ReadOnlyBean.class);
        assertNotNull(bean);
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testSimpleInclusionFilter
    public void testSimpleInclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("a"));
        assertEquals("{\"a\":\"a\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(prov);
        assertEquals("{\"a\":\"a\"}", mapper.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testIncludeAllFilter
    public void testIncludeAllFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAll());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testSimpleExclusionFilter
    public void testSimpleExclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAllExcept("a"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testMissingFilter
    public void testMissingFilter() throws Exception
    {
        
        try {
            MAPPER.writeValueAsString(new Bean());
            fail("Should have failed without configured filter");
        } catch (JsonMappingException e) { 
            verifyException(e, "Can not resolve PropertyFilter with id 'RootFilter'");
        }
        
        
        SimpleFilterProvider fp = new SimpleFilterProvider().setFailOnUnknownId(false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(fp);
        String json = mapper.writeValueAsString(new Bean());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", json);
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testDefaultFilter
    public void testDefaultFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testIssue89
    public void testIssue89() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Pod pod = new Pod();
        pod.username = "Bob";
        pod.userPassword = "s3cr3t!";

        String json = mapper.writeValueAsString(pod);

        assertEquals("{\"username\":\"Bob\"}", json);

        Pod pod2 = mapper.readValue("{\"username\":\"Bill\",\"user_password\":\"foo!\"}", Pod.class);
        assertEquals("Bill", pod2.username);
        assertEquals("foo!", pod2.userPassword);
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testFilterOnProperty
    public void testFilterOnProperty() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider()
            .addFilter("RootFilter", SimpleBeanPropertyFilter.filterOutAllExcept("a"))
            .addFilter("b", SimpleBeanPropertyFilter.filterOutAllExcept("b"));

        assertEquals("{\"first\":{\"a\":\"a\"},\"second\":{\"b\":\"b\"}}",
                MAPPER.writer(prov).writeValueAsString(new FilteredProps()));
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingDefault
    public void testUnknownHandlingDefault() throws Exception
    {
        try {
            MAPPER.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        } catch (JsonMappingException jex) {
            verifyException(jex, "Unrecognized field \"foo\"");
        }
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithHandler
    public void testUnknownHandlingIgnoreWithHandler() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.clearProblemHandlers();
        mapper.addHandler(new MyHandler());
        TestBean result = mapper.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        assertNotNull(result);
        assertEquals(1, result._a);
        assertEquals(-1, result._b);
        assertEquals("foo:START_ARRAY", result._unknown);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithHandlerAndObjectReader
    public void testUnknownHandlingIgnoreWithHandlerAndObjectReader() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.clearProblemHandlers();
        TestBean result = mapper.readerFor(TestBean.class).withHandler(new MyHandler()).readValue(new StringReader(JSON_UNKNOWN_FIELD));
        assertNotNull(result);
        assertEquals(1, result._a);
        assertEquals(-1, result._b);
        assertEquals("foo:START_ARRAY", result._unknown);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithFeature
    public void testUnknownHandlingIgnoreWithFeature() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TestBean result = null;
        try {
            result = mapper.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        } catch (JsonMappingException jex) {
            fail("Did not expect a problem, got: "+jex.getMessage());
        }
        assertNotNull(result);
        assertEquals(1, result._a);
        assertNull(result._unknown);
        assertEquals(-1, result._b);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testWithClassIgnore
    public void testWithClassIgnore() throws Exception
    {
        IgnoreSome result = MAPPER.readValue("{ \"a\":1,\"b\":2,\"c\":\"x\",\"d\":\"y\"}",
                IgnoreSome.class);
        
        assertEquals(1, result.a);
        assertEquals("y", result.d());
        
        assertEquals(0, result.b);
        assertNull(result.c());
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testClassIgnoreWithMap
    public void testClassIgnoreWithMap() throws Exception
    {
        
        IgnoreMap result = MAPPER.readValue
            ("{ \"a\":[ 1],\n"
                +"\"b\":2,\n"
                +"\"c\": \"x\",\n"
                +"\"d\":false }", IgnoreMap.class);
        assertEquals(2, result.size());
        Object ob = result.get("b");
        assertEquals(Integer.class, ob.getClass());
        assertEquals(Integer.valueOf(2), ob);
        assertEquals("x", result.get("c"));
        assertFalse(result.containsKey("a"));
        assertFalse(result.containsKey("d"));
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testClassWithIgnoreUnknown
    public void testClassWithIgnoreUnknown() throws Exception
    {
        IgnoreUnknown result = MAPPER.readValue
            ("{\"b\":3,\"c\":[1,2],\"x\":{ },\"a\":-3}", IgnoreUnknown.class);
        assertEquals(-3, result.a);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testClassWithUnknownAndIgnore
    public void testClassWithUnknownAndIgnore() throws Exception
    {
        
        ImplicitIgnores result = MAPPER.readValue
            ("{\"a\":1,\"b\":2,\"c\":3 }", ImplicitIgnores.class);
        assertEquals(3, result.c);

        
        try {
            MAPPER.readValue("{\"a\":1,\"b\":2,\"c\":3,\"d\":4 }", ImplicitIgnores.class);            
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field \"d\"");
        }
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testPropertyIgnoral
    public void testPropertyIgnoral() throws Exception
    {
        XYZWrapper1 result = MAPPER.readValue("{\"value\":{\"y\":2,\"x\":1,\"z\":3}}", XYZWrapper1.class);
        assertEquals(2, result.value.y);
        assertEquals(3, result.value.z);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testPropertyIgnoralWithClass
    public void testPropertyIgnoralWithClass() throws Exception
    {
        XYZWrapper2 result = MAPPER.readValue("{\"value\":{\"y\":2,\"x\":1,\"z\":3}}", XYZWrapper2.class);
        assertEquals(1, result.value.x);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testPropertyIgnoralForMap
    public void testPropertyIgnoralForMap() throws Exception
    {
        MapWithoutX result = MAPPER.readValue("{\"values\":{\"x\":1,\"y\":2}}", MapWithoutX.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(Integer.valueOf(2), result.values.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testIssue987
    public void testIssue987() throws Exception
    {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException, JsonProcessingException {
                p.skipChildren();
                return true;
            }
        });

        String input = "[{\"aProperty\":\"x\",\"unknown\":{\"unknown\":{}}}]";
        List<Bean987> deserializedList = jsonMapper.readValue(input,
                new TypeReference<List<Bean987>>() { });
        assertEquals(1, deserializedList.size());
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testSimplePOJOType
    public void testSimplePOJOType() throws Exception
    {
        JavaType elem = SimpleType.construct(Point.class);

        Point p = MAPPER.readValue(aposToQuotes("{'x':1,'y':2}"), elem);
        assertNotNull(p);
        assertEquals(1, p.x);
        assertEquals(2, p.getY());
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testPOJOSubType
    public void testPOJOSubType() throws Exception
    {
        JavaType elem = SimpleType.construct(Point3D.class);

        Point3D p = MAPPER.readValue(aposToQuotes("{'x':1,'z':3,'y':2}"), elem);
        assertNotNull(p);
        assertEquals(1, p.x);
        assertEquals(2, p.getY());
        assertEquals(3, p.z);
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testExplicitCollectionType
    public void testExplicitCollectionType() throws Exception
    {
        JavaType elem = SimpleType.construct(Point.class);
        JavaType t = CollectionType.construct(List.class, elem);

        final String json = aposToQuotes("[ {'x':1,'y':2}, {'x':3,'y':6 }]");        

        List<Point> l = MAPPER.readValue(json, t);
        assertNotNull(l);
        assertEquals(2, l.size());
        Object ob = l.get(0);
        assertEquals(Point.class, ob.getClass());
        Point p = (Point) ob;
        assertEquals(1, p.x);
        assertEquals(2, p.getY());
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testExplicitMapType
    public void testExplicitMapType() throws Exception
    {
        JavaType key = SimpleType.construct(String.class);
        JavaType elem = SimpleType.construct(Point.class);
        JavaType t = MapType.construct(Map.class, key, elem);

        final String json = aposToQuotes("{'x':{'x':3,'y':5}}");        

        Map<String,Point> m = MAPPER.readValue(json, t);
        assertNotNull(m);
        assertEquals(1, m.size());
        Object ob = m.values().iterator().next();
        assertEquals(Point.class, ob.getClass());
        Point p = (Point) ob;
        assertEquals(3, p.x);
        assertEquals(5, p.getY());
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testDeprecatedTypeResolution
    public void testDeprecatedTypeResolution() throws Exception
    {
        TypeFactory tf = MAPPER.getTypeFactory();

        
        JavaType t = tf.constructType(Point.class, getClass());
        assertEquals(Point.class, t.getRawClass());

        
        JavaType t2 = tf.constructType(Point.class, (Class<?>) null);
        assertEquals(Point.class, t2.getRawClass());

        JavaType ctxt = tf.constructType(getClass());
        JavaType t3 = tf.constructType(Point.class, ctxt);
        assertEquals(Point.class, t3.getRawClass());
    }

// com.fasterxml.jackson.databind.interop.TestExternalizable::testSerializeAsExternalizable
    public void testSerializeAsExternalizable() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream obs = new ObjectOutputStream(bytes);
        final MyPojo input = new MyPojo(13, "Foobar", new int[] { 1, 2, 3 } );
        obs.writeObject(input);
        obs.close();
        byte[] ser = bytes.toByteArray();

        
        byte[] json = MapperHolder.mapper().writeValueAsBytes(input);

        int ix = indexOf(ser, json);
        if (ix < 0) {
            fail("Serialization ("+ser.length+") does NOT contain JSON (of "+json.length+")");
        }
        
        
        if (false) {
            bytes = new ByteArrayOutputStream();
            obs = new ObjectOutputStream(bytes);
            MyPojoNative p = new MyPojoNative(13, "Foobar", new int[] { 1, 2, 3 } );
            obs.writeObject(p);
            obs.close();
            System.out.println("Native size: "+bytes.size()+", vs JSON: "+ser.length);
        }
        
        
        ObjectInputStream ins = new ObjectInputStream(new ByteArrayInputStream(ser));
        MyPojo output = (MyPojo) ins.readObject();
        ins.close();
        assertNotNull(output);
        
        assertEquals(input, output);
    }

// com.fasterxml.jackson.databind.interop.TestFormatDetection::testSimpleWithJSON
    public void testSimpleWithJSON() throws Exception
    {
        ObjectReader detecting = READER.forType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        POJO pojo = detecting.readValue(utf8Bytes("{\"x\":1}"));
        assertNotNull(pojo);
        assertEquals(1, pojo.x);
    }

// com.fasterxml.jackson.databind.interop.TestFormatDetection::testInvalid
    public void testInvalid() throws Exception
    {
        ObjectReader detecting = READER.forType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        try {
            detecting.readValue(utf8Bytes("<POJO><x>1</x></POJO>"));
            fail("Should have failed");
        } catch (JsonProcessingException e) {
            verifyException(e, "Can not detect format from input");
        }
    }

// com.fasterxml.jackson.databind.interop.TestJDKProxy::testSimple
    public void testSimple() throws Exception
    {
        IPlanet input = getProxy(IPlanet.class, new Planet("Foo"));
        String json = MAPPER.writeValueAsString(input);
        assertEquals("{\"name\":\"Foo\"}", json);
        
        
        Planet output = MAPPER.readValue(json, Planet.class);
        assertEquals("Foo", output.getName());
    }

// com.fasterxml.jackson.databind.introspect.NoClassDefFoundWorkaroundTest::testClassIsMissing
    public void testClassIsMissing() {}

// com.fasterxml.jackson.databind.introspect.NoClassDefFoundWorkaroundTest::testDeserialize
    public void testDeserialize() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Parent result = null;

        try {
            result = m.readValue(" { } ", Parent.class);
        } catch (Exception e) {
            fail("Should not have had issues, got: "+e);
        }
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.introspect.NoClassDefFoundWorkaroundTest::testUseMissingClass
    public void testUseMissingClass() {}

// com.fasterxml.jackson.databind.introspect.SetterConflictTest::testSetterPriority
    public void testSetterPriority() throws Exception
    {
        Issue1033Bean bean = MAPPER.readValue(aposToQuotes("{'value':42}"),
                Issue1033Bean.class);
        assertEquals(42, bean.value);
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationMerging::testSharedNames
    public void testSharedNames() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"x\":6}", mapper.writeValueAsString(new SharedName(6)));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationMerging::testSharedNamesFromGetterToSetter
    public void testSharedNamesFromGetterToSetter() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new SharedName2());
        assertEquals("{\"x\":1}", json);
        SharedName2 result = mapper.readValue(json, SharedName2.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationMerging::testSharedTypeInfo
    public void testSharedTypeInfo() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new Wrapper(13L));
        Wrapper result = mapper.readValue(json, Wrapper.class);
        assertEquals(Long.class, result.value.getClass());
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationMerging::testSharedTypeInfoWithCtor
    public void testSharedTypeInfoWithCtor() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new TypeWrapper(13L));
        TypeWrapper result = mapper.readValue(json, TypeWrapper.class);
        assertEquals(Long.class, result.value.getClass());
    }

// com.fasterxml.jackson.databind.introspect.TestAutoDetect::testPrivateCtor
    public void testPrivateCtor() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        PrivateBean bean = m.readValue("\"abc\"", PrivateBean.class);
        assertEquals("abc", bean.a);

        
        m = new ObjectMapper();
        
        VisibilityChecker<?> vc = m.getVisibilityChecker();
        vc = vc.withCreatorVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY);
        m.setVisibility(vc);
        try {
            m.readValue("\"abc\"", PrivateBean.class);
            fail("Expected exception for missing constructor");
        } catch (JsonProcessingException e) {
            verifyException(e, "no single-String constructor/factory");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestInferredMutators::testFinalFieldIgnoral
    public void testFinalFieldIgnoral() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        assertTrue(mapper.isEnabled(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS));
        mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);
        try {
             mapper.readValue("{\"x\":2}", FixedPoint.class);
            fail("Should not try to use final field");
        } catch (JsonMappingException e) {
            verifyException(e, "unrecognized field \"x\"");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestInferredMutators::testDeserializationInference
    public void testDeserializationInference() throws Exception
    {
        final String JSON = "{\"x\":2}";
        ObjectMapper mapper = new ObjectMapper();
        
        assertTrue(mapper.isEnabled(MapperFeature.INFER_PROPERTY_MUTATORS));
        Point p = mapper.readValue(JSON,  Point.class);
        assertEquals(2, p.x);

        
        mapper = new ObjectMapper();
        mapper.disable(MapperFeature.INFER_PROPERTY_MUTATORS);
        try {
            p = mapper.readValue(JSON,  Point.class);
            fail("Should not succeeed");
        } catch (JsonMappingException e) {
            verifyException(e, "unrecognized field \"x\"");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testSerializeDeserializeWithJaxbAnnotations
    public void testSerializeDeserializeWithJaxbAnnotations() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JacksonExample ex = new JacksonExample();
        QName qname = new QName("urn:hi", "hello");
        ex.setQname(qname);
        ex.setAttributeProperty("attributeValue");
        ex.setElementProperty("elementValue");
        ex.setWrappedElementProperty(Arrays.asList("wrappedElementValue"));
        ex.setEnumProperty(EnumExample.VALUE1);
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, ex);
        writer.flush();
        writer.close();

        String json = writer.toString();
        JacksonExample readEx = mapper.readValue(json, JacksonExample.class);

        assertEquals(ex.qname, readEx.qname);
        assertEquals(ex.attributeProperty, readEx.attributeProperty);
        assertEquals(ex.elementProperty, readEx.elementProperty);
        assertEquals(ex.wrappedElementProperty, readEx.wrappedElementProperty);
        assertEquals(ex.enumProperty, readEx.enumProperty);
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testJsonTypeResolver
    public void testJsonTypeResolver() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(TypeResolverBean.class, mapper.getSerializationConfig());
        JavaType baseType = TypeFactory.defaultInstance().constructType(TypeResolverBean.class);
        TypeResolverBuilder<?> rb = ai.findTypeResolver(mapper.getDeserializationConfig(), ac, baseType);
        assertNotNull(rb);
        assertSame(DummyBuilder.class, rb.getClass());
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testEnumHandling
    public void testEnumHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new LcEnumIntrospector());
        assertEquals("\"value1\"", mapper.writeValueAsString(EnumExample.VALUE1));
        EnumExample result = mapper.readValue(quote("value1"), EnumExample.class);
        assertEquals(EnumExample.VALUE1, result);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testIssue193
    public void testIssue193() throws Exception
    {
        String json = objectWriter().writeValueAsString(new Bean193(1, 2));
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testNonConflict
    public void testNonConflict() throws Exception
    {
        String json = MAPPER.writeValueAsString(new BogusConflictBean());
        assertEquals(aposToQuotes("{'prop1':2,'prop2':1}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testHypotheticalGetters
    public void testHypotheticalGetters() throws Exception
    {
        String json = objectWriter().writeValueAsString(new MultipleTheoreticalGetters());
        assertEquals(aposToQuotes("{'a':3}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testOverrideName
    public void testOverrideName() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        String json = mapper.writeValueAsString(new CoreBean158());
        assertEquals(aposToQuotes("{'bar':'x'}"), json);

        
        CoreBean158 result = null;
        try {
            result = mapper.readValue(aposToQuotes("{'bar':'y'}"), CoreBean158.class);
        } catch (Exception e) {
            fail("Unexpected failure when reading CoreBean158: "+e);
        }
        assertNotNull(result);
        assertEquals("y", result.bar);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testSimpleGetters
    public void testSimpleGetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        assertEquals("{\"Get-key\":123}", mapper.writeValueAsString(new GetterBean()));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testSimpleSetters
    public void testSimpleSetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        SetterBean bean = mapper.readValue("{\"Set-key\":13}", SetterBean.class);
        assertEquals(13, bean.value);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testSimpleFields
    public void testSimpleFields() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        String json = mapper.writeValueAsString(new FieldBean(999));
        assertEquals("{\"Field-key\":999}", json);

        
        FieldBean result = mapper.readValue(json, FieldBean.class);
        assertEquals(999, result.key);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testCStyleNaming
    public void testCStyleNaming() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new CStyleStrategy());
        String json = mapper.writeValueAsString(new PersonBean("Joe", "Sixpack", 42));
        assertEquals("{\"first_name\":\"Joe\",\"last_name\":\"Sixpack\",\"age\":42}", json);
        
        
        PersonBean result = mapper.readValue(json, PersonBean.class);
        assertEquals("Joe", result.firstName);
        assertEquals("Sixpack", result.lastName);
        assertEquals(42, result.age);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testWithGetterAsSetter
    public void testWithGetterAsSetter() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new CStyleStrategy());
        SetterlessWithValue input = new SetterlessWithValue().add(3);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value_list\":[{\"int_value\":3}]}", json);

        SetterlessWithValue result = mapper.readValue(json, SetterlessWithValue.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(3, result.values.get(0).intValue);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testLowerCase
    public void testLowerCase() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new LcStrategy());

        RenamedCollectionBean result = mapper.readValue("{\"thevalues\":[\"a\"]}",
                RenamedCollectionBean.class);
        assertNotNull(result.getTheValues());
        assertEquals(1, result.getTheValues().size());
        assertEquals("a", result.getTheValues().get(0));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testPerClassAnnotation
    public void testPerClassAnnotation() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new LcStrategy());
        BeanWithPrefixNames input = new BeanWithPrefixNames();
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"Get-a\":3}", json);

        BeanWithPrefixNames output = mapper.readValue("{\"Set-a\":7}",
                BeanWithPrefixNames.class);
        assertEquals(7, output.a);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseStrategyStandAlone
    public void testLowerCaseStrategyStandAlone()
    {
        for (Object[] pair : SNAKE_CASE_NAME_TRANSLATIONS) {
            String translatedJavaName = PropertyNamingStrategy.SNAKE_CASE.nameForField(null, null,
                    (String) pair[0]);
            assertEquals((String) pair[1], translatedJavaName);
        }
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseTranslations
    public void testLowerCaseTranslations() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new PersonBean("Joe", "Sixpack", 42));
        assertEquals("{\"first_name\":\"Joe\",\"last_name\":\"Sixpack\",\"age\":42}", json);
        
        
        PersonBean result = _lcWithUndescoreMapper.readValue(json, PersonBean.class);
        assertEquals("Joe", result.firstName);
        assertEquals("Sixpack", result.lastName);
        assertEquals(42, result.age);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseAcronymsTranslations
    public void testLowerCaseAcronymsTranslations() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new Acronyms("world wide web", "http://jackson.codehaus.org", "/path1/,/path2/"));
        assertEquals("{\"www\":\"world wide web\",\"some_url\":\"http://jackson.codehaus.org\",\"some_uris\":\"/path1/,/path2/\"}", json);
        
        
        Acronyms result = _lcWithUndescoreMapper.readValue(json, Acronyms.class);
        assertEquals("world wide web", result.WWW);
        assertEquals("http://jackson.codehaus.org", result.someURL);
        assertEquals("/path1/,/path2/", result.someURIs);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseOtherNonStandardNamesTranslations
    public void testLowerCaseOtherNonStandardNamesTranslations() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new OtherNonStandardNames("Results", "_User", "___", "$User"));
        assertEquals("{\"results\":\"Results\",\"user\":\"_User\",\"__\":\"___\",\"$_user\":\"$User\"}", json);
        
        
        OtherNonStandardNames result = _lcWithUndescoreMapper.readValue(json, OtherNonStandardNames.class);
        assertEquals("Results", result.Results);
        assertEquals("_User", result._User);
        assertEquals("___", result.___);
        assertEquals("$User", result.$User);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseUnchangedNames
    public void testLowerCaseUnchangedNames() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new UnchangedNames("from_user", "_user", "from$user", "from7user", "_x"));
        assertEquals("{\"from_user\":\"from_user\",\"user\":\"_user\",\"from$user\":\"from$user\",\"from7user\":\"from7user\",\"x\":\"_x\"}", json);
        
        
        UnchangedNames result = _lcWithUndescoreMapper.readValue(json, UnchangedNames.class);
        assertEquals("from_user", result.from_user);
        assertEquals("_user", result._user);
        assertEquals("from$user", result.from$user);
        assertEquals("from7user", result.from7user);
        assertEquals("_x", result._x);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testPascalCaseStandAlone
    public void testPascalCaseStandAlone()
    {
        String translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
    	        (null, null, "userName");
        assertEquals("UserName", translatedJavaName);

        translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
                (null, null, "User");
        assertEquals("User", translatedJavaName);

        translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
                (null, null, "user");
        assertEquals("User", translatedJavaName);
        translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
                (null, null, "x");
        assertEquals("X", translatedJavaName);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testIssue428PascalWithOverrides
    public void testIssue428PascalWithOverrides() throws Exception {

        String json = new ObjectMapper()
                            .setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                            .writeValueAsString(new Bean428());
        
        if (!json.contains(quote("fooBar"))) {
            fail("Should use name 'fooBar', does not: "+json);
        }
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testSimpleLowerCase
    public void testSimpleLowerCase() throws Exception
    {
        final BoringBean input = new BoringBean();
        ObjectMapper m = objectMapper();

        assertEquals(aposToQuotes("{'firstname':'Bob','lastname':'Burger'}"),
                m.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testKebabCaseStrategyStandAlone
    public void testKebabCaseStrategyStandAlone()
    {
        assertEquals("some-value",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "someValue"));
        assertEquals("some-value",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "SomeValue"));
        assertEquals("url",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "URL"));
        assertEquals("url-stuff",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "URLStuff"));
        assertEquals("some-url-stuff",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "SomeURLStuff"));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testSimpleKebabCase
    public void testSimpleKebabCase() throws Exception
    {
        final FirstNameBean input = new FirstNameBean("Bob");
        ObjectMapper m = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

        assertEquals(aposToQuotes("{'first-name':'Bob'}"), m.writeValueAsString(input));

        FirstNameBean result = m.readValue(aposToQuotes("{'first-name':'Billy'}"),
                FirstNameBean.class);
        assertEquals("Billy", result.firstName);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testNamingWithObjectNode
    public void testNamingWithObjectNode() throws Exception
    {
        ObjectMapper m = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
        ClassWithObjectNodeField result =
            m.readValue(
                "{ \"id\": \"1\", \"json\": { \"foo\": \"bar\", \"baz\": \"bing\" } }",
                ClassWithObjectNodeField.class);
        assertNotNull(result);
        assertEquals("1", result.id);
        assertNotNull(result.json);
        assertEquals(2, result.json.size());
        assertEquals("bing", result.json.path("baz").asText());
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testExplicitRename
    public void testExplicitRename() throws Exception
    {
      ObjectMapper m = new ObjectMapper();
      m.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      m.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
      
      assertEquals(aposToQuotes("{'firstName':'Peter','lastName':'Venkman','user_age':'35'}"),
          m.writeValueAsString(new ExplicitBean()));

      m = new ObjectMapper();
      m.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      m.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
      m.enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING);
      
      assertEquals(aposToQuotes("{'first_name':'Peter','last_name':'Venkman','user_age':'35'}"),
          m.writeValueAsString(new ExplicitBean()));

      
      ExplicitBean bean =
          m.readValue(aposToQuotes("{'first_name':'Egon','last_name':'Spengler','user_age':'32'}"),
              ExplicitBean.class);

      assertNotNull(bean);
      assertEquals("Egon", bean.userFirstName);
      assertEquals("Spengler", bean.userLastName);
      assertEquals("32", bean.userAge);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testExplicitNoNaming
    public void testExplicitNoNaming() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        String json = mapper.writeValueAsString(new DefaultNaming());
        assertEquals(aposToQuotes("{'someValue':3}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testFailWithDupProps
    public void testFailWithDupProps() throws Exception
    {
        BeanWithConflict bean = new BeanWithConflict();
        try {
            String json = objectWriter().writeValueAsString(bean);
            fail("Should have failed due to conflicting accessor definitions; got JSON = "+json);
        } catch (JsonProcessingException e) {
            verifyException(e, "Conflicting getter definitions");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testRegularAndIsGetter
    public void testRegularAndIsGetter() throws Exception
    {
        final ObjectWriter writer = objectWriter();
        
        
        assertEquals("{\"value\":4}", writer.writeValueAsString(new Getters1A()));
        assertEquals("{\"value\":4}", writer.writeValueAsString(new Getters1B()));

        
        ObjectMapper mapper = objectMapper();
        assertEquals(1, mapper.readValue("{\"value\":1}", Getters1A.class).value);
        assertEquals(2, mapper.readValue("{\"value\":2}", Getters1B.class).value);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testInferredNameConflictsWithGetters
    public void testInferredNameConflictsWithGetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InferingIntrospector());
        String json = mapper.writeValueAsString(new Infernal());
        assertEquals(aposToQuotes("{'name':'Bob'}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testInferredNameConflictsWithSetters
    public void testInferredNameConflictsWithSetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InferingIntrospector());
        Infernal inf = mapper.readValue(aposToQuotes("{'stuff':'Bob'}"), Infernal.class);
        assertNotNull(inf);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testIssue541
    public void testIssue541() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS,
                MapperFeature.USE_GETTERS_AS_SETTERS
        );
        Bean541 data = mapper.readValue("{\"str\":\"the string\"}", Bean541.class);
        if (data == null) {
            throw new IllegalStateException("data is null");
        }
        if (!"the string".equals(data.getStr())) {
            throw new IllegalStateException("bad value for data.str");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testValProperty
    public void testValProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"val\"}", m.writeValueAsString(new ValProperty("val")));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testValWithBeanProperty
    public void testValWithBeanProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"val\"}", m.writeValueAsString(new ValWithBeanProperty("val")));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testVarProperty
    public void testVarProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"var\"}", m.writeValueAsString(new VarProperty("var")));
        VarProperty result = m.readValue("{\"prop\":\"read\"}", VarProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testVarWithBeanProperty
    public void testVarWithBeanProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"var\"}", m.writeValueAsString(new VarWithBeanProperty("var")));
        VarWithBeanProperty result = m.readValue("{\"prop\":\"read\"}", VarWithBeanProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testGetterSetterProperty
    public void testGetterSetterProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"get/set\"}", m.writeValueAsString(new GetterSetterProperty()));
        GetterSetterProperty result = m.readValue("{\"prop\":\"read\"}", GetterSetterProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.jsonschema.TestReadJsonSchema::testDeserializeSimple
    public void testDeserializeSimple() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchema schema = mapper.generateJsonSchema(Schemable.class);
        assertNotNull(schema);

        String schemaStr = mapper.writeValueAsString(schema);
        assertNotNull(schemaStr);
        JsonSchema result = mapper.readValue(schemaStr, JsonSchema.class);
        assertEquals("Trying to read from '"+schemaStr+"'", schema, result);
    }

// com.fasterxml.jackson.databind.jsontype.Generic1128Test::testIssue1128
    public void testIssue1128() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        final DevMContainer devMContainer1 = new DevMContainer();
        final DevM entity = new DevM();
        final Dev parent = new Dev();
        parent.id = 2L;
        entity.parent = parent;
        devMContainer1.entity = entity;
    
        String json = mapper.writeValueAsString(devMContainer1);

        final DevMContainer devMContainer = mapper.readValue(json, DevMContainer.class);
        long id = devMContainer.entity.parent.id;

        assertEquals(2, id);
    }

// com.fasterxml.jackson.databind.jsontype.TestAbstractContainers::testAbstractLists
    public void testAbstractLists() throws Exception
    {
        ListWrapper w = new ListWrapper();
        w.list.add("x");

        String json = MAPPER.writeValueAsString(w);
        Object o = MAPPER.readValue(json, ListWrapper.class);
        assertEquals(ListWrapper.class, o.getClass());
        ListWrapper out = (ListWrapper) o;
        assertNotNull(out.list);
        assertEquals(1, out.list.size());
        assertEquals("x", out.list.get(0));
   }
