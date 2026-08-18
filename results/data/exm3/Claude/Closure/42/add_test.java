// com/google/javascript/jscomp/parsing/ParserTest.java
public void testForIn() {
  parse(
      "function f() {\n" +
      "  for (var x in obj) {\n" +
      "    console.log(x);\n" +
      "  }\n" +
      "};");
}