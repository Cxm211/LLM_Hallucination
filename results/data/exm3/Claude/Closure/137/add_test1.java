// com/google/javascript/jscomp/MakeDeclaredNamesUniqueTest.java
public void testInversionFirstDeclarationNoSuffix() {
    invert = true;
    test(
        "function x1() {" +
        "  var a;" +
        "  function x2() {" +
        "    var a$$1;" +
        "  }" +
        "}",
        "function x1() {" +
        "  var a;" +
        "  function x2() {" +
        "    var a;" +
        "  }" +
        "}");
  }