private String escape(String string) {
        int index = string.indexOf('\'');
        while (index != -1) {
            string =
                string.substring(0, index)
                    + "&apos;"
                    + string.substring(index + 1);
            index = string.indexOf('\'', index + 6);
        }
        index = string.indexOf('"');
        while (index != -1) {
            string =
                string.substring(0, index)
                    + "&quot;"
                    + string.substring(index + 1);
            index = string.indexOf('"', index + 6);
        }
        return string;
    }