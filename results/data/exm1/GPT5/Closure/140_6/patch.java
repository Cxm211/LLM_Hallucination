  public void init(JSSourceFile[] externs, JSModule[] modules,
      CompilerOptions options) {
    initOptions(options);

    if (modules == null) {
      report(JSError.make(EMPTY_MODULE_LIST_ERROR));
      return;
    }

    checkFirstModule(modules);

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

  private void checkFirstModule(JSModule[] modules) {
    if (modules == null || modules.length == 0) {
      report(JSError.make(EMPTY_MODULE_LIST_ERROR));
    } else if (modules[0].getInputs().isEmpty()) {
      report(JSError.make(EMPTY_ROOT_MODULE_ERROR,
          modules[0].getName()));
    }
  }

  Node getNodeForCodeInsertion(JSModule module) {
    if (module == null) {
      if (inputs.length == 0) {
        throw new IllegalStateException("No inputs");
      }

      // Prefer inserting into the first non-extern input when available.
      for (CompilerInput in : inputs) {
        if (!in.isExtern()) {
          return in.getAstRoot(this);
        }
      }
      // Fallback to the first input if all are externs (should be rare).
      return inputs[0].getAstRoot(this);
    }

    List<CompilerInput> inputs = module.getInputs();
    if (inputs.size() > 0) {
      return inputs.get(0).getAstRoot(this);
    }
    for (JSModule m : getModuleGraph().getTransitiveDepsDeepestFirst(module)) {
      inputs = m.getInputs();
      if (inputs.size() > 0) {
        return inputs.get(0).getAstRoot(this);
      }
    }
    throw new IllegalStateException("Root module has no inputs");
  }