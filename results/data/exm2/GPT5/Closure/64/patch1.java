String toSource(Node n) {
    initCompilerOptionsIfTesting();
    return toSource(n, null, options.getLanguageOut() == LanguageMode.ECMASCRIPT5_STRICT);
  }