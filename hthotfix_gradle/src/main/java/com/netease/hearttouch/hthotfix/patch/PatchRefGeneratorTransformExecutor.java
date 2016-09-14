/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.patch;

import com.netease.hearttouch.hthotfix.*;
import com.netease.hearttouch.hthotfix.refclass.RefScanInstrument;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 查找patch class的引用类
 * Created by zw on 16/5/27.
 */
public class PatchRefGeneratorTransformExecutor {
    private final Project project;

    private final Logger logger;
    private final PatchJarHelper patchJarHelper;
    private final HTHotfixExtension extension;
    private final RefScanInstrument refScanInstrument;
    public PatchRefGeneratorTransformExecutor(Project project, HTHotfixExtension extension,RefScanInstrument refScanInstrument) {
        this.project = project;
        this.logger = project.getLogger();
        patchJarHelper = new PatchJarHelper(project);
        this.extension = extension;
        this.refScanInstrument = refScanInstrument;
    }

    public void transformDirectory(File inputFile, File outputFile) throws IOException {

        Path inputDir =  inputFile.toPath();
        Path  outputDir = outputFile.toPath();

        if (!Files.isDirectory(inputDir)) {
            return;
        }
        final OutputDirectory outputDirectory = new OutputDirectory(outputDir);
        Files.walkFileTree(inputDir, new ClasspathVisitor() {
            @Override
            protected void visitClass(Path path,byte[] bytecode) throws IOException {
                ClassReader cr = new ClassReader(bytecode);
                String className =   cr.getClassName();
                if(extension.getGeneratePatch()&&extension.getScanRef()) {
                    if(!refScanInstrument.isPatchClass(className.replace("/","."))){
                        boolean bPatchRef = refScanInstrument.hasReference(bytecode,project);
                        if (bPatchRef) {
                            patchJarHelper.writeClassToDirectory(className, bytecode);
                        }
                    }
                }
                outputDirectory.writeClass(className,bytecode);
            }

            @Override
            protected void visitResource(Path relativePath, byte[] content) throws IOException {
                outputDirectory.writeFile(relativePath, content);

            }
        });
    }
    public void transformJar(File inputFile,File outputFile) throws IOException {
        final JarFile jar = new JarFile(inputFile);
        final OutputJar outputJar = new OutputJar(outputFile);
        for (final Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
            final JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            final InputStream is = jar.getInputStream(entry);
            try {

                byte[] bytecode = IOUtils.toByteArray(is);
                if (entry.getName().endsWith(".class")) {
                    if(extension.getGeneratePatch()&&extension.getScanRef()){
                        ClassReader cr = new ClassReader(bytecode);
                        String className =   cr.getClassName();
                        if(extension.getGeneratePatch()&&extension.getScanRef()) {
                            if(!refScanInstrument.isPatchClass(className.replace("/","."))){
                                boolean bPatchRef = refScanInstrument.hasReference(bytecode,project);
                                if (bPatchRef) {
                                    patchJarHelper.writeClassToDirectory(className, bytecode);
                                }
                            }
                        }
                    }
                    outputJar.writeEntry(entry, bytecode);
                } else {
                    outputJar.writeEntry(entry, bytecode);
                }
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        outputJar.close();
    }

    public void transformFinish(){

    }
}
