/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by lht on 16/7/28.
 */
public abstract class SopathVisitor extends SimpleFileVisitor<Path> {

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

        if (isSoFile(relativePath)) {
            visitSo(file, content);
        }

        return super.visitFile(file, attrs);
    }

    protected abstract void visitSo(Path path, byte[] bytecode);

    private static boolean isSoFile(Path file) {
        return file.getFileName().toString().endsWith(".so");
    }
}
