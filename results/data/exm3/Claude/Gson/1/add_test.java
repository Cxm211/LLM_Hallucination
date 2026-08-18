// com/google/gson/functional/TypeVariableTest.java
public void testNestedTypeVariables() throws Exception {
    Gson gson = new Gson();
    
    // Create a Baz with nested generic structure
    Baz<List<String>> baz = new Baz<List<String>>();
    ArrayList<String> innerList = new ArrayList<String>();
    innerList.add("item1");
    innerList.add("item2");
    baz.value = innerList;
    
    String json = gson.toJson(baz);
    
    // This will fail on buggy version because it doesn't recursively resolve
    // the type variable T which is bound to List<String>
    Type bazType = new TypeToken<Baz<List<String>>>(){}.getType();
    Baz<List<String>> result = gson.fromJson(json, bazType);
    
    assertNotNull(result);
    assertNotNull(result.value);
    assertEquals(2, ((List)result.value).size());
  }