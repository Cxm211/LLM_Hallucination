// com/fasterxml/jackson/databind/node/POJONodeTest.java
@Test
public void testPOJONodeWithSimpleValue() throws Exception
{
    ObjectNode treeTest = MAPPER.createObjectNode();
    treeTest.putPOJO("data", "simpleString");
    
    String treeOut = MAPPER.writeValueAsString(treeTest);
    assertEquals("{\"data\":\"simpleString\"}", treeOut);
}