// com/google/javascript/jscomp/DisambiguatePropertiesTest.java
public void testNumericProperty() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype = {1: 0};\n"
        + "/** @type Foo */\n"
        + "var F = new Foo;\n"
        + "F[1] = 0;";
    String expected = "{}";
    testSets(false, js, js, expected);
    testSets(true, js, js, expected);
  }
