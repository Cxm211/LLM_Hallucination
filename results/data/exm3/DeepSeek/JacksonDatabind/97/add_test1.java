// com/fasterxml/jackson/databind/node/POJONodeTest.java
public void testPOJONodeCustomSerWithData2() throws Exception {
    class Data2 {
        public int bInt;
    }
    SimpleModule module = new SimpleModule();
    module.addSerializer(Data2.class, new JsonSerializer<Data2>() {
        @Override
        public void serialize(Data2 value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Integer multiplier = (Integer) gen.getAttribute("multiplier");
            if (multiplier == null) multiplier = 1;
            gen.writeStartObject();
            gen.writeFieldName("bInt");
            gen.writeString("value-" + (value.bInt * multiplier));
            gen.writeEndObject();
        }
    });
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(module);
    Data2 data = new Data2();
    data.bInt = 42;
    POJONode node = new POJONode(data);
    String out = mapper.writer().withAttribute("multiplier", 2).writeValueAsString(node);
    assertEquals("{\"bInt\":\"value-84\"}", out);
}
