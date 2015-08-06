# PageRecyelerViewDemo
横向分页的RecyclerView，带有页码指示器。

![效果图](http://img.blog.csdn.net/20150714202531487)

**用法：**
```java
mRecyclerView = (PageRecyclerView) findViewById(R.id.cusom_swipe_view);
// 设置指示器
mRecyclerView.setIndicator((PageIndicatorView) findViewById(R.id.indicator));
// 设置行数和列数
mRecyclerView.setPageSize(3, 3);
// 设置页间距
mRecyclerView.setPageMargin(30);
```

具体请参考MainActivity.java和博客：http://blog.csdn.net/u014165119/article/details/46834265
