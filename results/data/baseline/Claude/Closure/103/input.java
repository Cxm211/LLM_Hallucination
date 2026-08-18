// buggy code
  private static boolean mayThrowException(Node n) {
    switch (n.getType()) {
      case Token.CALL:
      case Token.GETPROP:
      case Token.GETELEM:
      case Token.THROW:
      case Token.NEW:
      case Token.ASSIGN:
      case Token.INC:
      case Token.DEC:
        return true;
      case Token.FUNCTION:
        return false;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (!ControlFlowGraph.isEnteringNewCfgNode(c) && mayThrowException(c)) {
        return true;
      }
    }
    return false;
  }

    private Set<JSType> getTypesToSkipForTypeNonUnion(JSType type) {
      Set<JSType> types = Sets.newHashSet();
      JSType skipType = type;
      while (skipType != null) {
        types.add(skipType);

        ObjectType objSkipType = skipType.toObjectType();
        if (objSkipType != null) {
          skipType = objSkipType.getImplicitPrototype();
        } else {
          break;
        }
      }
      return types;
    }

