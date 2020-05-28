package com.wayyue.tracer.core.appender;

import java.io.IOException;

/**
 * Copyright (C), 上海维跃信息科技有限公司
 *
 * @className: TraceAppender
 * @author:   zhanglong
 * @since  : 2020/5/27
 * @Description:
 */
public interface TraceAppender {

    /**
     * Flush Data
     *
     * @throws IOException
     */
    void flush() throws IOException;

    /**
     * Add the log file to be output
     *
     * @param log
     * @throws IOException
     */
    void append(String log) throws IOException;

    /**
     * clean log
     */
    void cleanup();

}
