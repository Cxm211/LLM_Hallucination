public static <T1> TypeAdapterFactory newTypeHierarchyFactory(
      final Class<T1> clazz, final TypeAdapter<T1> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      public <T2> TypeAdapter<T2> create(final Gson gson, final TypeToken<T2> typeToken) {
        final Class<? super T2> requestedType = typeToken.getRawType();
        if (!clazz.isAssignableFrom(requestedType)) {
          return null;
        }
        return (TypeAdapter<T2>) new TypeAdapter<T1>() {
          @Override public void write(JsonWriter out, T1 value) throws IOException {
            if (value == null) {
              out.nullValue();
              return;
            }
            Class<?> valueType = value.getClass();
            if (valueType != requestedType) {
              TypeAdapter<T1> chosen = (TypeAdapter<T1>) gson.getAdapter(valueType);
              if (chosen instanceof com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter) {
                if (!(typeAdapter instanceof com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter)) {
                  chosen = typeAdapter;
                }
              }
              chosen.write(out, value);
              return;
            }
            typeAdapter.write(out, value);
          }
          @Override public T1 read(JsonReader in) throws IOException {
            T1 result = typeAdapter.read(in);
            if (result != null && !requestedType.isInstance(result)) {
              throw new JsonSyntaxException("Expected a " + requestedType.getName() + " but was " + result.getClass().getName());
            }
            return result;
          }
        };

      }
      @Override public String toString() {
        return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }