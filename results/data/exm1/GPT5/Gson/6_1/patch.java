static TypeAdapter<?> getTypeAdapter(ConstructorConstructor constructorConstructor, Gson gson,
      TypeToken<?> fieldType, JsonAdapter annotation) {
    Class<?> value = annotation.value();
    TypeAdapter<?> typeAdapter;
    if (TypeAdapter.class.isAssignableFrom(value)) {
      Class<? extends TypeAdapter<?>> typeAdapterClass = (Class<? extends TypeAdapter<?>>) value;
      typeAdapter = constructorConstructor.get(TypeToken.get((Class) typeAdapterClass)).construct();
      if (annotation.nullSafe()) {
        typeAdapter = typeAdapter.nullSafe();
      }
      return typeAdapter;
    } else if (TypeAdapterFactory.class.isAssignableFrom(value)) {
      Class<? extends TypeAdapterFactory> typeAdapterFactory = (Class<? extends TypeAdapterFactory>) value;
      TypeAdapterFactory factory = constructorConstructor.get(TypeToken.get((Class) typeAdapterFactory))
          .construct();
      typeAdapter = factory.create(gson, fieldType);
      if (typeAdapter != null && annotation.nullSafe()) {
        typeAdapter = typeAdapter.nullSafe();
      }
      return typeAdapter;
    } else if (JsonSerializer.class.isAssignableFrom(value) || JsonDeserializer.class.isAssignableFrom(value)) {
      Object instance = constructorConstructor.get(TypeToken.get((Class) value)).construct();
      JsonSerializer<?> serializer = instance instanceof JsonSerializer ? (JsonSerializer<?>) instance : null;
      JsonDeserializer<?> deserializer = instance instanceof JsonDeserializer ? (JsonDeserializer<?>) instance : null;
      TypeAdapter<?> treeAdapter = new TreeTypeAdapter(serializer, deserializer, gson, fieldType, null);
      if (annotation.nullSafe()) {
        treeAdapter = treeAdapter.nullSafe();
      }
      return treeAdapter;
    } else {
      throw new IllegalArgumentException(
          "@JsonAdapter value must be TypeAdapter, TypeAdapterFactory, JsonSerializer or JsonDeserializer reference.");
    }
  }