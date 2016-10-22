# HelloDaemon
### Android 服务保活/常驻 (Android Service Daemon)

#### 建议只在App的核心功能需要保活/常驻时使用。

#### 本示例中使用的保活方法部分来源于下面的博客和库。启动前台服务而不显示通知来自于D-clock的AndroidDaemonService，对其他的一些非native层保活方法进行了实现。

[Android 进程常驻（2）----细数利用android系统机制的保活手段](http://blog.csdn.net/marswin89/article/details/50890708)

[D-clock / AndroidDaemonService](https://github.com/D-clock/AndroidDaemonService)

## 实现了上面 2 个链接中的大多数保活思路 :

#### 1、将Service设置为前台服务而不显示通知

> D-clock :  
>  
思路一：API < 18，启动前台Service时直接传入new Notification()；
>   
思路二：API >= 18，同时启动两个id相同的前台Service，然后再将后启动的Service做stop处理；

//启动前台服务而不显示通知的漏洞已在 API Level 25 修复，大快人心！

前台服务相对于后台服务的优势，除了优先级的提升以外，还有一点：

在最近任务列表中划掉卡片时，前台服务不会停止；

(更新：经过测试，发现只是对于AOSP/CM/国际上对Framework层改动较小的Android系统是成立的；EMUI/MIUI等未加入白名单的情况下，划掉卡片，前台服务也会停止；加入白名单后划掉卡片的行为与国际厂商的系统相似。)

而后台服务会停止，并在稍后重新启动（onStartCommand 返回 START_STICKY 时）。

前台服务和后台服务被划掉卡片时，回调的都是 onTaskRemoved 方法。

onDestroy 方法只在 设置 -> 开发者选项 -> 正在运行的服务 里停止服务时才会回调。

#### 2.在 Service 的 onStartCommand 方法里返回 START_STICKY

#### 3.覆盖 Service 的 onDestroy/onTaskRemoved 方法, 保存数据到磁盘, 然后重新拉起服务

#### 4.监听 3 种系统广播 : BOOT\_COMPLETED, CONNECTIVITY\_CHANGE, USER\_PRESENT

在系统启动完成、网络连接改变、用户屏幕解锁时拉起 Service。

Service 内部做了判断，若 Service 已在运行，不会重复启动。

#### 5.开启守护服务 : 每 9 分钟检查一次服务是否在运行，如果不在运行就拉起来

#### 6.简单守护开机广播

详见上面的 2 个链接。

## 增加实现 :

#### \+ 增强对国产机型的适配 : 防止华为机型按返回键回到桌面再锁屏后几秒钟进程被杀

测试机型 : 华为 荣耀6 Plus (EMUI 3.0 Android 4.4.2), 应用未加入白名单.

>  
观察到 :
>  
在未加入白名单的情况下，按Back键回到桌面再锁屏后几秒钟即会杀掉进程；
>  
但是按Home键返回桌面的话，即使锁屏，也不会杀掉进程。

(更新：经过测试，在EMUI系统上，『即使锁屏，也不会杀掉进程』只对App的卡片还在多任务屏幕的第一屏时有效，一旦被挤到第二页及以后，锁屏后几秒钟即会杀掉进程；加入白名单后，回到桌面再锁屏后不会杀进程。)

因此，重写了onBackPressed方法，使其只是返回到桌面，而不是将当前Activity finish/destroy掉。

测试机型 : 红米1S 4G (MIUI8 稳定版 Android 4.4.2), 应用未加入白名单.

>  
观察到 :
>  
在未加入白名单的情况下，回到桌面再锁屏后不会杀进程；
>  
但划掉卡片，进程死亡并不再启动；加入白名单后，划掉卡片，服务不会停止，与CM的行为相似。

可以看出，若不想使用Native保活，引导用户加入白名单可能是比较可行的方法。

#### \+ 用 Intent 跳转

- Android Doze 模式
- 华为 自动启动管理
- 华为 锁屏后受保护的应用
- 小米 自启动管理
- 小米 神隐模式
- 三星 自启动应用程序管理
- 魅族 自启动管理
- 魅族 待机耗电管理
- Oppo 自启动管理
- Oppo 纯净后台应用管控
- Vivo 自启动管理
- Vivo 后台高耗电
- 金立 应用自启
- 乐视 自启动管理
- 乐视 应用保护
- 酷派 自启动管理
- 联想 后台管理
- 联想 后台耗电优化

配合 android.support.v7.AlertDialog 引导用户将 App 加入白名单.

#### \+ 守护服务和BroadcastReceiver运行在:watch子进程中，与主进程分离

#### \+ 工作服务运行在主进程中，免去与服务通信需使用AIDL或其他IPC方式的麻烦

参考了 Poweramp, 启动的前台服务与 UI 运行在同一进程中。

#### \+ 做了防止重复启动Service的处理，可以任意调用startService(Intent i)

若服务还在运行，就什么也不做；若服务不在运行就拉起来。

#### \+ 在子线程中运行定时任务，处理了运行前检查和销毁时保存的问题

开始任务前，先检查磁盘中是否有上次销毁时保存的数据；定期将数据保存到磁盘。

详见代码及注释。
