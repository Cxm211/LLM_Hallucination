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

// trigger testcase
public void testEmptyModule() {
    // When the dest module is empty, it might try to move the code to the
    // one of the modules that the empty module depends on. In some cases
    // this might ended up to be the same module as the definition of the code.
    // When that happens, CrossMooduleCodeMotion might report a code change
    // while nothing is moved. This should not be a problem if we know all
    // modules are non-empty.
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1", "function x() {}"));
    
    JSModule empty = new JSModule("empty");
    empty.addDependency(m1);
    
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2", "x()"));
    m2.addDependency(empty);
    
    JSModule m3 = new JSModule("m3");
    m3.add(JSSourceFile.fromCode("m3", "x()"));
    m3.addDependency(empty);
    
    test(new JSModule[] {m1,empty,m2,m3},
        new String[] {
          "",
          "function x() {}",
          "x()",
          "x()"
    });
  }
