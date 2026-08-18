public String html() {
    StringBuilder accum = new StringBuilder();
    html(accum);
    return OutputSettings.prettyPrint ? accum.toString().trim() : accum.toString();
}