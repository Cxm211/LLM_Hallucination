private void findAliases(NodeTraversal t) {
      Scope scope = t.getScope();
      for (Var v : scope.getVarIterable()) {
        Node n = v.getNode();
        Node parent = n.getParent();
        if (parent != null && parent.isVar()) {
          if (n.hasChildren() && n.getFirstChild().isQualifiedName()) {
            String name = n.getString();
            Var aliasVar = scope.getVar(name);
            if (aliasVar != null) {
              Node initialValue = aliasVar.getInitialValue();
              if (initialValue != null && initialValue.isQualifiedName()) {
                aliases.put(name, aliasVar);
                String qualifiedName = initialValue.getQualifiedName();
                transformation.addAlias(name, qualifiedName);
              } else {
                report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
              }
            } else {
              report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
            }
          } else {
            // TODO(robbyw): Support using locals for private variables.
            report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
          }
        }
      }
    }