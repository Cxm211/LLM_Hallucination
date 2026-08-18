private void visitModuleExports(Node prop) {
    if (prop == null) {
      return;
    }
    String sourceFileName = prop.getSourceFileName();
    String moduleName = guessCJSModuleName(sourceFileName);
    if (moduleName != null && prop.getChildCount() >= 2) {
      Node module = prop.getChildAtIndex(0);
      module.putProp(Node.ORIGINALNAME_PROP, "module");
      module.setString(moduleName);
      Node exports = prop.getChildAtIndex(1);
      exports.putProp(Node.ORIGINALNAME_PROP, "exports");
      exports.setString("module$exports");
    }
  }