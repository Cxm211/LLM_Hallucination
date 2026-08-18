// buggy function
  public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    if (n.getType() == Token.FUNCTION) {
      // Don't traverse functions that are constructors or have the @this
      // or @override annotation.
      JSDocInfo jsDoc = getFunctionJsDocInfo(n);
      if (jsDoc != null &&
          (jsDoc.isConstructor() ||
           jsDoc.hasThisType() ||
           jsDoc.isOverride())) {
        return false;
      }

      // Don't traverse functions unless they would normally
      // be able to have a @this annotation associated with them. e.g.,
      // var a = function() { }; // or
      // function a() {} // or
      // a.x = function() {};
      int pType = parent.getType();
      if (!(pType == Token.BLOCK ||
            pType == Token.SCRIPT ||
            pType == Token.NAME ||
            pType == Token.ASSIGN)) {
        return false;
      }
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
          if (lhs.getType() == Token.GETPROP &&
              lhs.getLastChild().getString().equals("prototype")) {
            return false;
          }
          if (lhs.getQualifiedName() != null && lhs.getQualifiedName().contains(".prototype.")) {
            return false;
          }
      }
    }

    return true;
  }

// trigger testcase
// com/google/javascript/jscomp/CheckGlobalThisTest.java::testInterface1
public void testInterface1() {
    testSame(
        "/** @interface */function A() { /** @type {string} */ this.m2; }");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testMethod4
public void testMethod4() {
    testSame("a.prototype['x' + 'y'] =  function() { this.foo = 3; };");
  }

// com/google/javascript/jscomp/CheckGlobalThisTest.java::testPropertyOfMethod
public void testPropertyOfMethod() {
    testFailure("a.protoype.b = {}; " +
        "a.prototype.b.c = function() { this.foo = 3; };");
  }
