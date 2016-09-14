/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.patch;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by lht on 16/7/28.
 */
public class HashFileHelper {

    /**
     * 生成hash文件
     * @param hashMap
     * @param outputPath 输出路径
     * @param logger
     * @param error
     * @throws IOException
     */
    public static void generate(HashMap hashMap, String outputPath, Logger logger, String error) throws IOException {
        if(hashMap.isEmpty()){
            return;
        }

        BufferedSink sink = null;
        int newLine = '\n';
        try {
            Path relativePath =   Paths.get(outputPath);

            if(relativePath.toFile().exists())
                relativePath.toFile().delete();

            if(!relativePath.getParent().toFile().exists()){
                Files.createDirectories(relativePath.getParent());
            }

            Files.createFile(relativePath);
            FileOutputStream outputStream = null;
            outputStream = new FileOutputStream(relativePath.toFile());

            sink =  Okio.buffer( Okio.sink(outputStream));
            Set<Map.Entry<String,String>> set =  hashMap.entrySet();
            Iterator<Map.Entry<String,String>> iterator  =  set.iterator();
            while(iterator.hasNext()){
                Map.Entry<String,String> entry =  iterator.next();
                String className =  entry.getKey();
                String sha1Hex = entry.getValue();
                sink.writeUtf8(String.format("%s,%s", className, sha1Hex));
                sink.writeByte(newLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(error + ":\t"+e.getMessage());
            throw e;
        }finally {
            if(sink != null) {
                sink.close();
                sink = null;
            }
        }
    }

    /**
     * 解析hash文件
     * @param hashFilePath 文件路径
     * @return
     */
    public static HashMap<String, String> parse(String hashFilePath)  {
        BufferedSource source;
        try {
            File hashFile = new File(hashFilePath);
            FileInputStream inputStream = null;
            inputStream = new FileInputStream(hashFile);
            source = Okio.buffer(Okio.source(inputStream));
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        HashMap<String, String> hashMap = new HashMap<>();
        try{
            for(;;){
                String clsNameAndHash = source.readUtf8Line();
                if(clsNameAndHash==null)
                    break;
                String[] strCLassName = clsNameAndHash.split(",");
                hashMap.put(strCLassName[0],strCLassName[1]);
            }

        }catch (IOException e){
        }finally {
            try{
                if(source!=null)
                    source.close();
                source = null;

            }catch (IOException e){
            }

        }
        return hashMap;
    }
}
