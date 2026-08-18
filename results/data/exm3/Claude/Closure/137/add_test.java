// com/google/javascript/jscomp/MakeDeclaredNamesUniqueTest.java
public void testInversionWithNonNumericSuffix() {
    invert = true;
    test(
        "function x1() {" +
        "  var a$$inline;" +
        "  function x2() {" +
        "    var a;" +
        "  }" +
        "}",
        "function x1() {" +
        "  var a$$inline;" +
        "  function x2() {" +
        "    var a;" +
        "  }" +
        "}");
  }