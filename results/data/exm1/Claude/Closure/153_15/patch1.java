public void onRedeclaration(
        Scope s, String name, Node n, Node parent, Node gramps,
        Node nodeWithLineNumber) {
      Preconditions.checkState(n.getType() == Token.NAME);
      Var v = s.getVar(name);

      if (v != null && v.getParentNode().getType() == Token.CATCH) {
        name = MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName(
            name);
        compiler.report(
            JSError.make(
                NodeUtil.getSourceName(nodeWithLineNumber), nodeWithLineNumber,
                CATCH_BLOCK_VAR_ERROR, name));
      } else if (v != null && parent.getType() == Token.FUNCTION) {
        if (v.getParentNode().getType() == Token.VAR) {
          s.undeclare(v);
          s.declare(name, n, n.getJSType(), v.input);
          replaceVarWithAssignment(v.getNameNode(), v.getParentNode(),
              v.getParentNode().getParent());
        }
      } else if (parent.getType() == Token.VAR) {
        if (v != null && v.getParentNode().getType() == Token.FUNCTION) {
          replaceVarWithAssignment(n, parent, gramps);
        } else {
          Preconditions.checkState(parent.hasOneChild());
          replaceVarWithAssignment(n, parent, gramps);
        }
      }
    }