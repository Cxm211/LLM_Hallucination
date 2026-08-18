public void init(JSSourceFile[] externs, JSModule[] modules,
      CompilerOptions options) {
    initOptions(options);

    checkFirstModule(modules);

    this.externs = makeCompilerInput(externs, true);
    // Filter out empty modules when storing
    List<JSModule> nonEmptyModules = new ArrayList<>();
    for (JSModule module : modules) {
      if (!module.getInputs().isEmpty()) {
        nonEmptyModules.add(module);
      }
    }
    this.modules = nonEmptyModules.toArray(new JSModule[0]);
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