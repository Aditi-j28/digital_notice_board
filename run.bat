@echo off
echo ============================================
echo   Digital Notice Board - Build Script
echo ============================================

set SRC_DIR=src
set OUT_DIR=out
set LIB_DIR=lib
set MAIN_CLASS=com.noticeboard.Main

echo.
echo [1/3] Creating output directory...
if not exist %OUT_DIR% mkdir %OUT_DIR%

echo [2/3] Compiling Java sources...
javac -cp "%LIB_DIR%\mysql-connector-java.jar" ^
      -d %OUT_DIR% ^
      -sourcepath %SRC_DIR% ^
      %SRC_DIR%\com\noticeboard\Main.java ^
      %SRC_DIR%\com\noticeboard\db\DBConnection.java ^
      %SRC_DIR%\com\noticeboard\db\UserDAO.java ^
      %SRC_DIR%\com\noticeboard\db\NoticeDAO.java ^
      %SRC_DIR%\com\noticeboard\model\User.java ^
      %SRC_DIR%\com\noticeboard\model\Notice.java ^
      %SRC_DIR%\com\noticeboard\util\UITheme.java ^
      %SRC_DIR%\com\noticeboard\ui\LoginFrame.java ^
      %SRC_DIR%\com\noticeboard\ui\AdminDashboard.java ^
      %SRC_DIR%\com\noticeboard\ui\StudentDashboard.java ^
      %SRC_DIR%\com\noticeboard\ui\NoticeDialog.java ^
      %SRC_DIR%\com\noticeboard\ui\NoticeViewDialog.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed! Check errors above.
    pause
    exit /b 1
)

echo [3/3] Compilation successful!
echo.
echo Running Digital Notice Board...
echo.
java -cp "%OUT_DIR%;%LIB_DIR%\mysql-connector-java.jar" %MAIN_CLASS%

pause
