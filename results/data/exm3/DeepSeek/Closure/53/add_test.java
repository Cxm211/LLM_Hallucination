// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testBug545MultipleEmpty() {
    testLocal("var a = {}, b = {}", "");
  }
