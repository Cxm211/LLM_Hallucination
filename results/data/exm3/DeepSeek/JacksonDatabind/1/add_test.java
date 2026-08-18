// com/fasterxml/jackson/databind/struct/TestPOJOAsArray.java
static class CustomNullSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("NULL");
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class CustomNullBean {
        @JsonSerialize(nullsUsing = CustomNullSerializer.class)
        public String a;
        public String b;
    }

    public void testNullColumnWithCustomNullSerializer() throws Exception {
        CustomNullBean bean = new CustomNullBean();
        bean.b = "bar";
        assertEquals("[\"NULL\",\"bar\"]", MAPPER.writeValueAsString(bean));
    }
