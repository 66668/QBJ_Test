
# 升级5.8.7修改说明
5.8.5发现的问题
1. 笔记内容缺失（h5导致的）
2. 添加笔记导出功能
3. 详情无加载动画
4. 


## 新功能设计

1. 离线笔记需求设计：
    离线笔记保存在sd卡文件中，卸载软件重新登陆，不影响丢失，但是会有被当作垃圾残留，容易被安全软件清除。
    离线笔记新界面添加，新界面listview。
    登陆后，同步到后台，完成后判断是否需要删除离线临时日记

2. 备份功能设计
    （1）提示：
    
    升级前先备份，备份分为：本地缓存数据备份和服务器数据备份。
    
    缓存备份只支持完全同步后的备份，备份占用手机存储，所以不建议使用备份。
    
    （2）分类：转移备份（功能：用途说明,备份设置，开始备份，还原笔记，导出设备)
    
        用途说明：用于将笔记从数据库中拷贝出来，备份成txt格式，为转移到其他笔记软件提供附件支持。txt可设置大小。
        每次点击备份，都将删除上一次的备份存储。备份过程中断后，将重新删除重新备份
        
        备份设置： 3m大小 5m大小，不限制大小
        
        开始备份：逻辑，先删除之前的备份文件，重新备份，备份中断，将重新备份。
        
        还原笔记：先检测是否有备份，有的话开始将备份转为数据库，并以最新修改日期，上传到后台数据库，修改后台数据库数据。
        
        导出设备：弹窗提示文件路径：由用户自己查找路径，自己拷贝出手机设备
        
    （3）分类：升级同步备份（功能：用途说明，创建备份，开始增量备份，还原笔记，导出设备）
        用途说明：该备份相当于您手机当作了最终数据库，一切同步都以该备份为标准，如果卸载重新安装后，获取的后台笔记和该备份不同，将以备份为主重新更新。
        每次升级前，都将强制跳转到本功能，避免同步后，后台数据丢失后的反悔操作。每次刷新同步前，先将新笔记保存到备份中，再同步。
      
## 待优化 深度优化

1. 数据库操作那块下个版本可以考虑重构下。保留最新即可。
2. 解锁的代码是否有更简便的方式，待验证


## 框架
架构设计为：Retrofit2+Okhttp3+Rxjava2+Dagger2




