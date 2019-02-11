[![Build Status](https://travis-ci.org/seaswalker/MiniNetty.svg?branch=master)](https://travis-ci.org/seaswalker/netty-wheel)
[![codecov](https://codecov.io/gh/seaswalker/MiniNetty/branch/master/graph/badge.svg)](https://codecov.io/gh/seaswalker/MiniNetty)

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

