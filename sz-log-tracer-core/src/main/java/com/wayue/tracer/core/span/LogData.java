
package com.wayue.tracer.core.span;

import java.util.Map;


/**
 * LogData
 *
 * @author zhanglong
 * @since 2020/05/27
 */
public final class LogData {

    /**
     * OpenTracing 时间类型key Nullable cs/cr/ss/sr
     */
    public static final String EVENT_TYPE_KEY = "event";

    /**
     * <b>cr</b>
     * <p> 客户端接收 - Client Receive </p>
     * <p> cr - 标识跨度结束 ，表示客户端已经成功收到服务端响应 </p>
     * <p> 如果从该时间戳中减去cs时间戳，则它将接收客户端从服务器接收响应所需的全部时间。  </p>
     */
    public static final String CLIENT_RECV_EVENT_VALUE = "cr";

    /**
     * <b>cs</b> - Client Sent. The client has made a request (a client can be e.g.
     * This annotation depicts the start of the span.
     */
    public static final String CLIENT_SEND_EVENT_VALUE = "cs";

    /**
     * <b>sr</b> - Server Receive. The server side got the request and will start
     * processing it. If one subtracts the cs timestamp from this timestamp one will
     * receive the network latency.
     */
    public static final String SERVER_RECV_EVENT_VALUE = "sr";

    /**
     * <b>ss</b> - Server Send. Annotated upon completion of request processing (when the
     * response got sent back to the client). If one subtracts the sr timestamp from this
     * timestamp one will receive the time needed by the server side to process the
     * request.
     */
    public static final String SERVER_SEND_EVENT_VALUE = "ss";

    private final long time;

    /* @Nullable eventName:value*/
    private final Map<String, ?> fields;

    public LogData(long time, Map<String, ?> fields) {
        this.time = time;
        this.fields = fields;
    }

    public long getTime() {
        return time;
    }

    public Map<String, ?> getFields() {
        return fields;
    }
}
