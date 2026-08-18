// buggy function
  public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    if (n.getType() == Token.FUNCTION) {
      // Don't traverse functions that are constructors or have the @this
      // annotation.
      JSDocInfo jsDoc = getFunctionJsDocInfo(n);
      if (jsDoc != null && (jsDoc.isConstructor() || jsDoc.hasThisType())) {
        return false;
      }

      // Don't traverse functions unless they would normally
      // be able to have a @this annotation associated with them. e.g.,
      // var a = function() { }; // or
      // function a() {} // or
      // a.x = function() {};
    }

    if (parent != null && parent.getType() == Token.ASSIGN) {
      Node lhs = parent.getFirstChild();
      Node rhs = lhs.getNext();
      
      if (n == lhs) {
        // Always traverse the left side of the assignment. To handle
        // nested assignments properly (e.g., (a = this).property = c;),
        // assignLhsChild should not be overridden.
        if (assignLhsChild == null) {
          assignLhsChild = lhs;
        }
      } else {
        // Only traverse the right side if it's not an assignment to a prototype
        // property or subproperty.
        if (lhs.getType() == Token.GETPROP) {
          if (lhs.getLastChild().getString().equals("prototype")) {
            return false;
          }
          String leftName = lhs.getQualifiedName();
          if (leftName != null && leftName.contains(".prototype.")) {
            return false;
          }
        }
      }
    }

    return true;
  }

  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Always report a THIS on the left side of an assign.
      return true;
    }

    // Also report a THIS with a property access.
    return false;
  }

// trigger testcase
// com/google/javascript/jscomp/CheckGlobalThisTest.java::testGlobalThis7
public void testGlobalThis7() {
    testFailure("var a = this.foo;");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testInnerFunction1
public void testInnerFunction1() {
    testFailure("function f() { function g() { return this.x; } }");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testInnerFunction2
public void testInnerFunction2() {
    testFailure("function f() { var g = function() { return this.x; } }");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testInnerFunction3
public void testInnerFunction3() {
    testFailure(
        "function f() { var x = {}; x.y = function() { return this.x; } }");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testStaticFunction6
public void testStaticFunction6() {
    testSame("function a() { return function() { this = 8; } }");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testStaticFunction7
public void testStaticFunction7() {
    testSame("var a = function() { return function() { this = 8; } }");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testStaticFunction8
public void testStaticFunction8() {
    testFailure("var a = function() { return this.foo; };");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testStaticMethod2
public void testStaticMethod2() {
    testSame("a.b = function() { return function() { this.m2 = 5; } }");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testStaticMethod3
public void testStaticMethod3() {
    testSame("a.b.c = function() { return function() { this.m2 = 5; } }");
  }
