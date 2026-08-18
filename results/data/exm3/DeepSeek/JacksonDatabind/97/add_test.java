// com/fasterxml/jackson/databind/node/POJONodeTest.java
public void testPOJONodeCustomSerWithIntArray() throws Exception {
    SimpleModule module = new SimpleModule();
    module.addSerializer(int[].class, new JsonSerializer<int[]>() {
        @Override
        public void serialize(int[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Integer factor = (Integer) gen.getAttribute("factor");
            if (factor == null) factor = 1;
            gen.writeStartArray();
            for (int i : value) {
                gen.writeNumber(i * factor);
            }
            gen.writeEndArray();
        }
    });
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(module);
    int[] arr = new int[]{1,2,3};
    POJONode node = new POJONode(arr);
    String out = mapper.writer().withAttribute("factor", 10).writeValueAsString(node);
    assertEquals("[10,20,30]", out);
}
