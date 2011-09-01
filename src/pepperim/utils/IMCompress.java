package pepperim.utils;

import java.io.*;
import java.util.zip.*;

/**
 * A class providing a simple interface for GZip compression.
 * @author      Anton Pirogov <anton dot pirogov at googlemail dot com> 
 */
public class IMCompress {
    /**
     * @param data Data to be GZipped
     * @return compressed data
     */
    public static byte[] zip(String data) {
        try {
        byte[] dat = data.getBytes();
        ByteArrayOutputStream compr = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream(compr);
        zip.write(dat,0,dat.length);
        zip.finish();
        return compr.toByteArray();
        
        } catch (Exception e) { 
            e.printStackTrace();    
            return null;
        }
    }

    /**
     * @param dat Data to be decompressed
     * @return original data
     */
    public static String unzip(byte[] dat) {
        try {
        ByteArrayInputStream compr = new ByteArrayInputStream(dat);
        ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
        GZIPInputStream zip = new GZIPInputStream(compr);
        byte[] buffer = new byte[65536];
        int size = 0;
        while ( ( size = zip.read(buffer,0,buffer.length) ) != -1)
            unzipped.write(buffer, 0, size);
        zip.close();
        return unzipped.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
