// buggy function
    boolean canCollapseUnannotatedChildNames() {
      if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
        return false;
      }

      // Don't try to collapse if the one global set is a twin reference.
      // We could theoretically handle this case in CollapseProperties, but
      // it's probably not worth the effort.

      if (isClassOrEnum) {
        return true;
      }
      return (type == Type.FUNCTION || aliasingGets == 0) &&
          (parent == null || parent.canCollapseUnannotatedChildNames());
    }

  public boolean recordBlockDescription(String description) {
    if (parseDocumentation) {
    populated = true;
    }
    return currentInfo.documentBlock(description);
  }

// trigger testcase
// com/google/javascript/jscomp/CheckSideEffectsTest.java::testJSDocComments
public void testJSDocComments() {
    test("function A() { /** This is a jsdoc comment */ this.foo; }", ok);
    test("function A() { /* This is a normal comment */ this.foo; }", e);
  }

// com/google/javascript/jscomp/CollapsePropertiesTest.java::testCrashInCommaOperator
public void testCrashInCommaOperator() {
    test("var a = {}; a.b = function() {},a.b();",
         "var a$b; a$b=function() {},a$b();");
  }

// com/google/javascript/jscomp/CollapsePropertiesTest.java::testCrashInNestedAssign
public void testCrashInNestedAssign() {
    test("var a = {}; if (a.b = function() {}) a.b();",
         "var a$b; if (a$b=function() {}) { a$b(); }");
  }

// com/google/javascript/jscomp/CollapsePropertiesTest.java::testTwinReferenceCancelsChildCollapsing
public void testTwinReferenceCancelsChildCollapsing() {
    test("var a = {}; if (a.b = function() {}) { a.b.c = 3; a.b(a.b.c); }",
         "var a$b; if (a$b = function() {}) { a$b.c = 3; a$b(a$b.c); }");
  }
