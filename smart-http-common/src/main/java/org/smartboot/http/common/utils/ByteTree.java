/*******************************************************************************
 * Copyright (c) 2017-2022, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: BitTree.java
 * Date: 2022-01-02
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/1/2
 */
public class ByteTree {
    public static final ByteTree ROOT = new ByteTree();
    private final byte value;
    private final int depth;
    private final Map<Byte, ByteTree> nextNodes = new ConcurrentHashMap<>();
    private final ByteTree parent;
    private String stringValue;

    public ByteTree() {
        this(null, Byte.MIN_VALUE);
    }

    public ByteTree(ByteTree parent, byte value) {
        this.parent = parent;
        this.value = value;
        this.depth = parent == null ? 0 : parent.depth + 1;
    }

    public int getDepth() {
        return depth;
    }

    public ByteTree search(byte[] bytes, int offset, int len, byte[] ends) {
        return search(bytes, offset, len, ends, true);
    }

    private ByteTree clone(ByteTree byteTree) {
        byte[] b = new byte[byteTree.getDepth()];
        ByteTree tree = byteTree;
        while (tree.depth != 0) {
            b[tree.depth - 1] = tree.value;
            tree = tree.parent;
        }
        ByteTree clone = new ByteTree(null, Byte.MAX_VALUE);
        clone.addNode(clone, b, 0, b.length);
        return clone;
    }

    /**
     * 从给定的字节数组总匹配出特定结尾的区块
     *
     * @param bytes  待匹配的字节数组
     * @param offset 起始位置
     * @param limit  截止位置
     * @param ends   结束符
     * @param cache  是否缓存新节点
     * @return
     */
    public ByteTree search(byte[] bytes, int offset, int limit, byte[] ends, boolean cache) {
        for (byte end : ends) {
            if (end == value) {
                return this.parent;
            }
        }
        if (offset == limit) {
            return null;
        }
        ByteTree b = nextNodes.get(bytes[offset]);
        if (b != null) {
            return b.search(bytes, offset + 1, limit, ends, cache);
        }

        if (cache) {
            //在当前节点上追加子节点
            addNode(this, bytes, offset, limit);
            return search(bytes, offset, limit, ends, cache);
        } else {
            // 构建临时对象，用完由JVM回收
            ByteTree clone = clone(this);
            return clone.search(bytes, offset - depth, limit, ends, true);
        }
    }


    /**
     * 从根节点开始，为入参字符串创建节点
     */
    public void addNode(String value) {
        byte[] bytes = value.getBytes();
        ByteTree tree = this;
        while (tree.depth > 0) {
            tree = tree.parent;
        }
        addNode(tree, bytes, 0, bytes.length);
    }

    private void addNode(ByteTree tree, byte[] value, int offset, int limit) {
        if (offset == limit) {
            return;
        }
        ByteTree nextTree = tree.nextNodes.get(value[offset]);
        if (nextTree == null) {
            nextTree = new ByteTree(tree, value[offset]);
            tree.nextNodes.put(nextTree.value, nextTree);
        }
        addNode(nextTree, value, offset + 1, limit);
    }


    public String getStringValue() {
        if (stringValue == null) {
            byte[] b = new byte[depth];
            ByteTree tree = this;
            while (tree.depth != 0) {
                b[tree.depth - 1] = tree.value;
                tree = tree.parent;
            }
            stringValue = new String(b);
        }
        return stringValue;
    }

}
