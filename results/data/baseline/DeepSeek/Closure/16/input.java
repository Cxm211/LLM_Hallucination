// buggy code
    public void applyAlias() {
      aliasReference.getParent().replaceChild(
          aliasReference, aliasDefinition.cloneTree());
    }

    AliasedTypeNode(Node typeReference,
        String aliasName) {
      this.typeReference = typeReference;
      this.aliasName = aliasName;
    }

    public void applyAlias() {
      typeReference.setString(aliasName);
    }

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
          aliasUsages.add(new AliasedTypeNode(typeNode, aliasedNode.getQualifiedName() + name.substring(endIndex)));
        }
      }

      for (Node child = typeNode.getFirstChild(); child != null;
           child = child.getNext()) {
        fixTypeNode(child);
      }
    }

