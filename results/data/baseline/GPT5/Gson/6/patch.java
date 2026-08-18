  static TypeAdapter<?> getTypeAdapter(ConstructorConstructor constructorConstructor, Gson gson,
      TypeToken<?> fieldType, JsonAdapter annotation) {
    Class<?> value = annotation.value();
    Object instance = constructorConstructor.get(TypeToken.get(value)).construct();
    TypeAdapter<?> typeAdapter;
    if (instance instanceof TypeAdapter) {
      typeAdapter = (TypeAdapter<?>) instance;
    } else if (instance instanceof TypeAdapterFactory) {
      typeAdapter = ((TypeAdapterFactory) instance).create(gson, fieldType);
    } else if (instance instanceof JsonSerializer || instance instanceof JsonDeserializer) {
      JsonSerializer<?> serializer = instance instanceof JsonSerializer ? (JsonSerializer<?>) instance : null;
      JsonDeserializer<?> deserializer = instance instanceof JsonDeserializer ? (JsonDeserializer<?>) instance : null;
      typeAdapter = new TreeTypeAdapter<>(serializer, deserializer, gson, fieldType, null);
    } else {
      throw new IllegalArgumentException(
          "@JsonAdapter value must be TypeAdapter, TypeAdapterFactory, JsonSerializer or JsonDeserializer reference.");
    }
    if (typeAdapter != null && annotation.nullSafe()) {
      typeAdapter = typeAdapter.nullSafe();
    }
    return typeAdapter;
  }