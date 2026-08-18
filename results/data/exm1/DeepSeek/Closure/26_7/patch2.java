private void visitModuleExports(Node prop) {
      String moduleName = guessCJSModuleName(prop.getSourceFileName());
      if (moduleName == null) {
        return;
      }
      Node module = prop.getChildAtIndex(0);
      module.putProp(Node.ORIGINALNAME_PROP, "module");
      module.setString(moduleName);
      Node exports = prop.getChildAtIndex(1);
      exports.putProp(Node.ORIGINALNAME_PROP, "exports");
      exports.setString("module$exports");
    }