// com/fasterxml/jackson/databind/ser/DateSerializationTest.java::testDateExplicitNumericShape
public void testDateExplicitNumericShape() throws Exception {
        class DateAsNumericBean {
            @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.NUMBER)
            public java.util.Date date;
            public DateAsNumericBean(long ts) { this.date = new java.util.Date(ts); }
        }
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = mapper.writeValueAsString(new DateAsNumericBean(0L));
        assertEquals(aposToQuotes("{'date':0}"), json);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsNumericBean(0L));
        assertEquals(aposToQuotes("{'date':0}"), json);
    }