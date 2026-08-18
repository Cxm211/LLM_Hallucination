// com/google/javascript/jscomp/DisambiguatePropertiesTest.java
public void testThreeTypesWithQuotedProperties() {
  String js = ""
      + "/** @constructor */ function Foo() {}\n"
      + "Foo.prototype = {a: 0};\n"
      + "/** @type Foo */\n"
      + "var F = new Foo;\n"
      + "F.a = 0;\n"
      + "/** @constructor */ function Bar() {}\n"
      + "Bar.prototype = {'a': 1};\n"
      + "/** @type Bar */\n"
      + "var B = new Bar;\n"
      + "B['a'] = 1;\n"
      + "/** @constructor */ function Baz() {}\n"
      + "Baz.prototype = {a: 2};\n"
      + "/** @type Baz */\n"
      + "var Z = new Baz;\n"
      + "Z.a = 2;";
  String output = ""
      + "function Foo(){}"
      + "Foo.prototype = {a: 0};"
      + "var F=new Foo;"
      + "F.a=0;"
      + "function Bar(){}"
      + "Bar.prototype = {'a': 1};"
      + "var B=new Bar;"
      + "B['a']=1;"
      + "function Baz(){}"
      + "Baz.prototype = {b: 2};"
      + "var Z=new Baz;"
      + "Z.b=2";
  testSets(false, js, output, "{a=[[Baz.prototype], [Foo.prototype]]}");
  testSets(true, js, output, "{a=[[Baz.prototype], [Foo.prototype]]}");
}