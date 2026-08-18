private static Collection<Definition> getCallableDefinitions(
      DefinitionProvider definitionProvider, Node name) {
      List<Definition> result = Lists.newArrayList();

      if (!NodeUtil.isGetProp(name) && !NodeUtil.isName(name)) {
        return null;
      }
      Collection<Definition> decls =
          definitionProvider.getDefinitionsReferencedAt(name);
      if (decls == null) {
        return null;
      }

      for (Definition current : decls) {
        Node rValue = current.getRValue();
        if (rValue != null && NodeUtil.isFunction(rValue)) {
          result.add(current);
        } else if (rValue != null && (rValue.isName() || NodeUtil.isName(rValue))) {
          // Follow the alias
          Collection<Definition> innerDecls = definitionProvider.getDefinitionsReferencedAt(rValue);
          if (innerDecls == null) {
            return null;
          }
          for (Definition inner : innerDecls) {
            if (inner.getRValue() != null && NodeUtil.isFunction(inner.getRValue())) {
              result.add(inner);
            } else {
              return null;
            }
          }
        } else {
          return null;
        }
      }

      return result;
  }