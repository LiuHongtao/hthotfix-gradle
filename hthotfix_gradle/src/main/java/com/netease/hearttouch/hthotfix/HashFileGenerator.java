/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.apache.commons.codec.digest.DigestUtils;
import java.io.FileNotFoundException;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import java.nio.file.Path;
import java.nio.file.Files;

/**
 * Created by zw on 16/5/26.
 */
public class HashFileGenerator {
    private String outputPath;
    private BufferedSink sink;
    private final Logger logger;
    private static final int newLine = '\n';
    private HashMap<String,String> hashMap;
    public HashFileGenerator(Project project ){
        outputPath = Constants.getHotfixPath(project,Constants.HOTFIX_CLASSES_HASH);
        this.logger = project.getLogger();
        hashMap= new HashMap<String, String>();
    }

    public void addClass(byte[] classByte) throws  IOException{
            ClassReader cr = new ClassReader(classByte);
            String className =   cr.getClassName();
            String sha1Hex =  DigestUtils.shaHex(classByte);
            hashMap.put(className,sha1Hex);
    }

    public void generate() throws IOException{
        if(hashMap.isEmpty()){
            return;
        }
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
            logger.error("HashFileGenerator generate error:\t"+e.getMessage());
            throw e;
        }finally {
            if(sink!=null) {
                sink.close();
                sink = null;
            }
        }

    }


}
