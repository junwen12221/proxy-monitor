package cn.lightfish.proxy;

import io.vertx.core.buffer.Buffer;

/**
 * cjw
 * 294712221@qq.com
 */
public class NetworkTrafficRecorder {

    public void record(Direction direction, Buffer buffer) {
        System.out.print("\n---------------------" +
                direction +
                "----------------------------\n");
        System.out.println(StringUtil.dumpAsHex(buffer.getBytes()));
    }


    public void close() {

    }
}
