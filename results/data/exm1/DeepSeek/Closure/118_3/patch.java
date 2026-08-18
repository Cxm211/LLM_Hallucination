private void handleObjectLit(NodeTraversal t, Node n) {
  for (Node child = n.getFirstChild();
      child != null;
      child = child.getNext()) {
    if (!child.isStringKey() && !child.isGetterDef() && !child.isSetterDef()) {
      continue;
    }
    String name;
    Node value;
    if (child.isStringKey()) {
      name = child.getString();
      value = child.getNext();
    } else {
      name = child.getFirstChild().getString();
      value = child.getLastChild();
    }
    T type = typeSystem.getType(getScope(), n, name);
    Property prop = getProperty(name);
    if (!prop.scheduleRenaming(child,
                               processProperty(t, prop, type, value))) {
      if (propertiesToErrorFor.containsKey(name)) {
        compiler.report(JSError.make(
            t.getSourceName(), child, propertiesToErrorFor.get(name),
            Warnings.INVALIDATION, name,
            (type == null ? "null" : type.toString()), n.toString(), ""));
      }
    }
  }
}