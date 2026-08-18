public void visit(NodeTraversal t, Node n, Node parent) {
  if (n.isGetProp()) {
    String propName = n.getFirstChild().getNext().getString();

    if (propName.equals("prototype")) {
      processPrototypeParent(t, parent);
    } else if (compiler.getCodingConvention().isExported(propName)) {
      addGlobalUseOfSymbol(propName, t.getModule(), PROPERTY);
    } else {
      addSymbolUse(propName, t.getModule(), PROPERTY);
    }

  } else if (n.isObjectLit() &&
      !(parent.isAssign() &&
        parent.getFirstChild().isGetProp() &&
        parent.getFirstChild().getLastChild().getString().equals(
            "prototype"))) {

    for (Node propNameNode = n.getFirstChild(); propNameNode != null;
         propNameNode = propNameNode.getNext()) {
      if (!propNameNode.isQuotedString()) {
        addSymbolUse(propNameNode.getString(), t.getModule(), PROPERTY);
      }
    }
  } else if (n.isName()) {
    String name = n.getString();

    Var var = t.getScope().getVar(name);
    if (var != null) {
      if (var.isGlobal()) {
        if (var.getInitialValue() != null &&
            var.getInitialValue().isFunction()) {
          if (t.inGlobalScope()) {
            if (!processGlobalFunctionDeclaration(t, n, parent,
                    parent.getParent())) {
              addGlobalUseOfSymbol(name, t.getModule(), VAR);
            }
          } else {
            addSymbolUse(name, t.getModule(), VAR);
          }
        }

      } else if (var.getScope() != t.getScope()){
        for (int i = symbolStack.size() - 1; i >= 0; i--) {
          NameContext context = symbolStack.get(i);
          if (context.scope == var.getScope()) {
            break;
          }

          context.name.readClosureVariables = true;
        }
      }
    }
  }

  if (isPrototypePropertyAssign(n) ||
      isGlobalFunctionDeclaration(t, n) ||
      n.isFunction()) {
    symbolStack.pop();
  }
}