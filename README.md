Progress Monitor
================
A utility to track the progress of long running operations in Java.

Overview
--------
Monitoring the progress of long running operations can become unexpectedly cumbersome. Even if all you want to do is print a status update to standard out.

How frequently should updates be printed? What if there's no change in progress? Or if the task has already finished? With _Progress Monitor_ you can take all the guess work out of the equation and write code like this:

```Java
 final ProgressMonitor progress = ProgressMonitor.create().addProgressListener(event -> ...);
 
 while (...) {
     ...
     progress.increment();
 }
 
 progress.completed();
```
Documentation
-------------
Please refer to the [Wiki](https://github.com/zleonov/progress-monitor/wiki) for details, specifications, API examples, and FAQ.

The latest API documentation can be accessed [here](https://zleonov.github.io/progress-monitor/api/latest).