@REM 调用同目录下的 git-add.bat: 切换目录 + 解除所有文件状态 + 添加文件
@REM 问题描述: Terminal只显示到git add完，就不进行commit操作
@REM 原因: Windows batch 默认是「直接跳转」执行被调用的 bat，执行完就结束了，不会回来
@REM 解决: 在 git-add.bat 第一行加 @echo off 或者把调用改成 call git-add.bat(子程序调用)
call git-commit-add.bat

REM 提交并附带信息
@REM git commit -m "feat: xxx" ^
@REM          -m "" ^
@REM          -m "yyyy"
@REM 每个 -m 占一行，可用 -m "" 占空行
@REM 如果分开写命令 Windows 用 caret 行连接符(倒V) 连接, Linux 用反斜杠
git commit -m "fix( mapstruct ): 修复 OrderConverter 字段映射错误" ^
            -m "* 部分字段没忽略: 看 target 对象的属性, 一个一个对比 source 对象" ^
            -m "  - Entity 字段远多于 DTO, DTO 转 Entity 可以忽略全部字段 + 手动映射, Entity 转 DTO 可以 unmappedTargetPolicy = IGNORE 然后自动转换" ^
            -m "* 虽然 MapStruct 可以自己处理批量转换, 但推荐自己写个单次转换接口方法让它调用" ^
            -m "* 补充注释"
@REM              ^
@REM             -m "* 补充事务注解"
@REM              ^
@REM             -m "* 补充提取相关常量到 OrderConstant, RedisConstant"


REM 查看文件的追踪状态
git status



