// com/google/gson/functional/DefaultTypeAdaptersTest.java
public void testTypeHierarchyFactoryDoesNotMatchSubclass() throws IOException {
    class Base {}
    class Sub extends Base {}
    TypeAdapter<Base> baseAdapter = new TypeAdapter<Base>() {
      @Override public void write(JsonWriter out, Base value) throws IOException {}
      @Override public Base read(JsonReader in) throws IOException { return new Base(); }
    };
    TypeAdapter<Sub> subAdapter = new TypeAdapter<Sub>() {
      @Override public void write(JsonWriter out, Sub value) throws IOException {}
      @Override public Sub read(JsonReader in) throws IOException { return new Sub(); }
    };
    TypeAdapterFactory baseFactory = TypeAdapters.newTypeHierarchyFactory(Base.class, baseAdapter);
    TypeAdapterFactory subFactory = new TypeAdapterFactory() {
      @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() == Sub.class) {
          return (TypeAdapter<T>) subAdapter;
        }
        return null;
      }
    };
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(baseFactory)
        .registerTypeAdapterFactory(subFactory)
        .create();
    Sub result = gson.fromJson("{}", Sub.class);
    assertTrue(result instanceof Sub);
  }
