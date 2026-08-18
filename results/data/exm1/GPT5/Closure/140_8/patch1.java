  private void checkFirstModule(JSModule[] modules) {
    if (modules.length == 0) {
      report(JSError.make(EMPTY_MODULE_LIST_ERROR));
    }
  }