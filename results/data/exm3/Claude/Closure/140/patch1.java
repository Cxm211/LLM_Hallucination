private void checkFirstModule(JSModule[] modules) {
    if (modules.length == 0) {
      report(JSError.make(EMPTY_MODULE_LIST_ERROR));
    } else {
      boolean hasNonEmptyModule = false;
      for (JSModule module : modules) {
        if (!module.getInputs().isEmpty()) {
          hasNonEmptyModule = true;
          break;
        }
      }
      if (!hasNonEmptyModule) {
        report(JSError.make(EMPTY_ROOT_MODULE_ERROR,
            modules[0].getName()));
      }
    }
  }