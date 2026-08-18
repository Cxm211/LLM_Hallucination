public JsonWriter value(double value) throws IOException {
    writeDeferredName();
    // Allow non-finite values here; responsibility for restricting them
    // (e.g., based on Gson configuration) lies with higher-level adapters.
    beforeValue();
    out.append(Double.toString(value));
    return this;
  }