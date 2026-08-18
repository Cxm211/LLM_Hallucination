// com/fasterxml/jackson/databind/deser/exc/ExceptionPathTest.java
public void testReferenceChainForTopLevelClass() throws Exception
{
    class TopLevel {
        public String value;
    }
    String json = "{\"value\":123}";
    try {
        MAPPER.readValue(json, TopLevel.class);
        fail("Should not pass");
    } catch (JsonMappingException e) {
        JsonMappingException.Reference reference = e.getPath().get(0);
        String desc = reference.toString();
        assertTrue("Description should contain $TopLevel: " + desc, desc.contains("$TopLevel"));
    }
}