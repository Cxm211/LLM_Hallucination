public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    java.util.List<String> sortedIds = new java.util.ArrayList<String>(ALL_IDS);
    java.util.Collections.sort(sortedIds, new java.util.Comparator<String>() {
        public int compare(String s1, String s2) {
            return s2.length() - s1.length();
        }
    });
    for (String id : sortedIds) {
        if (str.startsWith(id)) {
            bucket.setZone(DateTimeZone.forID(id));
            return position + id.length();
        }
    }
    return ~position;
}