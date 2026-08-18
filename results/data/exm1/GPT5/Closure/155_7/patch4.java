  public Var getVar(String name) {
    Var var = vars.get(name);
    if (var != null) {
      return var;
    } else {
      return null;
    }
  }