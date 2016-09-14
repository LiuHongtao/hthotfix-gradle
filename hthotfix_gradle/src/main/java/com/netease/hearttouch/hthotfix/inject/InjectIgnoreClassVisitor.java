/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.inject;

import org.gradle.api.Project;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.AnnotationVisitor;

import java.text.MessageFormat;

/**
 * Created by zw on 16/6/17.
 */
public class InjectIgnoreClassVisitor extends ClassVisitor {
    private Project project;
    private HotfixInstrumentContext context;
    public InjectIgnoreClassVisitor(Project project,HotfixInstrumentContext context,ClassVisitor cv){
        super(Opcodes.ASM4,cv);
        this.context = context;
        this.project = project;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        //如果Class被标注HotfixIgnore
        if (Annotations.hasIgnore(desc)) {
            context.setIgnoreClass(true);
        }
        return super.visitAnnotation(desc,visible);
    }
}
