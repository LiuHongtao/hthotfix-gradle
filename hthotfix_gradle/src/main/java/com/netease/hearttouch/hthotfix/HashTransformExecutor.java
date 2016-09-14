/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;
import com.netease.hearttouch.hthotfix.inject.HackInjector;
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
 * Created by zw on 16/5/27.
 */
public class HashTransformExecutor {
    private final Project project;

    private final Logger logger;
    private final HashFileGenerator hashFileGenerator;
    private final HTHotfixExtension extension;
    private HackInjector hackInjector;

    public HashTransformExecutor(Project project,HTHotfixExtension extension) {
        this.project = project;
        this.logger = project.getLogger();
        hashFileGenerator = new HashFileGenerator(project);
        this.extension = extension;
        this.hackInjector = new HackInjector(project,extension.getIncludePackage(),extension.getExcludeClass());
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
                String className  = cr.getClassName();
                if(!extension.getGeneratePatch()){
                    hashFileGenerator.addClass(bytecode);
                    outputDirectory.writeClass(className,hackInjector.inject(className,bytecode));
                }else{
                    outputDirectory.writeClass(className,bytecode);
                }
            }

            @Override
            protected void visitResource(Path relativePath, byte[] content) throws IOException {
                outputDirectory.writeFile(relativePath, content);
            }
        });
    }
    public void transformJar(File inputFile,File outputFile) throws IOException {
        logger.info("HASHTRANSFORMJAR\t" + outputFile.getAbsolutePath());
        final JarFile jar = new JarFile(inputFile);
        final OutputJar outputJar = new OutputJar(outputFile);
        for (final Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
            final JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
//                new File(outFile, entry.getName()).mkdirs();
                continue;
            }
            final InputStream is = jar.getInputStream(entry);

            try {
                byte[] bytecode = IOUtils.toByteArray(is);
                if (entry.getName().endsWith(".class")) {
                    if(!extension.getGeneratePatch()) {
                        hashFileGenerator.addClass(bytecode);
                        ClassReader cr = new ClassReader(bytecode);

                        outputJar.writeEntry(entry, hackInjector.inject(cr.getClassName(),bytecode));
                    }else{
                        outputJar.writeEntry(entry, bytecode);
                    }
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
        if(!extension.getGeneratePatch()) {
            try{
                hashFileGenerator.generate();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


}
