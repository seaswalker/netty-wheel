[![Build Status](https://travis-ci.org/seaswalker/netty-wheel.svg?branch=master)](https://travis-ci.org/seaswalker/netty-wheel)
[![codecov](https://codecov.io/gh/seaswalker/netty-wheel/branch/master/graph/badge.svg)](https://codecov.io/gh/seaswalker/netty-wheel)

# 实现的功能

- channelActive/channelInActive/channelRead/channelWrite事件处理

- HandlerInitializer 

- Handler链式处理

- StringEncoder/StringDecoder

- LengthFieldBasedDecoder

- DelimiterBasedDecoder

- LineBasedDecoder

# 线程模型

![ThreadMode](images/thread_mode.jpg)

# 示例

## 服务器启动

以定长解码器为例:

```java
@Test
public void lengthFieldBasedDecoder() {
    Server server = new Server();
    server.bind(8080).setHandlers(new HandlerInitializer() {
        @Override
        public Handler[] init() {
            return new Handler[] {new LengthFieldBasedDecoder(0, 4), 
                new StringDecoder(), new SimpleInBoundHandler()};
        }
    }).start();
}
```

## SimpleInBoundHandler

简单地打印出事件触发以及收到的消息:

```java
public class SimpleInBoundHandler extends InBoundHandlerAdapter {
    @Override
    public void channelActive(HandlerContext context) {
        System.out.println("channel active");
    }
    @Override
    public void channelInActive(HandlerContext context) {
        System.out.println("channel inActive");
    }
    @Override
    public void channelRead(Object message, HandlerContext context) {
        System.out.println(message.toString());
    }
}
```

## 客户端

数据发送代码:

```java
@Test
public void lengthFieldBasedDecoder() throws IOException, InterruptedException {
    byte[] result = new byte[35];
    System.arraycopy(DataUtils.int2Bytes(31), 0, result, 0, 4);
    System.arraycopy("org.apache.commons.lang.builder".getBytes(), 0, result, 4, 31);
    for (int i = 0; i < 6; i++) {
        bos.write(result);
    }
    TimeUnit.SECONDS.sleep(6);
}
```

# TODO

为了确保测试用例能够通过，使用了基于端口取余的worker线程分配方式，这样保证了向client发送数据的先后顺序。目前写功能很原始，只是将经过OutboundHandler处理的数据不做任何操作直接写入，这样的问题在于：
1. OS写缓冲区满，写入失败
2. OS缓冲区可用空间不足一次性写入，要分多次写入
3. 客户端如果已断开或网络出问题怎么办，可能需要心跳检测之类的手段
