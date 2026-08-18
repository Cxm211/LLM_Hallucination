private void findAliases(NodeTraversal t) {
      Scope scope = t.getScope();
      for (Var v : scope.getVarIterable()) {
        Node n = v.getNode();
        Node parent = n.getParent();
        boolean isVar = parent.isVar();
        if (isVar && n.getFirstChild() != null && n.getFirstChild().isQualifiedName()) {
          recordAlias(v);
        } else if (v.isBleedingFunction()) {
          // Bleeding functions already get a BAD_PARAMETERS error, so just
          // do nothing.
        } else if (parent.getType() == Token.LP) {
          // Parameters of the scope function also get a BAD_PARAMETERS
          // error.
        } else if (isVar) {
          Node value = n.hasChildren() ? v.getInitialValue() : null;
          // Only transform if the initial value is a function expression
          if (value != null && value.isFunction()) {
            Node detachedValue = value.detachFromParent();
            Node grandparent = parent.getParent();
            String name = n.getString();
            int nameCount = scopedAliasNames.count(name);
            scopedAliasNames.add(name);
            String globalName =
                "$jscomp.scope." + name + (nameCount == 0 ? "" : ("$" + nameCount));
            compiler.ensureLibraryInjected("base");
            if (detachedValue != null || v.getJSDocInfo() != null) {
              Node newDecl = NodeUtil.newQualifiedNameNodeDeclaration(
                  compiler.getCodingConvention(),
                  globalName,
                  detachedValue,
                  v.getJSDocInfo())
                  .useSourceInfoIfMissingFromForTree(n);
              NodeUtil.setDebugInformation(
                  newDecl.getFirstChild().getFirstChild(), n, name);
              grandparent.addChildBefore(newDecl, varNode);
            }
            v.getNameNode().addChildToFront(
                NodeUtil.newQualifiedNameNode(
                    compiler.getCodingConvention(), globalName, n, name));
            recordAlias(v);
          } else {
            // For non-function initial values, just record the alias
            recordAlias(v);
          }
        } else {
          // Do not other kinds of local symbols, like catch params.
          report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
        }
      }
    }