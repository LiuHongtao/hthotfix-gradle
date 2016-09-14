/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.inject;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.Project;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;
import java.text.MessageFormat;
import org.objectweb.asm.Opcodes;

/**
 * Created by zw on 16/6/12.
 */
public class HackClassVisitor extends ClassVisitor{

    private String className;
    private String supperClassName;
    static final ImmutableSet<String> APPLICATION_CLASS_NAMES = ImmutableSet.of("android/app/Application","android/support/multidex/MultiDexApplication");
    private Project project;
    private HotfixInstrumentContext context;

    public HackClassVisitor(Project project,HotfixInstrumentContext context,ClassVisitor cv){

        super(Opcodes.ASM4,cv);
        this.project = project;
        this.context = context;
    }

    public void visit(int version, int access, String name, String sig, String superName, String[] interfaces) {
        super.visit(version,access,name,sig,superName,interfaces);
        this.className = name;
        this.supperClassName = superName;
        this.context.setClassName(name);
        this.context.setSupperClassName(superName);
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        //在除标注HotfixIgnore和Application相关类外,所有类都在构造方法中调用Hack包中mock方法
        if(!context.isIgnoreClass()&&!APPLICATION_CLASS_NAMES.contains(this.supperClassName)){
            if(name.equals("<init>")){
                context.isClassModified();
               return new HackInitMethodVisitor(project,mv,access,name,desc);
            }
        }
        return mv;
    }
}
