package com.campuslink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * PDF代理控制器
 * 用于代理请求外部PDF文件，解决CORS跨域问题
 */
@Slf4j
@Tag(name = "PDF代理", description = "PDF文件代理接口")
@RestController
@RequestMapping("/api/pdf")
public class PdfProxyController {

    private final RestTemplate restTemplate;

    // 允许的域名白名单（防止被滥用）
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
            "ibm.com",
            "oracle.com",
            "microsoft.com",
            "apache.org",
            "github.com",
            "githubusercontent.com",
            // 添加更多可信域名
            "edu.cn",  // 教育机构
            "ac.cn"    // 学术机构
    );

    public PdfProxyController() {
        this.restTemplate = new RestTemplate();
    }

    @Operation(summary = "代理PDF文件", description = "代理请求外部PDF文件，添加CORS头")
    @GetMapping("/proxy")
    public ResponseEntity<byte[]> proxyPdf(
            @Parameter(description = "PDF文件URL（需URL编码）")
            @RequestParam String url
    ) {
        try {
            // URL解码
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
            log.info("代理PDF请求: {}", decodedUrl);

            // 安全检查：验证URL
            if (!isAllowedUrl(decodedUrl)) {
                log.warn("非法PDF URL: {}", decodedUrl);
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("不允许访问此域名的PDF文件".getBytes(StandardCharsets.UTF_8));
            }

            // 请求外部PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_PDF, MediaType.ALL));
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    URI.create(decodedUrl),
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            // 检查响应
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("PDF下载失败: {}, 状态码: {}", decodedUrl, response.getStatusCode());
                return ResponseEntity
                        .status(response.getStatusCode())
                        .body("PDF下载失败".getBytes(StandardCharsets.UTF_8));
            }

            // 返回PDF，添加CORS头
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_PDF);
            responseHeaders.setContentLength(response.getBody().length);
            responseHeaders.set("Access-Control-Allow-Origin", "*");
            responseHeaders.set("Access-Control-Allow-Methods", "GET, OPTIONS");
            responseHeaders.set("Access-Control-Allow-Headers", "*");
            responseHeaders.setCacheControl(CacheControl.maxAge(3600, java.util.concurrent.TimeUnit.SECONDS));

            log.info("PDF代理成功: {}, 大小: {} bytes", decodedUrl, response.getBody().length);

            return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);

        } catch (Exception e) {
            log.error("PDF代理异常: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("PDF代理失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 验证URL是否在白名单中
     */
    private boolean isAllowedUrl(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();

            if (host == null) {
                return false;
            }

            // 检查是否在白名单中
            return ALLOWED_DOMAINS.stream()
                    .anyMatch(domain -> host.endsWith(domain));
        } catch (Exception e) {
            log.error("URL解析失败: {}", url, e);
            return false;
        }
    }

    @Operation(summary = "健康检查", description = "检查PDF代理服务是否正常")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PDF代理服务运行正常");
    }
}
