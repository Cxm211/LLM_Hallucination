    public void applyAlias() {
      Node aliasDefinition = aliasVar.getInitialValue();
      String aliasName = aliasVar.getName();
      String typeName = aliasReference.getString();
      String aliasExpanded =
          Preconditions.checkNotNull(aliasDefinition.getQualifiedName());
      // Recursively expand any aliases in the qualified name
      while (true) {
        int dotPos = aliasExpanded.indexOf('.');
        String firstComponent = dotPos == -1 ? aliasExpanded : aliasExpanded.substring(0, dotPos);
        Var firstNameAlias = aliases.get(firstComponent);
        if (firstNameAlias == null) {
          break;
        }
        Node firstNameDef = firstNameAlias.getInitialValue();
        String firstNameExpanded = firstNameDef.getQualifiedName();
        if (firstNameExpanded == null) {
          break;
        }
        String rest = dotPos == -1 ? "" : aliasExpanded.substring(dotPos);
        aliasExpanded = firstNameExpanded + rest;
      }
      Preconditions.checkState(typeName.startsWith(aliasName));
      String replacement =
          aliasExpanded + typeName.substring(aliasName.length());
      aliasReference.setString(replacement);
    }