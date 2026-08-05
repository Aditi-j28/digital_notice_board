#!/bin/bash
echo "============================================"
echo "  Digital Notice Board - Build Script"
echo "============================================"

SRC_DIR="src"
OUT_DIR="out"
LIB_DIR="lib"
MAIN_CLASS="com.noticeboard.Main"
JAR_NAME="mysql-connector-java.jar"

echo ""
echo "[1/3] Creating output directory..."
mkdir -p "$OUT_DIR"

echo "[2/3] Compiling Java sources..."
javac -cp "$LIB_DIR/$JAR_NAME" \
      -d "$OUT_DIR" \
      -sourcepath "$SRC_DIR" \
      "$SRC_DIR/com/noticeboard/Main.java" \
      "$SRC_DIR/com/noticeboard/db/DBConnection.java" \
      "$SRC_DIR/com/noticeboard/db/UserDAO.java" \
      "$SRC_DIR/com/noticeboard/db/NoticeDAO.java" \
      "$SRC_DIR/com/noticeboard/model/User.java" \
      "$SRC_DIR/com/noticeboard/model/Notice.java" \
      "$SRC_DIR/com/noticeboard/util/UITheme.java" \
      "$SRC_DIR/com/noticeboard/ui/LoginFrame.java" \
      "$SRC_DIR/com/noticeboard/ui/AdminDashboard.java" \
      "$SRC_DIR/com/noticeboard/ui/StudentDashboard.java" \
      "$SRC_DIR/com/noticeboard/ui/NoticeDialog.java" \
      "$SRC_DIR/com/noticeboard/ui/NoticeViewDialog.java"

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Compilation failed!"
    exit 1
fi

echo "[3/3] Compilation successful!"
echo ""
echo "Running Digital Notice Board..."
echo ""
java -cp "$OUT_DIR:$LIB_DIR/$JAR_NAME" $MAIN_CLASS
