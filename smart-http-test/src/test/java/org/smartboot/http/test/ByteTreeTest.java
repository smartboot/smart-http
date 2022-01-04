/*******************************************************************************
 * Copyright (c) 2017-2022, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: ByteTreeTest.java
 * Date: 2022-01-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.test;

import org.junit.Assert;
import org.junit.Test;
import org.smartboot.http.common.utils.ByteTree;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/1/4
 */
public class ByteTreeTest {

    @Test
    public void test1() {
        ByteTree byteTree = new ByteTree(null, Byte.MAX_VALUE);
        byteTree.addNode("Hello");
        byteTree.addNode("Hello1");
        byteTree.addNode("Hello2");
        byte[] bytes = {'H', 'e', 'l', 'l', 'o', '1'};
        ByteTree searchTree = byteTree.search(bytes, 0, bytes.length, new byte[]{'1'}, false);
        Assert.assertEquals(searchTree.getStringValue(), "Hello");
        System.out.println(searchTree.getStringValue());

        searchTree = byteTree.search(bytes, 0, bytes.length, new byte[]{'l'}, false);
        Assert.assertEquals(searchTree.getStringValue(), "He");
        System.out.println(searchTree.getStringValue());

        searchTree = byteTree.search(bytes, 0, bytes.length, new byte[]{'o'}, false);
        System.out.println(searchTree.getStringValue());
        Assert.assertEquals(searchTree.getStringValue(), "Hell");
    }

    @Test
    public void test2() {
        ByteTree byteTree = new ByteTree(null, Byte.MAX_VALUE);
        byte[] bytes = "Hello Worldaa".getBytes();
        ByteTree searchTree = byteTree.search(bytes, 0, bytes.length, new byte[]{'a'});
        Assert.assertEquals(searchTree.getStringValue(), "Hello World");
        System.out.println(searchTree.getStringValue());

        searchTree = byteTree.search(bytes, 0, bytes.length, new byte[]{' '});
        System.out.println(searchTree.getStringValue());
        Assert.assertEquals(searchTree.getStringValue(), "Hello");

    }
}
