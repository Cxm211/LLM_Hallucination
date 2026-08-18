private void checkFirstModule(JSModule[] modules) {
    if (modules.length == 0) {
      report(JSError.make(EMPTY_MODULE_LIST_ERROR));
    } else if (modules[0].getInputs().isEmpty()) {
      report(JSError.make(EMPTY_ROOT_MODULE_ERROR, modules[0].getName()));
    }
  }