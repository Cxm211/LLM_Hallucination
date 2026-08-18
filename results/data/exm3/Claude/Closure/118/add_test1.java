// com/google/javascript/jscomp/DisambiguatePropertiesTest.java
public void testOnlyQuotedPropertiesInObjectLiteral() {
  String js = ""
      + "/** @constructor */ function Foo() {}\n"
      + "Foo.prototype = {'x': 0, 'y': 1};\n"
      + "/** @type Foo */\n"
      + "var F = new Foo;\n"
      + "F['x'] = 0;\n"
      + "F['y'] = 1;";
  String expected = "{}";
  testSets(false, js, js, expected);
  testSets(true, js, js, expected);
}