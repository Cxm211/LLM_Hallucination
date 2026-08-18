public void applyAlias() {
      Node aliasDefinition = aliasVar.getInitialValue();
      String aliasName = aliasVar.getName();
      String typeName = aliasReference.getString();
      String aliasExpanded;
      if (aliasDefinition.isQualifiedName()) {
        Node current = aliasDefinition;
        StringBuilder expanded = new StringBuilder();
        while (current != null) {
          if (current.isName()) {
            String name = current.getString();
            if (aliases.containsKey(name)) {
              Var aliasedVar = aliases.get(name);
              Node aliasedDef = aliasedVar.getInitialValue();
              if (aliasedDef != null && aliasedDef.isQualifiedName()) {
                expanded.insert(0, aliasedDef.getQualifiedName());
              } else {
                expanded.insert(0, name);
              }
            } else {
              expanded.insert(0, name);
            }
            break;
          } else if (current.isGetProp()) {
            String propName = current.getLastChild().getString();
            if (expanded.length() > 0) {
              expanded.insert(0, ".");
            }
            expanded.insert(0, propName);
            current = current.getFirstChild();
          } else {
            break;
          }
        }
        aliasExpanded = expanded.toString();
      } else {
        aliasExpanded = Preconditions.checkNotNull(aliasDefinition.getQualifiedName());
      }
      Preconditions.checkState(typeName.startsWith(aliasName));
      String replacement =
          aliasExpanded + typeName.substring(aliasName.length());
      aliasReference.setString(replacement);

    }