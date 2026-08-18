// com/google/javascript/jscomp/DisambiguatePropertiesTest.java
public void testGetterProperty() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype = {get a() { return 0; }};\n"
        + "/** @type Foo */\n"
        + "var F = new Foo;\n"
        + "F['a'];";
    String expected = "{}";
    testSets(false, js, js, expected);
    testSets(true, js, js, expected);
  }
