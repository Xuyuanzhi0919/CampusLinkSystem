#!/bin/bash

echo "===================================="
echo "评分功能测试脚本"
echo "===================================="
echo ""

# 从登录响应中提取Token
TOKEN=$(cat login_response.json | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "Token: $TOKEN"
echo ""

echo "=== 测试1: 查看当前资源详情（含评分） ==="
curl -s -X GET http://localhost:8080/api/v1/resource/16 \
  -H "Authorization: Bearer $TOKEN" | grep -E "averageRating|totalRatings|userRating" || echo "完整响应:"
curl -s -X GET http://localhost:8080/api/v1/resource/16 \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo ""

echo "=== 测试2: 修改评分为5星 ==="
curl -s -X POST http://localhost:8080/api/v1/resource/16/rate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"rating":5}'
echo ""
echo ""

sleep 1

echo "=== 测试3: 验证评分已更新 ==="
curl -s -X GET http://localhost:8080/api/v1/resource/16 \
  -H "Authorization: Bearer $TOKEN" | grep -E "averageRating|totalRatings|userRating" || echo "完整响应:"
curl -s -X GET http://localhost:8080/api/v1/resource/16 \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo ""

echo "=== 测试4: 修改评分为3星 ==="
curl -s -X POST http://localhost:8080/api/v1/resource/16/rate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"rating":3}'
echo ""
echo ""

sleep 1

echo "=== 测试5: 验证评分已更新为3星 ==="
curl -s -X GET http://localhost:8080/api/v1/resource/16 \
  -H "Authorization: Bearer $TOKEN" | grep -E "averageRating|totalRatings|userRating"
echo ""
echo ""

echo "=== 测试6: 取消评分（rating=0） ==="
curl -s -X POST http://localhost:8080/api/v1/resource/16/rate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"rating":0}'
echo ""
echo ""

sleep 1

echo "=== 测试7: 验证评分已取消 ==="
curl -s -X GET http://localhost:8080/api/v1/resource/16 \
  -H "Authorization: Bearer $TOKEN" | grep -E "averageRating|totalRatings|userRating"
echo ""
echo ""

echo "=== 测试8: 重新评分为2星 ==="
curl -s -X POST http://localhost:8080/api/v1/resource/16/rate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"rating":2}'
echo ""
echo ""

sleep 1

echo "=== 测试9: 最终验证 ==="
curl -s -X GET http://localhost:8080/api/v1/resource/16 \
  -H "Authorization: Bearer $TOKEN" | grep -E "averageRating|totalRatings|userRating"
echo ""
echo ""

echo "=== 测试10: 测试边界值（6星，应该失败） ==="
curl -s -X POST http://localhost:8080/api/v1/resource/16/rate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"rating":6}'
echo ""
echo ""

echo "=== 测试11: 测试边界值（-1星，应该失败） ==="
curl -s -X POST http://localhost:8080/api/v1/resource/16/rate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"rating":-1}'
echo ""
echo ""

echo "===================================="
echo "测试完成！"
echo "===================================="
