/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by lht on 16/7/29.
 */
public class FileUtil {

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static void moveFile(String fromFile, String toFile) {
        try {
            File zipFile = new File(fromFile);
            copyFile(new FileInputStream(zipFile),
                    new FileOutputStream(toFile));
            zipFile.delete();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * 压缩文件
     * @param inFilePath 目标文件
     * @param outFilePath 输出文件
     */
    public static void doZip(String inFilePath, String outFilePath){
        try{
            File inFile = new File(inFilePath);
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outFilePath));
            zipFile(zipOut, inFile, "");
            zipOut.close();
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    /**
     * @param out  输出流
     * @param file  目标文件
     * @return void
     * @throws Exception
     */
    private static void zipFile(ZipOutputStream out, File file, String dir) throws IOException{
        if(file.isDirectory()){
            out.putNextEntry(new ZipEntry(dir + "/"));
            dir = dir.length() == 0 ? "" : dir + "/";

            File[] files = file.listFiles();
            for(int i = 0 ; i < files.length ; i++){
                zipFile(out, files[i], dir + files[i].getName());
            }
        }
        else{
            FileInputStream fis = new FileInputStream(file);
            out.putNextEntry(new ZipEntry(dir));

            int tempByte;
            byte[] buffer = new byte[1024];
            while((tempByte = fis.read(buffer)) > 0){
                out.write(buffer,0,tempByte);
            }

            fis.close();
        }
    }
}
