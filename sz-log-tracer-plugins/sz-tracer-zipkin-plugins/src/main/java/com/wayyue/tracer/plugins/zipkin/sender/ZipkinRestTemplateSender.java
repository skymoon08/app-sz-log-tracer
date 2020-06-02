package com.wayyue.tracer.plugins.zipkin.sender;

import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;
import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.BytesMessageEncoder;
import zipkin2.reporter.Sender;

import java.net.URI;
import java.util.List;

/**
 * ZipkinRestTemplateSender
 *
 * @author guolei.sgl
 */
public class ZipkinRestTemplateSender extends Sender {

    private RestTemplate restTemplate;
    private String url;

    public ZipkinRestTemplateSender(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v2/spans";
    }

    @Override
    public Encoding encoding() {
        return Encoding.JSON;
    }

    @Override
    public int messageMaxBytes() {
        // Max span size is 2MB
        return 2 * 1024 * 1024;
    }

    @Override
    public int messageSizeInBytes(List<byte[]> spans) {
        return encoding().listSizeInBytes(spans);
    }

    @Override
    public Call<Void> sendSpans(List<byte[]> encodedSpans) {
        try {
            byte[] message = BytesMessageEncoder.JSON.encode(encodedSpans);
            post(message);
        } catch (Throwable e) {
            SelfDefineLog.error("Failed to report span to remote server. Current rest url is " + url, e);
        }
        return Call.create(null);
    }

    private void post(byte[] json) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<byte[]> requestEntity = new RequestEntity<byte[]>(json, httpHeaders,
                HttpMethod.POST, URI.create(this.url));
        this.restTemplate.exchange(requestEntity, String.class);
    }
}
