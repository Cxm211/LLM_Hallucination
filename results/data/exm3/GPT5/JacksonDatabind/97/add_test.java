// com/fasterxml/jackson/databind/node/POJONodeTest.java::testPOJONodeCustomSerInArray
public void testPOJONodeCustomSerInArray() throws Exception
    {
      Data data = new Data();
      data.aStr = "Hello";

      ArrayNode arr = MAPPER.createArrayNode();
      arr.addPOJO(data);

      final String EXP = "[{\"aStr\":\"The value is: Hello!\"}]";

      String out = MAPPER.writer().withAttribute("myAttr", "Hello!").writeValueAsString(arr);
      assertEquals(EXP, out);
    }