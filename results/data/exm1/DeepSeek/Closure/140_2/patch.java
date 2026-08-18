public void init(JSSourceFile[] externs, JSModule[] modules,
      CompilerOptions options) {
    initOptions(options);
    checkFirstModule(modules);
    this.externs = makeCompilerInput(externs, true);
    this.modules = modules;
    if (modules != null) {
      try {
        this.moduleGraph = new JSModuleGraph(modules);
      } catch (JSModuleGraph.ModuleDependenceException e) {
        report(JSError.make(MODULE_DEPENDENCY_ERROR,
            e.getModule().getName(), e.getDependentModule().getName()));
        return;
      }
      this.inputs = getAllInputsFromModules();
    } else {
      this.inputs = new CompilerInput[0];
    }
    initBasedOnOptions();
    initInputsByNameMap();
  }
