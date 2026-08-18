  private void expect(JsonToken expected) throws IOException {
    if (peek() != expected) {
      throw new IllegalStateException(
          "Expected " + expected + " but was " + peek() + locationString());
    }
  }