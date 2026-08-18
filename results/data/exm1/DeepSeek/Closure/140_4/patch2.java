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
    JSModuleGraph moduleGraph = getModuleGraph();
    if (moduleGraph != null) {
      for (JSModule m : moduleGraph.getTransitiveDepsDeepestFirst(module)) {
        inputs = m.getInputs();
        if (inputs.size() > 0) {
          return inputs.get(0).getAstRoot(this);
        }
      }
    }
    throw new IllegalStateException("Root module has no inputs");
  }