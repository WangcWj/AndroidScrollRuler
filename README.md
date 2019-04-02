# AndroidScrollRuler
####一 功能：

自定义View实现跟随手指滚动的刻度尺，实现了类似SeekBar的滑动选中效果。通过设置最小值跟最大值的范围，然后选择offset值，ScrollRuler将根据这些数据去计算出需要几个小刻度和几个大刻度，每个大刻度上面显示当前所表示的值。指针停留在View的中心坐标，只会停留再大刻度上面，当滑动停止后，会根据四舍五入去选择自动滑动到那个刻度上面。支持范围越界回弹，支持设置默认值。

![](https://raw.githubusercontent.com/WangcWj/image-folder/master/scrollruler.gif)
