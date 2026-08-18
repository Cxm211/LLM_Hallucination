private void findAliases(NodeTraversal t) {
      Scope scope = t.getScope();
      for (Var v : scope.getVarIterable()) {
        Node n = v.getNode();
        Node parent = n.getParent();
        if (parent == null) {
          continue;
        }
        if (parent.isVar()) {
          if (n.hasChildren() && n.getFirstChild().isQualifiedName()) {
            String name = n.getString();
            Var aliasVar = scope.getVar(name);
            aliases.put(name, aliasVar);

            Node init = (aliasVar != null) ? aliasVar.getInitialValue() : null;
            if (init != null) {
              String qualifiedName = init.getQualifiedName();
              transformation.addAlias(name, qualifiedName);
            } else {
              report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
            }
          } else {
            report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
          }
        } else if (parent.isFunction()) {
          // Function declarations are not valid aliases in goog.scope.
          report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
        }
      }
    }