private void fixTypeNode(Node typeNode) {
  if (typeNode.isString()) {
    String name = typeNode.getString();
    int endIndex = name.indexOf('.');
    if (endIndex == -1) {
      endIndex = name.length();
    }
    String baseName = name.substring(0, endIndex);
    Var aliasVar = aliases.get(baseName);
    if (aliasVar != null) {
      Node aliasedNode = aliasVar.getInitialValue();
      String suffix = (endIndex < name.length()) ? name.substring(endIndex) : "";
      String qualifiedName = aliasedNode.getQualifiedName();
      while (aliasedNode.isName() && aliases.containsKey(aliasedNode.getString())) {
        aliasedNode = aliases.get(aliasedNode.getString()).getInitialValue();
        qualifiedName = aliasedNode.getQualifiedName();
      }
      aliasUsages.add(new AliasedTypeNode(typeNode, qualifiedName + suffix));
    }
  }
  for (Node child = typeNode.getFirstChild(); child != null;
       child = child.getNext()) {
    fixTypeNode(child);
  }
}