// buggy function
  private void replaceReferencesToThis(Node node, String name) {
    if (NodeUtil.isFunction(node)) {
      return;
    }

    for (Node child : node.children()) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }

  public boolean hasProperty(String name) {
    return super.hasProperty(name) || "prototype".equals(name);
  }

  boolean defineProperty(String name, JSType type,
      boolean inferred, boolean inExterns) {
    if ("prototype".equals(name)) {
      ObjectType objType = type.toObjectType();
      if (objType != null) {
        return setPrototype(
            new FunctionPrototypeType(
                registry, this, objType, isNativeObjectType()));
      } else {
        return false;
      }
    }
    return super.defineProperty(name, type, inferred, inExterns);
  }

// trigger testcase
// com/google/javascript/jscomp/DevirtualizePrototypeMethodsTest.java::testRewritePrototypeMethods2
public void testRewritePrototypeMethods2() throws Exception {
    // type checking on
    enableTypeCheck(CheckLevel.ERROR);
    checkTypes(RewritePrototypeMethodTestInput.INPUT,
               RewritePrototypeMethodTestInput.EXPECTED,
               RewritePrototypeMethodTestInput.EXPECTED_TYPE_CHECKING_ON);
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testGoodExtends9
public void testGoodExtends9() throws Exception {
    testTypes(
        "/** @constructor */ function Super() {}" +
        "Super.prototype.foo = function() {};" +
        "/** @constructor \n * @extends {Super} */ function Sub() {}" +
        "Sub.prototype = new Super();" +
        "/** @override */ Sub.prototype.foo = function() {};");
  }
