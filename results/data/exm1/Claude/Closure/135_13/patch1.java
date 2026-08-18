public boolean hasProperty(String name) {
  if ("prototype".equals(name)) {
    return prototypeSlot != null;
  }
  return super.hasProperty(name);
}