static boolean isValidDefineValue(Node val, Set<String> defines) {
    switch (val.getType()) {
      case Token.STRING:
      case Token.NUMBER:
      case Token.TRUE:
      case Token.FALSE:
        return true;

      case Token.BITAND:
      case Token.BITOR:
      case Token.BITXOR:
        return isValidDefineValue(val.getFirstChild(), defines)
            && isValidDefineValue(val.getLastChild(), defines);

      case Token.BITNOT:
      case Token.NOT:
      case Token.NEG:
        return isValidDefineValue(val.getFirstChild(), defines);

      case Token.NAME:
      case Token.GETPROP:
        if (val.isQualifiedName()) {
          return defines.contains(val.getQualifiedName());
        }
    }
    return false;
  }