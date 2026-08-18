// com/fasterxml/jackson/databind/ser/DateSerializationTest.java
public void testDateWithCombinedPatternAndLocale() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // @JsonFormat with both pattern and locale => STRING shape
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        class DateWithPatternAndLocale {
            @JsonFormat(pattern="yyyy-MM-dd", locale="en_US")
            public Date date;
            public DateWithPatternAndLocale(long timestamp) {
                this.date = new Date(timestamp);
            }
        }
        
        String json = mapper.writeValueAsString(new DateWithPatternAndLocale(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01'}"), json);
    }