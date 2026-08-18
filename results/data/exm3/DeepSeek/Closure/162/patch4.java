    private void fixTypeNode(NodeTraversal t, Node node) {
      if (node == null) return;
      int type = node.getType();
      if (type == Token.NAME) {
        String name = node.getString();
        Var aliasVar = aliases.get(name);
        if (aliasVar != null && t.getScope().getVar(name) == aliasVar) {
          Node aliasedNode = aliasVar.getInitialValue();
          aliasUsages.add(new AliasedNode(node, aliasedNode));
        }
      } else {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
          fixTypeNode(t, child);
        }
      }
    }