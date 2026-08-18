// com/fasterxml/jackson/databind/convert/TestConvertingSerializer.java
public void testIssue731Content() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // Bean with content converter (list elements)
        mapper.addMixIn(BeanWithList.class, BeanWithListMixin.class);
        BeanWithList bean = new BeanWithList();
        bean.values = Arrays.asList(1, 2, 3);
        String json = mapper.writeValueAsString(bean);
        assertEquals("{"values":[2,4,6]}", json);
    }
    static class BeanWithList {
        public List<Integer> values;
    }
    @JsonSerialize(contentConverter = UntypedDoublerConverter.class)
    interface BeanWithListMixin {}
    static class UntypedDoublerConverter extends StdConverter<Integer, Object> {
        @Override
        public Object convert(Integer value) { return value * 2; }
    }
