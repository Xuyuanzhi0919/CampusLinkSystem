@echo off
echo ========================================
echo CampusLink 数据库初始化脚本
echo ========================================
echo.

set /p MYSQL_USER="请输入 MySQL 用户名 (默认: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

echo.
echo 正在初始化数据库...
echo.

mysql -u %MYSQL_USER% -p < sql\schema.sql

if %errorlevel% neq 0 (
    echo.
    echo ❌ 数据库初始化失败！
    pause
    exit /b 1
)

echo.
echo 正在插入测试数据...
echo.

mysql -u %MYSQL_USER% -p < sql\test-data.sql

if %errorlevel% neq 0 (
    echo.
    echo ❌ 测试数据插入失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ 数据库初始化完成！
echo ========================================
echo.
echo 数据库名称: campuslink
echo 已插入 3 所测试学校
echo.
pause
