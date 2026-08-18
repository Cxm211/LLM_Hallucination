// com/google/javascript/jscomp/DisambiguatePropertiesTest.java
public void testMixedQuotedAndUnquotedInObjectLiteral() {
  String js = ""
      + "/** @constructor */ function Foo() {}\n"
      + "Foo.prototype = {a: 0, 'b': 1};\n"
      + "/** @type Foo */\n"
      + "var F = new Foo;\n"
      + "F.a = 0;\n"
      + "F['b'] = 1;";
  String output = ""
      + "function Foo(){}"
      + "Foo.prototype = {a: 0, 'b': 1};"
      + "var F=new Foo;"
      + "F.a=0;"
      + "F['b']=1";
  testSets(false, js, output, "{a=[[Foo.prototype]]}");
  testSets(true, js, output, "{a=[[Foo.prototype]]}");
}