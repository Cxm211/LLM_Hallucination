public String html() {
    StringBuilder accum = new StringBuilder();
    html(accum);
    return OutputSettings.prettyPrint(outputSettings()) ? accum.toString().trim() : accum.toString();
}