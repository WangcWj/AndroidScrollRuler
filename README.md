# AndroidScrollRuler
自定义View实现跟随手指滚动的刻度尺，实现了类似SeekBar的滑动选中效果。[项目解析](<https://blog.csdn.net/jsonChumpKlutz/article/details/88973025>)   
需要的可以直接下载demo运行看效果，不上传Maven仓库是因为这个效果是我想要的，但是并不是你们都想要的，这里只是提供一个基础模版，需要的话还是要使用者自己修改代码。如有碰到问题随时issues联系我～

**UI图：**![](<https://raw.githubusercontent.com/WangcWj/image-folder/master/ruler.png>)

**功能：**

- 通过设置最小值跟最大值的范围，以及offset值。View将根据这些数据去计算出需要几个小刻度和几个长刻度，和每个长刻度上面显示的数值。
- 指针可以随意的定制。
- 当滑动停止后，刻度尺会根据四舍五入将距离指针最近的长刻度滑动到指针的位置。
- 支持范围越界回弹。
- 支持设置默认值。

![](https://raw.githubusercontent.com/WangcWj/image-folder/master/scrollruler.gif)
