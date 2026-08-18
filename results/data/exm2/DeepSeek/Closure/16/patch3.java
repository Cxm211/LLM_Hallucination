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
          String aliasedQualifiedName = aliasedNode.getQualifiedName();
          // Expand aliases in the qualified name.
          String expanded = aliasedQualifiedName;
          while (true) {
            int dotPos = expanded.indexOf('.');
            String first = dotPos == -1 ? expanded : expanded.substring(0, dotPos);
            Var nextAlias = aliases.get(first);
            if (nextAlias == null) {
              break;
            }
            String nextQualified = nextAlias.getInitialValue().getQualifiedName();
            String rest = dotPos == -1 ? "" : expanded.substring(dotPos);
            expanded = nextQualified + rest;
          }
          aliasUsages.add(new AliasedTypeNode(typeNode, expanded + name.substring(endIndex)));
        }
      }

      for (Node child = typeNode.getFirstChild(); child != null;
           child = child.getNext()) {
        fixTypeNode(child);
      }
    }