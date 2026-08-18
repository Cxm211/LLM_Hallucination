// com/google/gson/regression/JsonAdapterNullSafeTest.java
public void testTypeAdapterFactoryWithNullSafe() throws Exception {
    Gson customGson = new GsonBuilder()
        .registerTypeAdapter(Device.class, new TypeAdapterFactory() {
          @Override
          public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() != Device.class) return null;
            return (TypeAdapter<T>) new TypeAdapter<Device>() {
              @Override
              public void write(JsonWriter out, Device value) throws IOException {
                out.beginObject();
                out.name("id").value(value.id);
                out.endObject();
              }
              @Override
              public Device read(JsonReader in) throws IOException {
                in.beginObject();
                String id = null;
                while (in.hasNext()) {
                  if (in.nextName().equals("id")) {
                    id = in.nextString();
                  }
                }
                in.endObject();
                return new Device(id);
              }
            };
          }
        })
        .create();
    Device device = customGson.fromJson("{\"id\":\"test123\"}", Device.class);
    assertEquals("test123", device.id);
  }