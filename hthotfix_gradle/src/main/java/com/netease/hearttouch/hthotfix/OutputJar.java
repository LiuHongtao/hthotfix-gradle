/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class OutputJar {

    private JarOutputStream stream;

    public OutputJar(File outputFile) {
        try {
            Files.createDirectories(outputFile.getParentFile().toPath());
            stream = new JarOutputStream(new FileOutputStream(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeEntry(JarEntry jarEntry, byte[] content) throws IOException {
        JarEntry entry = new JarEntry(jarEntry.getName());
        stream.putNextEntry(entry);
        stream.write(content);
        stream.flush();
    }

    public void close() {
        try {
            if(stream == null) {
                return;
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
