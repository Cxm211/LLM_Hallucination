public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    java.util.Arrays.sort(ALL_IDS, new java.util.Comparator<String>() {
        public int compare(String a, String b) {
            return b.length() - a.length();
        }
    });
    for (String id : ALL_IDS) {
        if (str.startsWith(id)) {
            bucket.setZone(DateTimeZone.forID(id));
            return position + id.length();
        }
    }
    return ~position;
}