:: ============
:: 注: batch 文件避免用反引号(Windows 会当成命令替换符，可能出现报错)
:: ============

:: 取消命令回显(可选)
:: @echo off

:: UTF-8
chcp 65001

:: 切换到项目根目录
cd D:\Users\Programming\Java-Development\Java-Beginner-Code\Sky-Takout\sky-takeout

:: 由于上次提交后暂存区已清空，而修改文件需要手动 add 到暂存区
:: 所以暂存区仍然为空，不需要 git restore --staged . 清理暂存区

:: 补上忘提交的文件
:: git add <文件在仓库中的绝对路径>
git add sky-server/src/main/java/com/sky/mapper/CategoryMapper.java
git add sky-server/src/main/java/com/sky/service/CategoryService.java
git add sky-server/src/main/resources/com/sky/mapper/CategoryMapper.xml


:: 查看文件的追踪状态
git status

:: 提交并修改信息
:: git commit --amend -m "feat: xxx" \
::          -m "" \
::          -m "yyyy"
git commit --amend -m "fix( user.category ): 用户端查询分类应当只展示起售状态菜品" ^
            -m "" ^
            -m "* Service 方法注释补充管理端/用户端语义约束" ^
            -m "" ^
            -m "* 同时注意查询时排序"


:: 提交但不修改信息
:: git commit --amend --no-edit

