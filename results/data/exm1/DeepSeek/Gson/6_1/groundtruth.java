// ===== FIXED com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory :: getTypeAdapter(ConstructorConstructor, Gson, TypeToken, JsonAdapter) [lines 51-71] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Gson/Gson-6-fixed/gson/src/main/java/com/google/gson/internal/bind/JsonAdapterAnnotationTypeAdapterFactory.java =====
  static TypeAdapter<?> getTypeAdapter(ConstructorConstructor constructorConstructor, Gson gson,
      TypeToken<?> fieldType, JsonAdapter annotation) {
    Class<?> value = annotation.value();
    TypeAdapter<?> typeAdapter;
    if (TypeAdapter.class.isAssignableFrom(value)) {
      Class<TypeAdapter<?>> typeAdapterClass = (Class<TypeAdapter<?>>) value;
      typeAdapter = constructorConstructor.get(TypeToken.get(typeAdapterClass)).construct();
    } else if (TypeAdapterFactory.class.isAssignableFrom(value)) {
      Class<TypeAdapterFactory> typeAdapterFactory = (Class<TypeAdapterFactory>) value;
      typeAdapter = constructorConstructor.get(TypeToken.get(typeAdapterFactory))
          .construct()
          .create(gson, fieldType);
    } else {
      throw new IllegalArgumentException(
          "@JsonAdapter value must be TypeAdapter or TypeAdapterFactory reference.");
    }
    if (typeAdapter != null) {
      typeAdapter = typeAdapter.nullSafe();
    }
    return typeAdapter;
  }
