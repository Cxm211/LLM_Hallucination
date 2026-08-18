  private void expect(JsonToken expected) throws IOException {
    JsonToken actual = peek();
    if (actual != expected) {
      if (expected == JsonToken.NAME) {
        if (actual == JsonToken.STRING || actual == JsonToken.NUMBER) {
          return;
        }
      }
      throw new IllegalStateException(
          "Expected " + expected + " but was " + peek() + locationString());
    }
  }