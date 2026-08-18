// com/fasterxml/jackson/databind/node/POJONodeTest.java
@Test
public void testPOJONodeWithNullValue() throws Exception
{
    ObjectNode treeTest = MAPPER.createObjectNode();
    treeTest.putPOJO("data", null);
    
    String treeOut = MAPPER.writeValueAsString(treeTest);
    assertEquals("{\"data\":null}", treeOut);
}