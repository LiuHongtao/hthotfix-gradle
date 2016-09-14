/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix

import com.netease.hearttouch.hthotfix.inject.HackInjector
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import java.nio.file.Files
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import java.nio.file.Paths;

/**
 * 混淆相关
 * Created by zw on 16/6/7.
 */
public class ProguardHelper {

    private String mappingPath
    private Project project
    private String intermediateClassPath
    private HashMap<String,String> hashMap
    private HTHotfixExtension extension;
    public ProguardHelper(Project project,String mappingPath,String intermediateClassPath,HTHotfixExtension extension){
        this.project = project
        this.mappingPath = mappingPath
        this.intermediateClassPath  = intermediateClassPath
        hashMap = new HashMap<String,String>()
        this.extension = extension
    }

    public List<String> parse(){
        File mappingFile = new File(mappingPath)
        if(mappingFile.exists()){
           def reader =  mappingFile.newReader()
            reader.eachLine { String line ->
                if(line.endsWith(':')) {
                    String[] strings = line.replace(':','').split(' -> ')
                    if(strings.length == 2) {
                        hashMap.put(strings[0],strings[1])
                    }
                }
            }
            reader.close()
        }

        List<String> proguardClass = new ArrayList<String>()
        File intermediateClassFile = new File(this.intermediateClassPath)
        if(intermediateClassFile.exists()){
            intermediateClassFile.eachFileRecurse { File file->
                String filePath = file.absolutePath

                if(filePath.endsWith('.class')) {
                    // 获取类名
                    int beginIndex = filePath.lastIndexOf(intermediateClassFile.name)+intermediateClassFile.name.length()+1
                    String className = filePath.substring(beginIndex, filePath.length()-6).replace('\\','.').replace('/','.')
                    // 获取混淆后类名
                    String proguardName = hashMap.get(className)
                    proguardClass.add(proguardName)
                }

            }
        }
        return proguardClass
    }

    public void copyProguardClass(){
        List<String> proguardClassName  = parse()
        //删除原有文件
        File intermediateClassFile = new File(this.intermediateClassPath)
        project.getLogger().error("delete interdir");
        intermediateClassFile.deleteDir()
        intermediateClassFile.mkdir()
        Path intermediateClassPath = intermediateClassFile.toPath()
        File proguardDir = new File(project.getBuildDir().getAbsolutePath()+"/intermediates/transforms/proguard/")

        HackInjector hackInjector  = new HackInjector(project,extension.includePackage,extension.excludeClass)

        if(proguardDir.exists()){
            proguardDir.eachFileRecurse { File file->
                if(file.name.endsWith(".jar")){
                    //写到临时目录

                    JarFile jar = new JarFile(file)
                    for (final Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                        final JarEntry entry = entries.nextElement();
                        if (entry.isDirectory()) {
                            continue;
                        }
                        //key 混淆前,value 混淆后

                        if (entry.getName().endsWith(".class")) {
                            final InputStream is = jar.getInputStream(entry);
                            byte[] bytecode = IOUtils.toByteArray(is);
                            ClassReader cr = new ClassReader(bytecode);
                            String clsName = cr.getClassName(); //混淆后

                            if(proguardClassName.contains(clsName.replace("/","."))){
                                project.getLogger().error("write proguardClass"+clsName);

                                Path relativePath = Paths.get(clsName + ".class");
                                //此时clsName为混淆后的类名，不能启动判断的作用

                                //但之前已经做过是否注入的检测，
                                writeFile(intermediateClassPath,relativePath, hackInjector.injectImpl(bytecode));
                            }
                        }
                    }
                }
            }
        } else {
            project.getLogger().error("proguard transform  class not existed")
        }
    }

    public void writeFile(Path hotfixIntermediatePath,Path relativePath, byte[] content) throws IOException {
        Path outputFile = hotfixIntermediatePath.resolve(relativePath);
        Files.createDirectories(outputFile.getParent());
        Files.write(outputFile, content);
    }
}