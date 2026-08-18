// com/google/javascript/jscomp/InlineObjectLiteralsTest.java::testObject12
public void testObject12_additional() {
    testLocal("var x; x = {a:1}; f(x.b);", "var x; x = {a:1}; f(x.b);");
  }