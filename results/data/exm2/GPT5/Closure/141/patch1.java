  private static Collection<Definition> getCallableDefinitions(
      DefinitionProvider definitionProvider, Node name) {
      List<Definition> result = Lists.newArrayList();

      // Handle compound callee expressions that select among multiple functions.
      switch (name.getType()) {
        case Token.HOOK: {
          Node first = name.getFirstChild();
          Node trueExpr = first.getNext();
          Node falseExpr = trueExpr.getNext();
          Collection<Definition> t = getCallableDefinitions(definitionProvider, trueExpr);
          Collection<Definition> f = getCallableDefinitions(definitionProvider, falseExpr);
          if (t == null || f == null) {
            return null;
          }
          result.addAll(t);
          result.addAll(f);
          return result;
        }
        case Token.OR:
        case Token.AND: {
          Node left = name.getFirstChild();
          Node right = left.getNext();
          Collection<Definition> l = getCallableDefinitions(definitionProvider, left);
          Collection<Definition> r = getCallableDefinitions(definitionProvider, right);
          if (l == null || r == null) {
            return null;
          }
          result.addAll(l);
          result.addAll(r);
          return result;
        }
        default:
          break;
      }

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