// com/fasterxml/jackson/databind/node/POJONodeTest.java
@Test
public void testPOJONodeWithJsonSerializable() throws Exception
{
    class CustomSerializable implements JsonSerializable {
        @Override
        public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("custom-serialized");
        }
        
        @Override
        public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
            serialize(gen, serializers);
        }
    }
    
    ObjectNode treeTest = MAPPER.createObjectNode();
    treeTest.putPOJO("data", new CustomSerializable());
    
    String treeOut = MAPPER.writeValueAsString(treeTest);
    assertEquals("{\"data\":\"custom-serialized\"}", treeOut);
}