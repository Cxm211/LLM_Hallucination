// com/google/javascript/jscomp/TypedScopeCreatorTest.java
public void testGlobalQualifiedNameInNestedLocalScope() {
    testSame(
        "var obj = {}; " +
        "(function() { " +
        "    (function() { " +
        "        /** @type {string} */ obj.prop = 'test'; " +
        "    })(); " +
        "})();");
    assertNotNull(globalScope.getVar("obj.prop"));
    assertEquals(
        "string",
        globalScope.getVar("obj.prop").getType().toString());
  }