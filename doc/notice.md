## notice
- 记录服务层功能实现日志
- api请求数据合法性在controller层处理
- md编辑器：http://lab.lepture.com/editor/
- API的url编写参考文章：http://www.ruanyifeng.com/blog/2014/05/restful_api.html
- 各种检查在Controller层，业务由Service层实现
- 文件名统一添加前缀 "时间-" 以避免覆盖。ImageManager#saveImageToDisk

## 难点<br>
- 数据库操作可以自动回滚，但磁盘操作无法自动回滚
com.duan.blogos.service.impl.blogger.profile.GalleryServiceImpl#insertPicture
