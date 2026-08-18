  public void init(JSSourceFile[] externs, JSModule[] modules,
      CompilerOptions options) {
    initOptions(options);

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
    if (modules.length == 0) {
      report(JSError.make(EMPTY_MODULE_LIST_ERROR));
      return;
    }
    // Do not report an error just because the first module is empty.
    // Some module graphs legitimately have an empty root with inputs
    // in dependent modules. Only report if ALL modules have no inputs.
    boolean anyInputs = false;
    for (JSModule m : modules) {
      if (!m.getInputs().isEmpty()) {
        anyInputs = true;
        break;
      }
    }
    if (!anyInputs) {
      report(JSError.make(EMPTY_ROOT_MODULE_ERROR, modules[0].getName()));
    }
  }

  Node getNodeForCodeInsertion(JSModule module) {
    if (module == null) {
      if (inputs.length == 0) {
        throw new IllegalStateException("No inputs");
      }

      return inputs[0].getAstRoot(this);
    }

    List<CompilerInput> inputs = module.getInputs();
    if (inputs.size() > 0) {
      return inputs.get(0).getAstRoot(this);
    }
    // Prefer the closest dependency with inputs rather than the deepest.
    List<JSModule> deps = getModuleGraph().getTransitiveDepsDeepestFirst(module);
    for (int i = deps.size() - 1; i >= 0; i--) {
      JSModule m = deps.get(i);
      inputs = m.getInputs();
      if (inputs.size() > 0) {
        return inputs.get(0).getAstRoot(this);
      }
    }
    throw new IllegalStateException("Module graph has no inputs available for insertion");
  }