// com/fasterxml/jackson/databind/struct/TestPOJOAsArray.java::testNullColumn
public void testNullColumnWithCustomNullSerializer() throws Exception {
        assertEquals("[\"N/A\",\"bar\"]", MAPPER.writeValueAsString(new TwoStringsWithNullSer()));
    }
    
    @com.fasterxml.jackson.annotation.JsonPropertyOrder({"a","b"})
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.ARRAY)
    static class TwoStringsWithNullSer {
        @com.fasterxml.jackson.databind.annotation.JsonSerialize(nullsUsing = NASer.class)
        public String a = null;
        public String b = "bar";
    }

    static class NASer extends com.fasterxml.jackson.databind.JsonSerializer<Object> {
        @Override
        public void serialize(Object value, com.fasterxml.jackson.core.JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers) throws java.io.IOException {
            gen.writeString("N/A");
        }
    }