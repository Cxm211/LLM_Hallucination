private void checkFirstModule(JSModule[] modules) {
    if (modules.length == 0) {
      report(JSError.make(EMPTY_MODULE_LIST_ERROR));
    } else {
      boolean hasInputs = false;
      for (JSModule m : modules) {
        if (!m.getInputs().isEmpty()) {
          hasInputs = true;
          break;
        }
      }
      if (!hasInputs) {
        report(JSError.make(EMPTY_ROOT_MODULE_ERROR,
            modules[0].getName()));
      }
    }
  }