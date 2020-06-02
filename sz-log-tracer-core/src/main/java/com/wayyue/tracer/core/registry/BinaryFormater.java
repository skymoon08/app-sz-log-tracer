package com.wayyue.tracer.core.registry;

import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.utils.ByteArrayUtils;
import io.opentracing.propagation.Format;

import java.nio.ByteBuffer;

/**
 * BinaryFormater
 * <p>
 *     Note: only supports the heap memory does not support outside the heap memory
 * </p>
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class BinaryFormater implements RegistryExtractorInjector<ByteBuffer> {

    /**
     * As the keyword key or header identification information of the cross-process transmission field,
     * its value is the serialization representation of {@link com.wayyue.tracer.core.context.span.SzTracerSpanContext}: Sz tracer head
     *
     * Converted to bytecode, this set of bytecodes will be used as the start of the spanContext in the byteArray
     */
    private static final byte[] FORMATER_KEY_HEAD_BYTES = FORMATER_KEY_HEAD.getBytes(SzTracerConstant.DEFAULT_UTF8_CHARSET);

    @Override
    public Format<ByteBuffer> getFormatType() {
        return Format.Builtin.BINARY;
    }

    @Override
    public SzTracerSpanContext extract(ByteBuffer carrier) {
        if (carrier == null || carrier.array().length < FORMATER_KEY_HEAD_BYTES.length) {
            return SzTracerSpanContext.rootStart();
        }
        byte[] carrierDatas = carrier.array();
        //head
        byte[] formaterKeyHeadBytes = FORMATER_KEY_HEAD_BYTES;
        int index = ByteArrayUtils.indexOf(carrierDatas, formaterKeyHeadBytes);
        if (index < 0) {
            return SzTracerSpanContext.rootStart();
        }
        try {
            //(UTF-8)Put the head from 0
            carrier.position(index + formaterKeyHeadBytes.length);
            //value byte arrays
            byte[] contextDataBytes = new byte[carrier.getInt()];
            carrier.get(contextDataBytes);
            String spanContextInfos = new String(contextDataBytes, SzTracerConstant.DEFAULT_UTF8_CHARSET);
            return SzTracerSpanContext.deserializeFromString(spanContextInfos);
        } catch (Exception e) {
            SelfDefineLog.error("BinaryFormater.extract Error.Recover by root start", e);
            return SzTracerSpanContext.rootStart();
        }
    }

    @Override
    public void inject(SzTracerSpanContext spanContext, ByteBuffer carrier) {
        if (carrier == null || spanContext == null) {
            return;
        }
        //head
        carrier.put(FORMATER_KEY_HEAD_BYTES);
        String spanContextInfos = spanContext.serializeSpanContext();
        byte[] value = spanContextInfos.getBytes(SzTracerConstant.DEFAULT_UTF8_CHARSET);
        //length
        carrier.putInt(value.length);
        //data
        carrier.put(value);
    }

}
