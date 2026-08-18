public String generateToolTipFragment(String toolTipText) {
    String escaped = toolTipText.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    return " title=\"" + escaped + "\" alt=\"";
}