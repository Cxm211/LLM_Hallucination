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
      String qName = null;
      if (aliasedNode != null) {
        qName = aliasedNode.getQualifiedName();
        if (qName == null) {
          // Manually resolve qualified name through other aliases, e.g., c -> b.c and b -> a.b
          String suffix = "";
          Node current = aliasedNode;
          while (current != null && current.isGetProp()) {
            Node prop = current.getLastChild();
            suffix = "." + prop.getString() + suffix;
            current = current.getFirstChild();
          }
          if (current != null && current.isName()) {
            Var baseAlias = aliases.get(current.getString());
            if (baseAlias != null && baseAlias.getInitialValue() != null) {
              String baseQName = baseAlias.getInitialValue().getQualifiedName();
              if (baseQName != null) {
                qName = baseQName + suffix;
              }
            }
          }
        }
      }
      if (qName != null) {
        aliasUsages.add(new AliasedTypeNode(typeNode, qName + name.substring(endIndex)));
      }
    }
  }

  for (Node child = typeNode.getFirstChild(); child != null;
       child = child.getNext()) {
    fixTypeNode(child);
  }
}