/*
 * Copyright (c) Shadow client, 0x150, Saturn5VFive 2022. All rights reserved.
 */

package net.shadow.client.helper.render;

import net.shadow.client.ShadowMain;
import org.lwjgl.glfw.GLFW;

public class Cursor {
    public static long CLICK = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
    public static long STANDARD = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
    public static long TEXT_EDIT = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
    public static long HSLIDER = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);
    private static long currentCursor = -1;

    public static void setGlfwCursor(long cursor) {
        if (currentCursor == cursor) return;
        String cname = "(unknown)";
        if (CLICK == cursor) cname = "CLICK";
        if (STANDARD == cursor) cname = "STANDARD";
        if (TEXT_EDIT == cursor) cname = "TEXT_EDIT";
        if (HSLIDER == cursor) cname = "HSLIDER";
        System.out.println("set cursor: 0x" + Long.toHexString(cursor).toUpperCase() + ": " + cname);
        currentCursor = cursor;
        GLFW.glfwSetCursor(ShadowMain.client.getWindow().getHandle(), cursor);
    }
}