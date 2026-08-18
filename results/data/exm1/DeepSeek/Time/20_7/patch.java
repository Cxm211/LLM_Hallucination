public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    String bestId = null;
    int bestLen = -1;
    for (String id : ALL_IDS) {
        if (str.startsWith(id) && id.length() > bestLen) {
            bestLen = id.length();
            bestId = id;
        }
    }
    if (bestId != null) {
        bucket.setZone(DateTimeZone.forID(bestId));
        return position + bestLen;
    }
    return ~position;
}