package com.campuslink.controller;

import com.campuslink.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 位置服务
 */
@Tag(name = "位置服务")
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final ObjectMapper objectMapper;

    /**
     * 反地理编码：经纬度 → 可读地址
     * 使用 OpenStreetMap Nominatim 免费接口，无需 API Key
     */
    @Operation(summary = "反地理编码")
    @GetMapping("/reverse-geocode")
    public Result<Map<String, String>> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng) {

        try {
            String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?lat=%s&lon=%s&format=json&accept-language=zh&addressdetails=1",
                lat, lng
            );

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                // Nominatim 要求自定义 User-Agent
                .header("User-Agent", "CampusLink/1.0 (campus resource sharing app)")
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return Result.success(fallbackAddress(lat, lng));
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode address = root.path("address");

            // 按优先级提取最有意义的地址片段
            String placeName = extractPlaceName(address);
            String district = extractDistrict(address);
            String city = address.path("city").asText(address.path("county").asText(""));

            // 组装简短可读地址
            String readable = buildReadableAddress(placeName, district, city);

            Map<String, String> result = new LinkedHashMap<>();
            result.put("address", readable);
            result.put("fullAddress", root.path("display_name").asText(""));

            return Result.success(result);

        } catch (Exception e) {
            // 网络超时等情况降级返回坐标描述
            return Result.success(fallbackAddress(lat, lng));
        }
    }

    /**
     * 提取最有意义的地点名称（建筑 > 校园 > 道路）
     */
    private String extractPlaceName(JsonNode address) {
        String[] keys = {
            "amenity", "building", "university", "college", "school",
            "tourism", "leisure", "shop", "road", "pedestrian"
        };
        for (String key : keys) {
            String val = address.path(key).asText("");
            if (!val.isEmpty()) return val;
        }
        return "";
    }

    /**
     * 提取区级信息
     */
    private String extractDistrict(JsonNode address) {
        String[] keys = { "suburb", "neighbourhood", "quarter", "district" };
        for (String key : keys) {
            String val = address.path(key).asText("");
            if (!val.isEmpty()) return val;
        }
        return "";
    }

    /**
     * 组装简短可读地址：最多保留 2 段
     */
    private String buildReadableAddress(String place, String district, String city) {
        StringBuilder sb = new StringBuilder();
        if (!place.isEmpty()) sb.append(place);
        if (!district.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(district);
        }
        if (!city.isEmpty() && sb.length() == 0) {
            sb.append(city);
        }
        return sb.length() > 0 ? sb.toString() : String.format("%.4f°N, %.4f°E", 0.0, 0.0);
    }

    /**
     * 解析失败时的降级文案
     */
    private Map<String, String> fallbackAddress(double lat, double lng) {
        Map<String, String> fallback = new LinkedHashMap<>();
        fallback.put("address", String.format("📍 %.5f, %.5f", lat, lng));
        fallback.put("fullAddress", "");
        return fallback;
    }
}
