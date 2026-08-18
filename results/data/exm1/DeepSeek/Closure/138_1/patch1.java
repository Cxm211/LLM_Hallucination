private FlowScope traverseName(Node n, FlowScope scope) {
  String varName = n.getString();
  Node value = n.getFirstChild();
  JSType type = n.getJSType();
  if (value != null) {
    scope = traverse(value, scope);
    updateScopeForTypeChange(scope, n, n.getJSType() /* could be null */,
        getJSType(value));
    return scope;
  } else {
    StaticSlot<JSType> var = scope.getSlot(varName);
    if (var != null && !var.isTypeInferred()) {
      type = var.getType();
      if (type == null) {
        type = getNativeType(UNKNOWN_TYPE);
      }
    }
  }
  n.setJSType(type);
  return scope;
}