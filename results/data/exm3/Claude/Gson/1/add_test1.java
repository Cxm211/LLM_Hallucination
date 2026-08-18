// com/google/gson/functional/TypeVariableTest.java
public void testMultipleTypeParameters() throws Exception {
    Gson gson = new Gson();
    
    // Test with multiple type parameters
    Pair<String, Integer> pair = new Pair<String, Integer>();
    pair.first = "test";
    pair.second = 42;
    
    String json = gson.toJson(pair);
    
    Type pairType = new TypeToken<Pair<String, Integer>>(){}.getType();
    Pair<String, Integer> result = gson.fromJson(json, pairType);
    
    assertEquals("test", result.first);
    assertEquals(Integer.valueOf(42), result.second);
  }