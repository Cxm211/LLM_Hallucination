// buggy code
  public static <T1> TypeAdapterFactory newTypeHierarchyFactory(
      final Class<T1> clazz, final TypeAdapter<T1> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      public <T2> TypeAdapter<T2> create(Gson gson, TypeToken<T2> typeToken) {
        final Class<? super T2> requestedType = typeToken.getRawType();
        if (!clazz.isAssignableFrom(requestedType)) {
          return null;
        }
        return new TypeAdapter<T2>() {
          @Override public void write(JsonWriter out, T2 value) throws IOException {
            typeAdapter.write(out, (T1) value);
          }
          @Override public T2 read(JsonReader in) throws IOException {
            T1 value = typeAdapter.read(in);
            if (value == null || requestedType.isInstance(value)) {
              return (T2) value;
            }
            // Special handling for Date hierarchy: adapt to requested subtype
            if (value instanceof java.util.Date) {
              long time = ((java.util.Date) value).getTime();
              if (requestedType == java.sql.Date.class) {
                return (T2) new java.sql.Date(time);
              } else if (requestedType == java.sql.Timestamp.class) {
                return (T2) new java.sql.Timestamp(time);
              } else if (requestedType == java.sql.Time.class) {
                return (T2) new java.sql.Time(time);
              }
            }
            return (T2) value;
          }
        };

      }
      @Override public String toString() {
        return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }