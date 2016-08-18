# HelloDaemon
### Android 进程常驻/保活

#### 本示例中使用的保活方法部分来源于下面的博客和库。启动前台服务而不显示通知来自于D-clock的AndroidDaemonService，对其他的一些非native层保活方法进行了实现。

[Android 进程常驻（2）----细数利用android系统机制的保活手段](http://blog.csdn.net/marswin89/article/details/50890708)

[D-clock / AndroidDaemonService](https://github.com/D-clock/AndroidDaemonService)

## 实现了上面 2 个链接中的大多数保活思路（非native层）

#### 1、将Service设置为前台服务而不显示通知

> D-clock :
>  
思路一：API < 18，启动前台Service时直接传入new Notification()；
>
思路二：API >= 18，同时启动两个id相同的前台Service，然后再将后启动的Service做stop处理；

#### 2.在 Service 的 onStartCommand 方法里返回 START_STICKY

#### 3.覆盖 Service 的 onDestroy/onTaskRemoved 方法, 销毁后重新拉起服务

#### 4.监听 3 种系统广播 : BOOT\_COMPLETED, CONNECTIVITY\_CHANGE, USER\_PRESENT

在系统启动完成、网络连接改变、用户屏幕解锁时拉起 Service。Service 内部做了判断，若 Service 已在运行，不会重复启动。

#### 5.设置闹钟 : 每 5 分钟检查一次服务是否在运行，如果不在运行就拉起来

#### 6.简单守护开机广播

详见上面的 2 个链接。

## 增加实现 :

#### \+ 增强对国产机型的适配

测试机型 : 华为 荣耀6 Plus, 应用未加入白名单.

>
观察到 :
>
在未加入白名单的情况下，按Back键回到桌面再锁屏后几秒钟即会杀掉进程；
>
但是按Home键返回桌面的话，即使锁屏，也不会杀掉进程。

因此，重写了onBackPressed方法，使其只是返回到桌面，而不是将当前Activity finish/destroy掉。

>
观察到 :
>
在未加入白名单的情况下，在最近任务列表中划掉卡片，
>
会回调Activity.onDestroy和Service.onTaskRemoved。

因此，重写了Activity.onDestroy，重新拉起服务；重写了Service.onTaskRemoved，使其跟Service.onDestroy的逻辑一样 : 取消对任务的订阅，然后重新拉起服务。

测试机型 : 红米 1S 4G, 神隐模式开启.

应用未加入白名单的情况下，不在最近任务列表中划掉卡片或点击全部清理，服务可一直存活；划掉卡片或点击全部清理，服务会死掉并不再重启。

应用加入白名单的情况下，划掉卡片或点击全部清理，服务会销毁并重新启动起来。

建议加入引导页来引导用户加入MIUI的自启动白名单。

#### \+ 双Service双进程守护

#### \+ WorkService和WakeUpReceiver在:work子进程中运行，WatchDogService在:watch子进程中运行，与包含UI界面的主进程分离，更不容易被杀

#### \+ 做了防止重复启动Service的处理，可以任意调用startService(Intent i)

若服务还在运行，就什么也不做；若服务不在运行就拉起来。

#### \+ 在子线程中运行定时任务，处理了运行前检查和销毁时保存的问题

开始任务前，先检查磁盘中是否有上次销毁时保存的数据；

在onDestroy/onTaskRemoved中取消订阅时，把数据保存到磁盘，并进行清理工作；

详见代码及注释。
