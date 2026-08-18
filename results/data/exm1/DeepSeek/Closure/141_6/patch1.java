private static Collection<Definition> getCallableDefinitions(
      DefinitionProvider definitionProvider, Node name) {
    if (!NodeUtil.isGetProp(name) && !NodeUtil.isName(name)) {
      return null;
    }
    Collection<Definition> decls =
        definitionProvider.getDefinitionsReferencedAt(name);
    if (decls == null || decls.isEmpty()) {
      return null;
    }

    List<Definition> result = Lists.newArrayList();
    for (Definition current : decls) {
      Node rValue = current.getRValue();
      if ((rValue != null) && NodeUtil.isFunction(rValue)) {
        result.add(current);
      } else {
        return null;
      }
    }

    return result.isEmpty() ? null : result;
  }