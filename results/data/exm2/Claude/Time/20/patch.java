public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    String longestMatch = null;
    int longestMatchLength = 0;
    for (String id : ALL_IDS) {
        if (str.startsWith(id)) {
            if (id.length() > longestMatchLength) {
                longestMatch = id;
                longestMatchLength = id.length();
            }
        }
    }
    if (longestMatch != null) {
        bucket.setZone(DateTimeZone.forID(longestMatch));
        return position + longestMatchLength;
    }
    return ~position;
}