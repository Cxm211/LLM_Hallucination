public Iterator<Var> getVars() {
  return Collections.unmodifiableCollection(vars.values()).iterator();
}