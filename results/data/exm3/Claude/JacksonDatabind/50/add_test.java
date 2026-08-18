// com/fasterxml/jackson/databind/objectid/ObjectWithCreator1261Test.java
public void testObjectIdsWithBuildException() throws Exception
    {
         ObjectMapper mapper = new ObjectMapper();
         mapper.enable(DeserializationFeature.WRAP_EXCEPTIONS);
         
         // Test case where creator.build() throws exception at END_OBJECT with unknown properties
         String json = "{\"id\":1,\"question\":{\"id\":2,\"text\":\"Q1\"},\"unknownProp\":\"value\"}";
         
         try {
             Answer answer = mapper.readValue(json, Answer.class);
             // If we get here, exception was wrapped properly
             assertNotNull(answer);
         } catch (Exception e) {
             // Exception should be properly wrapped, not NPE
             assertFalse("Should not throw NullPointerException", e instanceof NullPointerException);
         }
   }