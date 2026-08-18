// com/fasterxml/jackson/databind/objectid/Objecid1083Test.java
public void testNarrowingWithDifferentTypeParameters() throws Exception {
          final ObjectMapper mapper = new ObjectMapper();
          JavaType listType = mapper.getTypeFactory().constructParametricType(List.class, String.class);
          JavaType arrayListType = listType.narrowBy(ArrayList.class);
          List<String> result = mapper.readValue("[]", arrayListType);
          assertNotNull(result);
          assertTrue(result.isEmpty());
      }
