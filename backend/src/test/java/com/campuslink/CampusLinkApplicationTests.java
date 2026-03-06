package com.campuslink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * 应用上下文加载测试
 *
 * <p>使用 MOCK WebEnvironment 类型，避免启动真实的 Servlet 容器，
 * 提高测试速度并避免 WebSocket ServerEndpointExporter 在测试环境中的依赖问题。</p>
 *
 * <p>通过 {@code websocket.server-endpoint.enabled=false} 显式禁用 WebSocket 端点导出器，
 * 避免因 MOCK 环境中缺少 ServerContainer 而导致测试失败。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = "websocket.server-endpoint.enabled=false")
class CampusLinkApplicationTests {

	@Test
	void contextLoads() {
	}

}
