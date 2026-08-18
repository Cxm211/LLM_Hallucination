// com/google/javascript/jscomp/ExpresssionDecomposerTest.java
public void testCanExposeNewConstantConstructor() {
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "new Foo()", "Foo");
  }
