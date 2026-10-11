@REM ============
@REM 注: batch 文件避免用反引号(Windows 会当成命令替换符，可能出现报错)
@REM ============

@REM 取消命令回显(可选)
@REM @echo off

REM UTF-8
chcp 65001

REM 切换到项目根目录
cd D:\Users\Programming\Java-Development\Java-Beginner-Code\Sky-Takout\sky-takeout

REM 把暂存区所有文件移出去、解除 add 状态
git restore --staged .

REM 添加目标文件到暂存区
@REM git add <文件在仓库中的绝对路径>


git add sky-server/src/main/java/com/sky/convertor/AddressBookConverter.java
git add sky-server/src/main/java/com/sky/convertor/OrderConverter.java
git add sky-server/src/main/java/com/sky/convertor/ShoppingCartConverter.java