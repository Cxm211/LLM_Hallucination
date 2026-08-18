  public JsonWriter value(boolean value) throws IOException {
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    int[] pathIndices = this.pathIndices;
    pathIndices[stackSize - 1]++;
    return this;
  }