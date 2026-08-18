    public Number read(JsonReader in) throws IOException {
      JsonToken jsonToken = in.peek();
      switch (jsonToken) {
      case NULL:
        in.nextNull();
        return null;
      case NUMBER:
        return new LazilyParsedNumber(in.nextString());
      default:
        throw new JsonSyntaxException("Expecting number, got: " + jsonToken);
      }
    }

// trigger testcase
public void testNumberAsStringDeserialization() {
    Number value = gson.fromJson("\"18\"", Number.class);
    assertEquals(18, value.intValue());
  }
