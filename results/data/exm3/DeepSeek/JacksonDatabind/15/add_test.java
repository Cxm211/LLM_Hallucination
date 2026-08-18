// com/fasterxml/jackson/databind/convert/TestConvertingSerializer.java
public void testIssue731Property() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // Bean with property-level untyped converter
        mapper.addMixIn(SimpleBean.class, SimpleBeanMixin.class);
        String json = mapper.writeValueAsString(new SimpleBean(5));
        assertEquals("{"value":10}", json);
    }
    static class SimpleBean {
        public int value;
        public SimpleBean(int v) { value = v; }
    }
    @JsonSerialize(converter = UntypedDoublerConverter.class)
    interface SimpleBeanMixin {}
    static class UntypedDoublerConverter extends StdConverter<Integer, Object> {
        @Override
        public Object convert(Integer value) { return value * 2; }
    }
