private void checkFirstModule(JSModule[] modules) {
    if (modules.length == 0) {
      report(JSError.make(EMPTY_MODULE_LIST_ERROR));
      return;
    }
    for (JSModule m : modules) {
      if (m.getInputs().isEmpty()) {
        report(JSError.make(EMPTY_ROOT_MODULE_ERROR, m.getName()));
      }
    }
  }