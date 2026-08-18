private void attachLiteralTypes(NodeTraversal t, Node n) {
  switch (n.getType()) {
    case Token.NULL:
      n.setJSType(getNativeType(NULL_TYPE));
      break;

    case Token.VOID:
      n.setJSType(getNativeType(VOID_TYPE));
      break;

    case Token.STRING:
      if (!NodeUtil.isObjectLitKey(n, n.getParent())) {
        n.setJSType(getNativeType(STRING_TYPE));
      } else if (n.getParent() != null) {
        Node parent = n.getParent();
        if (!parent.isGetterDef() && !parent.isSetterDef()) {
          n.setJSType(getNativeType(STRING_TYPE));
        }
      }
      break;

    case Token.NUMBER:
      n.setJSType(getNativeType(NUMBER_TYPE));
      break;

    case Token.TRUE:
    case Token.FALSE:
      n.setJSType(getNativeType(BOOLEAN_TYPE));
      break;

    case Token.REGEXP:
      n.setJSType(getNativeType(REGEXP_TYPE));
      break;

    case Token.OBJECTLIT:
      defineObjectLiteral(n);
      break;
  }
}