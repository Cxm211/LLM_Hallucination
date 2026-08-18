  private void expect(JsonToken expected) throws IOException {
    JsonToken actual = peek();
    if (actual != expected) {
      throw new IllegalStateException(
          "Expected " + expected + " but was " + actual + locationString());
    }
  }