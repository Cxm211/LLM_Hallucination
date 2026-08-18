    static int reduceAndCorrect(Calendar start, Calendar end, int field, int difference) {
        end.add( field, -1 * difference );
        if (field == Calendar.MONTH) {
            long totalMonthsStart = start.get(Calendar.YEAR) * 12L + start.get(Calendar.MONTH);
            long totalMonthsEnd = end.get(Calendar.YEAR) * 12L + end.get(Calendar.MONTH);
            if (totalMonthsEnd < totalMonthsStart) {
                long newdiff = totalMonthsStart - totalMonthsEnd;
                end.add( Calendar.MONTH, (int) newdiff );
                return (int) newdiff;
            } else {
                return 0;
            }
        } else {
            int endValue = end.get(field);
            int startValue = start.get(field);
            if (endValue < startValue) {
                int newdiff = startValue - endValue;
                end.add( field, newdiff );
                return newdiff;
            } else {
                return 0;
            }
        }
    }