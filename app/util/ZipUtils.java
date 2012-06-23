package util;

import scala.collection.Iterator;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static final int BUFFERSIZE = 4096;

    public static void zip(String outZip, scala.collection.Iterable<String> inFiles) {
        if (!inFiles.isEmpty()) {
            long start = System.currentTimeMillis();
            System.out.print("Zipping ... ");
            try {
                File outFolder = new File(outZip);
                ZipOutputStream out = new ZipOutputStream(new
                        BufferedOutputStream(new FileOutputStream(outFolder)));
                BufferedInputStream in = null;
                byte[] data  = new byte[BUFFERSIZE];
                Iterator<String> filesIt = inFiles.iterator();
                while(filesIt.hasNext()) {
                    String filePath = filesIt.next();
                    in = new BufferedInputStream(new FileInputStream(filePath), BUFFERSIZE);
                    out.putNextEntry(new ZipEntry(filePath));
                    int count;
                    while((count = in.read(data, 0, BUFFERSIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                    out.closeEntry();
                }
                out.flush();
                out.close();
                System.out.println("done in " + (System.currentTimeMillis() - start) + " ms.");
            } catch(Exception e) {
                System.out.println();
                e.printStackTrace();
            }
        }
    }

    /**def zip( out: String, files: Iterable[String] ) = {
     import java.io.{ BufferedInputStream, FileInputStream, FileOutputStream }
     import java.util.zip.{ ZipEntry, ZipOutputStream }
     val zip = new ZipOutputStream(new FileOutputStream(out))
     files.foreach { name =>
     zip.putNextEntry(new ZipEntry(name))
     val in = new BufferedInputStream(new FileInputStream(name))
     var b = in.read()
     while (b > -1) {
     zip.write(b)
     b = in.read()
     }
     in.close()
     zip.closeEntry()
     }
     zip.close()
     }  **/
}
