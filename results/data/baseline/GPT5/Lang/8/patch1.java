public void appendTo(StringBuffer buffer, Calendar calendar) {
            if (calendar.get(Calendar.DST_OFFSET) != 0) {
                buffer.append(mDaylight);
            } else {
                buffer.append(mStandard);
            }
        }