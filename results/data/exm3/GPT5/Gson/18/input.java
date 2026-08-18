// buggy function
  static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
      // wildcards are useless for resolving supertypes. As the upper bound has the same raw type, use it instead
    checkArgument(supertype.isAssignableFrom(contextRawType));
    return resolve(context, contextRawType,
        $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
  }

// trigger testcase
// com/google/gson/functional/CollectionTest.java::testIssue1107
public void testIssue1107() {
    String json = "{\n" +
            "  \"inBig\": {\n" +
            "    \"key\": [\n" +
            "      { \"inSmall\": \"hello\" }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
    BigClass bigClass = new Gson().fromJson(json, BigClass.class);
    SmallClass small = bigClass.inBig.get("key").get(0);
    assertNotNull(small);
    assertEquals("hello", small.inSmall);
  }
