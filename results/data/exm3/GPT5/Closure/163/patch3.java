public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      // Process prototype assignments to non-functions.
      if (isPrototypePropertyAssign(n)) {
        symbolStack.push(new NameContext(getNameInfoForName(
                n.getFirstChild().getLastChild().getString(), PROPERTY)));
      } else if (isGlobalFunctionDeclaration(t, n)) {
        String name = parent.isName() ?
            parent.getString() /* VAR */ :
            n.getFirstChild().getString() /* named function */;
        symbolStack.push(new NameContext(getNameInfoForName(name, VAR)));
      } else if (n.isFunction()) {
        boolean pushed = false;
        // If this function is a value of an object-literal that's being
        // assigned to a prototype (e.g. Foo.prototype = { a: function(){} })
        // then associate this function with the property name context so
        // closure-capture analysis is attributed to the correct property.
        Node keyNode = n.getParent();
        if (keyNode != null && (keyNode.isStringKey() || keyNode.isGetterDef() || keyNode.isSetterDef())) {
          Node objLit = keyNode.getParent();
          if (objLit != null && objLit.isObjectLit()) {
            Node assign = objLit.getParent();
            if (assign != null && assign.isAssign()) {
              Node lhs = assign.getFirstChild();
              if (lhs != null && lhs.isGetProp()) {
                Node last = lhs.getLastChild();
                if (last != null && last.isString() && "prototype".equals(last.getString())) {
                  String propName = keyNode.getString();
                  symbolStack.push(new NameContext(getNameInfoForName(propName, PROPERTY)));
                  pushed = true;
                }
              }
            }
          }
        }
        if (!pushed) {
          symbolStack.push(new NameContext(anonymousNode));
        }
      }
      return true;
    }