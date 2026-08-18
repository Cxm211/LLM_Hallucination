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
          String expanded = aliasedNode.getQualifiedName();
          String rest = name.substring(endIndex);
          String resolved = expanded + rest;
          while (true) {
            int dotIdx = resolved.indexOf('.');
            String first = dotIdx == -1 ? resolved : resolved.substring(0, dotIdx);
            Var nextAlias = aliases.get(first);
            if (nextAlias == null) {
              break;
            }
            String nextExpanded = nextAlias.getInitialValue().getQualifiedName();
            String nextRest = dotIdx == -1 ? "" : resolved.substring(dotIdx);
            resolved = nextExpanded + nextRest;
          }
          aliasUsages.add(new AliasedTypeNode(typeNode, resolved));
        }
      }

      for (Node child = typeNode.getFirstChild(); child != null;
           child = child.getNext()) {
        fixTypeNode(child);
      }
    }