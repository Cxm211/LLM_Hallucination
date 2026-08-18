public void init(JSSourceFile[] externs, JSModule[] modules, CompilerOptions options) {
    initOptions(options);

    if (modules.length == 0) {
      report(JSError.make(EMPTY_MODULE_LIST_ERROR));
      return;
    } else if (modules[0].getInputs().isEmpty()) {
      report(JSError.make(EMPTY_ROOT_MODULE_ERROR, modules[0].getName()));
      return;
    }

    this.externs = makeCompilerInput(externs, true);
    this.modules = modules;
    try {
      this.moduleGraph = new JSModuleGraph(modules);
    } catch (JSModuleGraph.ModuleDependenceException e) {
      report(JSError.make(MODULE_DEPENDENCY_ERROR, e.getModule().getName(), e.getDependentModule().getName()));
      return;
    }
    this.inputs = getAllInputsFromModules();
    initBasedOnOptions();
    initInputsByNameMap();
  }