// ===== FIXED com.google.javascript.jscomp.DevirtualizePrototypeMethods :: replaceReferencesToThis(Node, String) [lines 354-368] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-135-fixed/src/com/google/javascript/jscomp/DevirtualizePrototypeMethods.java =====
  private void replaceReferencesToThis(Node node, String name) {
    if (NodeUtil.isFunction(node)) {
      return;
    }

    for (Node child : node.children()) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        newName.setJSType(child.getJSType());
        node.replaceChild(child, newName);
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }

// ===== FIXED com.google.javascript.rhino.jstype.FunctionType :: defineProperty(String, JSType, boolean, boolean) [lines 471-487] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-135-fixed/src/com/google/javascript/rhino/jstype/FunctionType.java =====
  boolean defineProperty(String name, JSType type,
      boolean inferred, boolean inExterns) {
    if ("prototype".equals(name)) {
      ObjectType objType = type.toObjectType();
      if (objType != null) {
        if (objType.equals(prototype)) {
          return true;
        }
        return setPrototype(
            new FunctionPrototypeType(
                registry, this, objType, isNativeObjectType()));
      } else {
        return false;
      }
    }
    return super.defineProperty(name, type, inferred, inExterns);
  }

// ===== FIXED com.google.javascript.rhino.jstype.FunctionType :: hasProperty(String) [lines 408-410] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-135-fixed/src/com/google/javascript/rhino/jstype/FunctionType.java =====
  public boolean hasProperty(String name) {
    return super.hasProperty(name) || "prototype".equals(name);
  }
