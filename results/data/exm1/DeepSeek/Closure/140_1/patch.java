  public void init(JSSourceFile[] externs, JSModule[] modules,
      CompilerOptions options) {
    initOptions(options);

    checkFirstModule(modules);
    if (modules.length == 0 || (modules.length > 0 && modules[0].getInputs().isEmpty())) {
      return;
    }

    this.externs = makeCompilerInput(externs, true);
    this.modules = modules;
    // Generate the module graph, and report any errors in the module
    // specification as errors.
    try {
      this.moduleGraph = new JSModuleGraph(modules);
    } catch (JSModuleGraph.ModuleDependenceException e) {
      // problems with the module format.  Report as an error.  The
      // message gives all details.
      report(JSError.make(MODULE_DEPENDENCY_ERROR,
          e.getModule().getName(), e.getDependentModule().getName()));
      return;
    }
    this.inputs = getAllInputsFromModules();
    initBasedOnOptions();

    initInputsByNameMap();
  }