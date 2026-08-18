  
  public Var getVar(String name) {
    Var var = vars.get(name);
    if (var != null) {
      return var;
    }
    // Do not resolve "arguments" to a parent scope.
    if ("arguments".equals(name)) {
      return null;
    } else if (parent != null) { // Recurse up the parent Scope
      return parent.getVar(name);
    } else {
      return null;
    }
  }