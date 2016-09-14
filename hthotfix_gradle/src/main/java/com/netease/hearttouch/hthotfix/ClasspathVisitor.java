/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

/**
 * Created by zw on 16/5/26.
 */
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class ClasspathVisitor extends SimpleFileVisitor<Path> {

    private Path baseDir;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (baseDir == null) {
            baseDir = dir;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path relativePath = baseDir.relativize(file);
        byte[] content = Files.readAllBytes(file);

        if (isJavaClass(relativePath)) {
            visitClass(relativePath,content);
        } else {
            visitResource(relativePath, content);
        }
        return FileVisitResult.CONTINUE;
    }

    protected abstract void visitClass(Path path,byte[] bytecode) throws IOException;

    protected abstract void visitResource(Path relativePath, byte[] content) throws IOException;

    private static boolean isJavaClass(Path file) {
        return file.getFileName().toString().endsWith(".class");
    }
}
