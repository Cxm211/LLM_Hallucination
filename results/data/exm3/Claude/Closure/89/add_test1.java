// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testAliasCreatedForObjectDepth2WithLocalSetInFunction() {
  test(
      "var a = {}; a.b = {}; a.b.c = 1; var d = a.b; (function(){a.b.e = 2;})(); a.b.c;",
      "var a$b = {}; a$b.c = 1; var d = a$b; (function(){a$b.e = 2;})(); a$b.c;");
}