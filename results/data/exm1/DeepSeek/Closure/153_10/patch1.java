public void onRedeclaration(
    Scope s, String name, Node n, Node parent, Node gramps,
    Node nodeWithLineNumber) {
  Preconditions.checkState(n.getType() == Token.NAME);
  Var v = s.getVar(name);

  if (v != null && v.getParentNode().getType() == Token.CATCH) {
    if (parent.getType() == Token.CATCH) {
      // Both are catch variables, rename the new one.
      String newName = name + "$$" + renameCount++;
      n.setString(newName);
      s.declare(newName, n, n.getJSType(), v.input);
    } else {
      // The existing var is a catch variable and the new is not (e.g., var).
      name = MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName(
          name);
      compiler.report(
          JSError.make(
              NodeUtil.getSourceName(nodeWithLineNumber), nodeWithLineNumber,
              CATCH_BLOCK_VAR_ERROR, name));
    }
  } else if (v != null && parent.getType() == Token.FUNCTION) {
    if (v.getParentNode().getType() == Token.VAR) {
      s.undeclare(v);
      s.declare(name, n, n.getJSType(), v.input);
      replaceVarWithAssignment(v.getNameNode(), v.getParentNode(),
          v.getParentNode().getParent());
    }
  } else if (parent.getType() == Token.VAR) {
    Preconditions.checkState(parent.hasOneChild());

    replaceVarWithAssignment(n, parent, gramps);
  } else if (v != null && parent.getType() == Token.CATCH) {
    // Existing var is not catch, new is catch -> rename and declare.
    String newName = name + "$$" + renameCount++;
    n.setString(newName);
    s.declare(newName, n, n.getJSType(), v.input);
  }
}