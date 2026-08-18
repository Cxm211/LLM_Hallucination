// com/google/javascript/jscomp/ConvertToDottedPropertiesTest.java
public void testIsJSIdentifierAdditional() {
    // Non‑ASCII currency symbol
    testSame("a['\u00A3']");
    // Non‑ASCII letter
    testSame("a['\u00E9']");
    // Ignorable control character (U+0005)
    testSame("a['A\u0005']");
    // Multiple non‑ASCII letters
    testSame("a['\u00C1\u00E9']");
  }
