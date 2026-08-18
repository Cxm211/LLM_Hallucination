// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testUndefinedProperty() {
    testLocal("var obj = {a:1}; obj.b;",
              "var obj = {a:1}; obj.b;");
  }
