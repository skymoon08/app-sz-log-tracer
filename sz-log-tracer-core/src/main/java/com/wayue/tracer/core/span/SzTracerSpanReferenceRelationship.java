package com.wayue.tracer.core.span;

import com.wayue.tracer.core.context.span.SzTracerSpanContext;
import com.wayue.tracer.core.utils.AssertUtils;
import com.wayue.tracer.core.utils.StringUtils;

/**
 * Copyright (C), 上海维跃信息科技有限公司
 * @fileName: SzTracerSpanReferenceRelationship
 * @author:   zhanglong
 * @since :     2020/5/27
 * @description:
 */
public class SzTracerSpanReferenceRelationship {

    private SzTracerSpanContext szTracerSpanContext;


    /**
     * {@link io.opentracing.References}
     */
    private String referenceType;



    public SzTracerSpanReferenceRelationship(SzTracerSpanContext szTracerSpanContext,
                                               String referenceType) {
        AssertUtils.isTrue(szTracerSpanContext != null,
                "szTracerSpanContext can't be null in SzTracerSpanReferenceRelationship");
        AssertUtils.isTrue(!StringUtils.isBlank(referenceType), "ReferenceType can't be null");
        this.szTracerSpanContext = szTracerSpanContext;
        this.referenceType = referenceType;
    }

    public SzTracerSpanContext getSzTracerSpanContext() {
        return szTracerSpanContext;
    }

    public void setSzTracerSpanContext(SzTracerSpanContext szTracerSpanContext) {
        this.szTracerSpanContext = szTracerSpanContext;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }
}
