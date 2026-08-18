  public Var getVar(String name) {
    Var var = vars.get(name);
    if (var != null) {
      return var;
    } else if (parent != null) {
      return parent.getVar(name);
    } else {
      return null;
    }
  }
