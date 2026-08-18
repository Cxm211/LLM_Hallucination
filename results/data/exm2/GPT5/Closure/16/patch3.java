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
          if (aliasedNode != null) {
            String aliasedQname = aliasedNode.getQualifiedName();
            if (aliasedQname != null) {
              int dot = aliasedQname.indexOf('.');
              String head = dot == -1 ? aliasedQname : aliasedQname.substring(0, dot);
              Var headVar = aliases.get(head);
              while (headVar != null) {
                Node headInit = headVar.getInitialValue();
                String headQ = headInit != null ? headInit.getQualifiedName() : null;
                if (headQ == null) {
                  break;
                }
                String rest = dot == -1 ? "" : aliasedQname.substring(dot);
                aliasedQname = headQ + rest;
                dot = aliasedQname.indexOf('.');
                head = dot == -1 ? aliasedQname : aliasedQname.substring(0, dot);
                headVar = aliases.get(head);
              }
              aliasUsages.add(new AliasedTypeNode(typeNode, aliasedQname + name.substring(endIndex)));
            }
          }
        }
      }

      for (Node child = typeNode.getFirstChild(); child != null;
           child = child.getNext()) {
        fixTypeNode(child);
      }
    }