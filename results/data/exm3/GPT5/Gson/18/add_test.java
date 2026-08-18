// com/google/gson/functional/CollectionTest.java::testIssue1107
public void testIssue1107Wildcard() {
    String json = "{\n" +
            "  \"inBig\": {\n" +
            "    \"key\": [\n" +
            "      { \"inSmall\": \"hello\" }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
    class BigClass2 { java.util.Map<String, java.util.List<? extends SmallClass>> inBig; }
    BigClass2 big = new Gson().fromJson(json, BigClass2.class);
    SmallClass small = big.inBig.get("key").get(0);
    assertNotNull(small);
    assertEquals("hello", small.inSmall);
  }