/*******************************************************************************
 * Copyright (c) 2017-2022, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: BitTree.java
 * Date: 2022-01-02
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.utils;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/1/2
 */
public class ByteTree {
    public static final ByteTree ROOT = new ByteTree();
    private final byte value;
    private final int depth;
    private final ByteTree parent;
    private int shift = -1;
    private ByteTree[] nodes;
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
        clone.addNode(b, 0, b.length);
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
        ByteTree byteTree = this;
        while (true) {
            if (offset >= limit) {
                return null;
            }
            for (byte end : ends) {
                if (end == bytes[offset]) {
                    return byteTree;
                }
            }

            int i = bytes[offset] - byteTree.shift;
            if (byteTree.nodes == null || i >= byteTree.nodes.length || i < 0) {
                break;
            }
            ByteTree b = byteTree.nodes[i];
            if (b != null) {
                byteTree = b;
                offset++;
//                return b.search(bytes, offset + 1, limit, ends, cache);
            } else {
                break;
            }
        }
        if (cache) {
            //在当前节点上追加子节点
//            System.out.println("add");
            byteTree.addNode(bytes, offset, limit);
            return byteTree.search(bytes, offset, limit, ends, cache);
        } else {
//            System.out.println("tmp");
            // 构建临时对象，用完由JVM回收
            ByteTree clone = clone(byteTree);
            return clone.search(bytes, offset - byteTree.depth, limit, ends, true);
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
        ByteTree leafNode = tree.addNode(bytes, 0, bytes.length);
        leafNode.stringValue = value;
    }

    private ByteTree addNode(byte[] value, int offset, int limit) {
        if (offset == limit) {
            return this;
        }

        byte b = value[offset];
        if (shift == -1) {
            shift = b;
        }
        if (b - shift < 0) {
            increase(b - shift);
        } else {
            increase(b + 1 - shift);
        }

        ByteTree nextTree = nodes[b - shift];
        if (nextTree == null) {
            nextTree = nodes[b - shift] = new ByteTree(this, b);
        }
        return nextTree.addNode(value, offset + 1, limit);
    }

    private void increase(int size) {
        if (size == 0)
            size = -1;
        if (nodes == null) {
            nodes = new ByteTree[size];
        } else if (size < 0) {
            ByteTree[] temp = new ByteTree[nodes.length - size];
            System.arraycopy(nodes, 0, temp, -size, nodes.length);
            nodes = temp;
            shift += size;
        } else if (nodes.length < size) {
            ByteTree[] temp = new ByteTree[size];
            System.arraycopy(nodes, 0, temp, 0, nodes.length);
            nodes = temp;
        }
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
