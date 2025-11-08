@echo off
echo ========================================
echo CampusLink 数据库检查脚本
echo ========================================
echo.

set /p MYSQL_USER="请输入 MySQL 用户名 (默认: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

echo.
echo 正在检查数据库...
echo.

mysql -u %MYSQL_USER% -p -e "SHOW DATABASES LIKE 'campuslink';"

if %errorlevel% neq 0 (
    echo.
    echo ❌ 无法连接到 MySQL 或数据库不存在！
    echo.
    echo 请先运行 init-database.bat 初始化数据库
    pause
    exit /b 1
)

echo.
echo 正在检查表...
echo.

mysql -u %MYSQL_USER% -p -e "USE campuslink; SHOW TABLES;"

echo.
echo 正在检查 school 表数据...
echo.

mysql -u %MYSQL_USER% -p -e "USE campuslink; SELECT * FROM school;"

echo.
echo 正在检查 user 表结构...
echo.

mysql -u %MYSQL_USER% -p -e "USE campuslink; DESC user;"

echo.
echo ========================================
echo ✅ 数据库检查完成！
echo ========================================
pause
