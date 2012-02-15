package com.manno.android.skatespotsapp.Utils;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    
	public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

	public static String getCurrentUTCTime() {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String time = df.format(new Date());
        Log.d("Test", "UTC time is " + time);
        return time;
	}
		
	public static String convertToLocalTime(String UTCtime) {
    	
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
		try {
			date = df.parse(UTCtime);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
		df = new SimpleDateFormat("d MMM");
        DateFormat timeStringPart = new SimpleDateFormat("h:mma");
        return df.format(date)+", "+timeStringPart.format(date).toLowerCase();
    	
    }
}