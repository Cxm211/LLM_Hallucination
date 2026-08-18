// com/google/gson/functional/TypeVariableTest.java
public void testGenericArrayType() throws Exception {
    Gson gson = new Gson();
    class GenericArray<T> {
      T[] array;
    }
    Type type = new TypeToken<GenericArray<String>>(){}.getType();
    GenericArray<String> obj = new GenericArray<>();
    obj.array = new String[]{"a", "b"};
    String json = gson.toJson(obj, type);
    GenericArray<String> obj2 = gson.fromJson(json, type);
    assertArrayEquals(obj.array, obj2.array);
  }
