// com/google/javascript/jscomp/NodeUtilTest.java
public void testLocalValueNewExpressions() throws Exception {
    // NEW expressions are not local unless explicitly marked as such
    assertFalse(testLocalValue("new Object()"));
    assertFalse(testLocalValue("new Array()"));
    assertFalse(testLocalValue("new Array(1, 2, 3)"));
    
    // NEW in complex expressions
    assertFalse(testLocalValue("x = new Y()"));
    assertFalse(testLocalValue("(new X(), 1)"));
    assertFalse(testLocalValue("new X() || 1"));
    assertFalse(testLocalValue("1 || new X()"));
    assertFalse(testLocalValue("new X() && 1"));
    assertFalse(testLocalValue("1 && new X()"));
    assertFalse(testLocalValue("x ? new Y() : 1"));
    assertFalse(testLocalValue("x ? 1 : new Y()"));
  }