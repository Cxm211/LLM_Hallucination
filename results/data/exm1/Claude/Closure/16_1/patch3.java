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
          String aliasName = aliasedNode.getQualifiedName();
          if (aliasName != null) {
            aliasUsages.add(new AliasedTypeNode(typeNode, aliasName + name.substring(endIndex)));
          }
        }
      }

      for (Node child = typeNode.getFirstChild(); child != null;
           child = child.getNext()) {
        fixTypeNode(child);
      }
    }