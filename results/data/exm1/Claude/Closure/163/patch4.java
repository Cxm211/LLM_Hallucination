private void addSymbolUse(String name, JSModule module, SymbolType type) {
  NameInfo info = getNameInfoForName(name, type);
  NameInfo def = null;
  for (int i = symbolStack.size() - 1; i >= 0; i--) {
    def = symbolStack.get(i).name;
    if (def != anonymousNode) {
      break;
    }
  }
  if (def != null && !def.equals(info)) {
    symbolGraph.connect(def, module, info);
  }
}