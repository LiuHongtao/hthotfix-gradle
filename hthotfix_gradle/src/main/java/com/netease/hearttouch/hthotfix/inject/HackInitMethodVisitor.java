/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.inject;

import org.gradle.api.Project;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;
/**
 * 在构造方法中添加Hack.mock()形成依赖
 * Created by zw on 16/6/12.
 */
public class HackInitMethodVisitor extends AdviceAdapter {
    private static final String HackClassName = "com/netease/hearttouch/hthotfix/Hack";
    private String name;
    private int access;
    private Project project;

    public HackInitMethodVisitor(Project project,MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM4,mv, access, name, desc);
        this.access = access;
        this.name = name;
        this.project = project;
    }

    protected void onMethodExit(int opcode) {
        Type targetType = Type.getObjectType(HackClassName);
        super.invokeStatic(targetType, new Method("mock", "()V"));
    }
}
