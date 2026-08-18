private static Collection<Definition> getCallableDefinitions(
      DefinitionProvider definitionProvider, Node name) {
      List<Definition> result = Lists.newArrayList();

      switch (name.getType()) {
        case Token.NAME:
        case Token.GETPROP: {
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
            if ((rValue != null) && NodeUtil.isFunction(rValue)) {
              result.add(current);
            } else {
              return null;
            }
          }

          return result;
        }
        case Token.HOOK: {
          Collection<Definition> thenDefs =
              getCallableDefinitions(definitionProvider, name.getSecondChild());
          if (thenDefs == null) {
            return null;
          }
          Collection<Definition> elseDefs =
              getCallableDefinitions(definitionProvider, name.getLastChild());
          if (elseDefs == null) {
            return null;
          }
          result.addAll(thenDefs);
          result.addAll(elseDefs);
          return result;
        }
        case Token.AND:
        case Token.OR: {
          Collection<Definition> left =
              getCallableDefinitions(definitionProvider, name.getFirstChild());
          if (left == null) {
            return null;
          }
          Collection<Definition> right =
              getCallableDefinitions(definitionProvider, name.getLastChild());
          if (right == null) {
            return null;
          }
          result.addAll(left);
          result.addAll(right);
          return result;
        }
        default:
          return null;
      }
  }