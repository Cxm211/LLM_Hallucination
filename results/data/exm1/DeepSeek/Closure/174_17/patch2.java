private void findAliases(NodeTraversal t) {
      Scope scope = t.getScope();
      for (Var v : scope.getVarIterable()) {
        Node n = v.getNode();
        Node parent = n.getParent();
        boolean isVarAssign = parent.isVar() && n.hasChildren();
        if (isVarAssign && n.getFirstChild().isQualifiedName()) {
          recordAlias(v);
          // Remove the var declaration
          detachVar(n, parent);
        } else if (v.isBleedingFunction()) {
          // Bleeding functions already get a BAD_PARAMETERS error, so just
          // do nothing.
        } else if (parent.getType() == Token.LP) {
          // Parameters of the scope function also get a BAD_PARAMETERS
          // error.
        } else if (isVarAssign) {
          Node value = v.getInitialValue().detachFromParent();
          String name = n.getString();
          int nameCount = scopedAliasNames.count(name);
          scopedAliasNames.add(name);
          String globalName =
              "$jscomp.scope." + name + (nameCount == 0 ? "" : ("$" + nameCount));

          compiler.ensureLibraryInjected("base");

          // Add $jscomp.scope.name = EXPR;
          // Make sure we copy over all the jsdoc and debug info.
            Node newDecl = NodeUtil.newQualifiedNameNodeDeclaration(
                compiler.getCodingConvention(),
                globalName,
                value,
                v.getJSDocInfo())
                .useSourceInfoIfMissingFromForTree(n);
            NodeUtil.setDebugInformation(
                newDecl.getFirstChild().getFirstChild(), n, name);
            parent.getParent().addChildBefore(newDecl, parent);

          recordAlias(v);
          // Remove the var declaration
          detachVar(n, parent);
        } else {
          // Do not allow hoisted functions or other kinds of local symbols.
          report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
        }
      }
    }

    private void detachVar(Node n, Node parent) {
      NodeUtil.removeChild(parent, n);
      if (!parent.hasChildren()) {
        parent.detachFromParent();
      }
    }