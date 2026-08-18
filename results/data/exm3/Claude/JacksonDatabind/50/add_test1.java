// com/fasterxml/jackson/databind/objectid/ObjectWithCreator1261Test.java
public void testObjectIdsMinimalProperties() throws Exception
    {
         ObjectMapper mapper = new ObjectMapper();
         
         // Test with minimal creator properties only (no unknown properties)
         String json = "{\"id\":1,\"question\":{\"id\":2,\"text\":\"Q1\"}}";
         
         Answer answer = mapper.readValue(json, Answer.class);
         assertNotNull(answer);
         assertEquals(1, answer.id);
         assertNotNull(answer.question);
         assertEquals(2, answer.question.id);
   }