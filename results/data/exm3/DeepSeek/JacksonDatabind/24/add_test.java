// com/fasterxml/jackson/databind/ser/TestConfig.java
public void testDateFormatWithNullTimeZone() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TimeZone tz = TimeZone.getTimeZone(\"GMT\");
        mapper.setTimeZone(tz);
        DateFormat df = new NullTimeZoneDateFormat();
        mapper.setDateFormat(df);
        assertEquals(tz, mapper.getSerializationConfig().getTimeZone());
        assertEquals(tz, mapper.getDeserializationConfig().getTimeZone());
        assertEquals(tz, mapper.writer().getConfig().getTimeZone());
        assertEquals(tz, mapper.reader().getConfig().getTimeZone());
    }
    
    static class NullTimeZoneDateFormat extends DateFormat {
        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            return toAppendTo.append(date.toString());
        }
        @Override
        public Date parse(String source, ParsePosition pos) {
            pos.setIndex(source.length());
            return new Date();
        }
        @Override
        public TimeZone getTimeZone() {
            return null;
        }
    }
