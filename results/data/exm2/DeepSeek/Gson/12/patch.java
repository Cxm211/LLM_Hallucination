private void expect(JsonToken expected) throws IOException {
  JsonToken peeked = peek();
  if (peeked != expected) {
    throw new IllegalStateException(
        "Expected " + expected + " but was " + peeked + locationString());
  }
}