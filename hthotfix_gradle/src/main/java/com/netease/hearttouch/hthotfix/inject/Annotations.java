/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.inject;

/**
 * Created by zw on 16/6/17.
 */
public class Annotations {
    public static final String HOTFIX_IGNORE = "Lcom/netease/hearttouch/hthotfix/HotfixIgnore;";

    public static boolean hasIgnore(String descriptor) {
        return descriptor.contains(HOTFIX_IGNORE);
    }
}
