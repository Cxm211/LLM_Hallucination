    protected void _format(TimeZone tz, Locale loc, Date date,
            StringBuffer buffer)
    {
        Calendar cal = _getCalendar(tz);
        cal.setTime(date);
        // [databind#2167]: handle range beyond [1, 9999]
        final int year = cal.get(Calendar.YEAR);

        // Handle sign and padding for ISO 8601
        char sign = year < 0 ? '-' : '+';
        int absYear = Math.abs(year);
        buffer.append(sign);
        if (absYear < 10000) {
            pad4(buffer, absYear);
        } else {
            buffer.append(absYear);
        }
        buffer.append('-');
        pad2(buffer, cal.get(Calendar.MONTH) + 1);
        buffer.append('-');
        pad2(buffer, cal.get(Calendar.DAY_OF_MONTH));
        buffer.append('T');
        pad2(buffer, cal.get(Calendar.HOUR_OF_DAY));
        buffer.append(':');
        pad2(buffer, cal.get(Calendar.MINUTE));
        buffer.append(':');
        pad2(buffer, cal.get(Calendar.SECOND));
        buffer.append('.');
        pad3(buffer, cal.get(Calendar.MILLISECOND));

        int offset = tz.getOffset(cal.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / (60 * 1000)) / 60);
            int minutes = Math.abs((offset / (60 * 1000)) % 60);
            buffer.append(offset < 0 ? '-' : '+');
            pad2(buffer, hours);
            if( _tzSerializedWithColon ) {
              buffer.append(':');
            }
            pad2(buffer, minutes);
        } else {
            // 24-Jun-2017, tatu: While `Z` would be conveniently short, older specs
            //   mandate use of full `+0000`
//            formatted.append('Z');
          if( _tzSerializedWithColon ) {
             buffer.append("+00:00");
          }
          else {
           buffer.append("+0000");
          }
        }
    }