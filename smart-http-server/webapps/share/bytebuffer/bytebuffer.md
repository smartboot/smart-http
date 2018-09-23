ByteBuffer是在NIO/AIO开发中非常关键的类，也是从事通信开发必须熟练掌握的。但因其极具灵活性，不少朋友在使用上会遇上一些困扰。本文将尽量以一种通俗易懂的方式,为大家阐述为什么要用ByteBuffer，如何正确运用ByteBuffer。

#byte
这是一种逐字节解析的方式，每次仅读取一个字节，处理完成之后继续读取下一个。这种处理方式特别简单，也非常容易理解，并且可以精准识别消息边界，无需担忧如何处理粘包半包现象。但同时弊端也比较显著，由于频繁进行IO操作且每次仅1字节，导致整体效率不高（IO操作相对于内存操作是比较低效的），所以该处理方式几乎不被采用。

![bytebuffer.png](bytebuffer.png)

```
InputStream in=socket.getInputStream();
byte b=-1;
while((b= (byte) in.read())!=-1){
    ....
}
```

#byte[]
这是第一种操作方式的进阶版，一次读取一批数据填充至byte数组，待已读数据都处理完成后再进行下一次的读取。此类方式通过减少IO次数有效提升处理性能，但是必须保证每一批的数据都处理完才可进行下一轮读取。如下图所示，假设1、2、3组成一个完整的消息，而4为下一个消息的一部分，即出现了粘包情况，此时需要在处理逻辑中做一下额外处理来缓存多读的一部分。又或者假设1、2、3、4、5才是一个完整的消息，本轮只读取到4为止，即出现了半包情况，这个时候又需要将已读的部分先缓存起来，以便为byte数组腾出空间来进行下一次读取。总之，通过byte数组的方式进行IO处理在性能上有一定的提升，但也增加了操作逻辑的复杂度。

![bytebuffer.png](bytearray.png)

```
InputStream in=socket.getInputStream();
byte[] array=new byte[1024];
int length=0;
while((length= in.read(array))!=-1){
    process(array,0,length);
}
```

#ByteBuffer
ByteBuffer其实是个抽象类，本章节会基于它的一个子类HeapByteBuffer进行讲解，所以下文所说的ByteBuffer指代的也是HeapByteBuffer。

ByteBuffer的本质也是个byte数组，但同时它还包含几个数据元素，本文会筛选其中关键的几个进行深入解读，其余部分有待读者亲自去翻一下JDK的源码。ByteBuffer类中封装的几个关键要素为：

- byte[] hb：heap buffers，用于存储数据
- position：当前行为事件所处的点位，行为可能是读操作，或者是写操作。其数值与`byte[] hb`的下标维持映射关系。
- limit：本次行为事件的最大可操作点位，同样也是映射了`byte[] hb`的下标。无论何种事件类型，position值都不可大于limit值，否则视为异常。
- capacity：该值等同于`byte[] hb`的长度，在构造ByteBuffer时边确定下来，且不可更改。

> 行为事件：读操作ByteBuffer.get()、写操作ByteBuffer.put()

