package me.radu.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class NativeFileDialogs {

    static {
        try {
            InputStream inputStream = NativeFileDialogs.class.getResourceAsStream("/native_file_dialogs.dll");
            File tempFile = File.createTempFile("native_file_dialogs", ".dll");
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            System.load(tempFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public native static String selectFile(String string);
}
