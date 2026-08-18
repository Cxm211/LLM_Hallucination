// com/google/gson/regression/JsonAdapterNullSafeTest.java
@Test(expected = IllegalArgumentException.class)
  public void testJsonAdapterFactoryReturnsNullThrows() {
    Gson gson = new Gson();
    class NullFactory implements TypeAdapterFactory {
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return null;
      }
    }
    class TestClass {
      @JsonAdapter(NullFactory.class)
      String field;
    }
    gson.toJson(new TestClass());
  }
