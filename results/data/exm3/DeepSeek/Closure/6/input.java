// buggy function
  boolean expectCanAssignToPropertyOf(NodeTraversal t, Node n, JSType rightType,
      JSType leftType, Node owner, String propName) {
    // The NoType check is a hack to make typedefs work OK.
    if (!leftType.isNoType() && !rightType.canAssignTo(leftType)) {
      if ((leftType.isConstructor() || leftType.isEnumType()) && (rightType.isConstructor() || rightType.isEnumType())) {
        registerMismatch(rightType, leftType, null);
      } else {
      // Do not type-check interface methods, because we expect that
      // they will have dummy implementations that do not match the type
      // annotations.
      JSType ownerType = getJSType(owner);
      if (ownerType.isFunctionPrototypeType()) {
        FunctionType ownerFn = ownerType.toObjectType().getOwnerFunction();
        if (ownerFn.isInterface() &&
            rightType.isFunctionType() && leftType.isFunctionType()) {
          return true;
        }
      }

      mismatch(t, n,
          "assignment to property " + propName + " of " +
          getReadableJSTypeName(owner, true),
          rightType, leftType);
      }
      return false;
    }
    return true;
  }

  boolean expectCanAssignTo(NodeTraversal t, Node n, JSType rightType,
      JSType leftType, String msg) {
    if (!rightType.canAssignTo(leftType)) {
      if ((leftType.isConstructor() || leftType.isEnumType()) && (rightType.isConstructor() || rightType.isEnumType())) {
        registerMismatch(rightType, leftType, null);
      } else {
      mismatch(t, n, msg, rightType, leftType);
      }
      return false;
    }
    return true;
  }

// trigger testcase
// com/google/javascript/jscomp/LooseTypeCheckTest.java::testTypeRedefinition
public void testTypeRedefinition() throws Exception {
    testClosureTypesMultipleWarnings(
        "a={};/**@enum {string}*/ a.A = {ZOR:'b'};"
        + "/** @constructor */ a.A = function() {}",
        Lists.newArrayList(
            "variable a.A redefined with type function (new:a.A): undefined, " +
            "original definition at [testcode]:1 with type enum{a.A}",
            "assignment to property A of a\n" +
            "found   : function (new:a.A): undefined\n" +
            "required: enum{a.A}"));
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testIssue635b
public void testIssue635b() throws Exception {
    testTypes(
        "/** @constructor */" +
        "function F() {}" +
        "/** @constructor */" +
        "function G() {}" +
        "/** @type {function(new:G)} */ var x = F;",
        "initializing variable\n" +
        "found   : function (new:F): undefined\n" +
        "required: function (new:G): ?");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testTypeRedefinition
public void testTypeRedefinition() throws Exception {
    testClosureTypesMultipleWarnings("a={};/**@enum {string}*/ a.A = {ZOR:'b'};"
        + "/** @constructor */ a.A = function() {}",
        Lists.newArrayList(
            "variable a.A redefined with type function (new:a.A): undefined, " +
            "original definition at [testcode]:1 with type enum{a.A}",
            "assignment to property A of a\n" +
            "found   : function (new:a.A): undefined\n" +
            "required: enum{a.A}"));
  }
