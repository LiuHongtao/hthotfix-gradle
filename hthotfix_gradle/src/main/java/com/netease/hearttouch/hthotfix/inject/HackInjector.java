/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.inject;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.Project;
import org.objectweb.asm.*;
import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

import java.util.ArrayList;

/**
 * Created by zw on 16/6/12.
 */
public class HackInjector {

    private static final String AndroidSupportPackage = "android/support/";
    private static final String HOTFIXPACKAGE = "com/netease/hearttouch/hthotfix/";

    public static final ImmutableSet<String> RESOURCE_CLASS_NAME = ImmutableSet.of(".R",".R$anim",".R$attr", ".R$bool",
            ".R$color", ".R$dimen", ".R$drawable", ".R$id", ".R$integer", ".R$layout", ".R$mipmap", ".R$string", ".R$style",
            ".R$styleable");


    public ArrayList<String> includePackages;
    public ArrayList<String> execludeClasses;
    private Project project;

    private String lastSuperName;
    public HackInjector(Project project, ArrayList<String> includePackages,ArrayList<String> execludeClasses){
        this.includePackages = includePackages;
        this.execludeClasses = execludeClasses;
        this.project = project;
    }

    public byte[] inject(String className,byte[] bytes){
        if (shouldInject(className)) {
            return injectImpl(bytes);
        }else{
            return bytes;
        }
    }

    public byte[] injectImpl(byte[] bytes){
        this.lastSuperName = null;
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        HotfixInstrumentContext context = new HotfixInstrumentContext();
        ClassVisitor cv = new InjectIgnoreClassVisitor(project,context,cw);
        cv = new HackClassVisitor(project,context,cv);
        cr.accept(cv,EXPAND_FRAMES);
        lastSuperName  = context.getSupperClassName();
        //怎么传？  context.getSupperClassName()
        return cw.toByteArray();

    }

    public String getLastSuperName(){
        return lastSuperName;
    }

    private boolean shouldInject(String className){
        if(className==null||className.length() ==0)
            return false;
        String internalClassName = className.replace('/','.');
        for(String resourceClassName:RESOURCE_CLASS_NAME){
            if(internalClassName.endsWith(resourceClassName))
                return false;
        }

        if(internalClassName.startsWith(AndroidSupportPackage)||internalClassName.startsWith(HOTFIXPACKAGE)){
            return false;
        }else if(execludeClasses.contains(internalClassName))
            return false;
        else{
            for(int i=0;i<includePackages.size();i++){
                if(internalClassName.startsWith(includePackages.get(i))){
                    return true;
                }
            }
        }
        return false;
    }
}
