package com.wayyue.tracer.core.reporter.facade;

import com.wayyue.tracer.core.span.SzTracerSpan;

/**
 * Copyright (C), 上海维跃信息科技有限公司
 * @FileName: Reporter
 * @author :   zhanglong
 * @since :   2020/5/27
 * @Description:
 */
public interface Reporter {

        /**
         * Persistence type reported to the remote server
         */
        String REMOTE_REPORTER    = "REMOTE_REPORTER";

        /**
         * Combined reporting type
         */
        String COMPOSITE_REPORTER = "COMPOSITE_REPORTER";

        /**
         * get reporter type
         * @return
         */
        String getReporterType();

        /**
         * report span
         * @param span
         */
        void report(SzTracerSpan span);

        /**
         * turn off output ability
         */
        void close();
}
