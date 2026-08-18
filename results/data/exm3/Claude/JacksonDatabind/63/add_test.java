// com/fasterxml/jackson/databind/deser/exc/ExceptionPathTest.java
public void testReferenceChainForNestedInnerClass() throws Exception
{
    class Level1 {
        public Level2 level2;
        class Level2 {
            public String value;
        }
    }
    String json = "{\"level2\":{\"value\":123}}";
    try {
        MAPPER.readValue(json, Level1.class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        JsonMappingException.Reference reference = e.getPath().get(0);
        String desc = reference.toString();
        assertTrue("Description should contain $Level1$Level2: " + desc, desc.contains("$Level1$Level2"));
    }
}