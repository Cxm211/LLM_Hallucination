// com/google/gson/functional/PrimitiveTest.java
public void testNumberAsStringDeserializationInvalidString() {
    Gson gson = new Gson();
    Number value = gson.fromJson(\"\\\"abc\\\"\", Number.class);
    try {
        value.intValue();
        fail(\"Expected NumberFormatException\");
    } catch (NumberFormatException e) {
        // expected
    }
  }
