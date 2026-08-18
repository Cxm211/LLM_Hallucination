public Iterator<Var> getVars() {
  if (vars == null) {
    return Collections.<Var>emptyList().iterator();
  }
  return new ArrayList<Var>(vars.values()).iterator();
}