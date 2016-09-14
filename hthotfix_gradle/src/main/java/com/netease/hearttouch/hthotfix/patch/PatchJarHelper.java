/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.patch;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.netease.hearttouch.hthotfix.Constants;
import org.gradle.api.Project;

import com.netease.hearttouch.hthotfix.OutputDirectory;

/**
 * Created by zw on 16/5/26.
 */
public class PatchJarHelper {
    private final OutputDirectory outputDir;
    private final Path patchClassPath;
    public PatchJarHelper(Project project){
        File classFile = new File(Constants.getHotfixPath(project, Constants.HOTFIX_CLASSES));
        patchClassPath = classFile.toPath();
        outputDir  = new OutputDirectory(patchClassPath);
    }

    public void writeClassToDirectory(String className,byte[] content) throws IOException {

        Path relativePath = Paths.get(className + ".class");
        Path outputFile = patchClassPath.resolve(relativePath);
        Files.createDirectories(outputFile.getParent());
        Files.write(outputFile, content);
    }
}
