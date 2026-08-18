public String generateToolTipFragment(String toolTipText) {
        String escaped = String.valueOf(toolTipText).replace("\"", "&quot;");
        return " title=\"" + escaped + "\" alt=\"\"";
    }