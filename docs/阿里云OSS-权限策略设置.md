* **<u>[开通阿里云对象存储服务](https://zhuanlan.zhihu.com/p/2013536613716754986)</u>**

1. 新建 bucket 来放 sky-takeout 的上传文件，但是报错，推测是权限问题

2. 阿里云界面 → 点击右上角个人头像 → `权限与安全` → `访问控制`

3. 来到 `RAM 访问控制` → `权限管理` → `权限策略`

4. 创建/修改自己的 `权限策略`

5. 进入自己的权限策略 → `编辑权限策略` → `脚本编辑` → 使用 JSON 代码
    
    * 这是我之前 JavaWeb 用的权限策略(注意是增删改查权限)
    
    ```json
    {
      "Version": "1",
      "Statement": [
        {
          "Effect": "Allow",
          "Action": [
            "oss:PutObject",
            "oss:ListObjects",
            "oss:GetBucketStat",
            "oss:GetBucketInfo"
          ],
          "Resource": "acs:oss:*:*:javaweb-learning-20260306"
        },
        {
          "Effect": "Allow",
          "Action": [
            "oss:GetObject",
            "oss:PutObject",
            "oss:DeleteObject"
          ],
          "Resource": "acs:oss:*:*:javaweb-learning-20260306/*"
        }
      ]
    }
    ```
    
    * 修改优化一下
    
    ```json
   {
      "Version": "1",
      "Statement": [
         {
            "Effect": "Allow",
            "Action": [
               "oss:PutObject",
               "oss:ListObjects",
               "oss:GetBucketStat",
               "oss:GetBucketInfo"
            ],
            "Resource": [
               "acs:oss:*:*:javaweb-learning-20260306",
               "acs:oss:*:*:javaweb-learning-20260306/*",
               "acs:oss:*:*:sky-takeout-20260523",
               "acs:oss:*:*:sky-takeout-20260523/*"
            ]
         }
      ]
   }
    ```
   


