package com.yangyuan.wififileshareNio.Utils;

import java.util.LinkedList;

/**
 * Created by yangy on 2016/12/1.
 */
public class ThreadPool extends  ThreadGroup{
    private boolean isClosed=false;//线程池是否关闭
    private LinkedList<Runnable> workQueue;//工作队列
    private static int threadPoolID;//表示线程池ID
    private int threadID;//表示工作线程ID
    public ThreadPool(int poolSize){
        super("ThreadPool-"+(threadPoolID++));
        setDaemon(true);
        workQueue=new LinkedList<Runnable>();
        for(int i=0;i<poolSize;i++){
            new WorkThread().start();
        }
    }
    public synchronized void execute(Runnable task){
        if(isClosed){
            throw new IllegalStateException();
        }
        if(task!=null){
            workQueue.add(task);
            notify();
        }
    }
    public synchronized Runnable getTask() throws InterruptedException{
        while (workQueue.size()==0) {
            if(isClosed)return null;
            wait();
        }
        return workQueue.removeFirst();
    }
    public synchronized void closed(){
        if(!isClosed){
            isClosed=true;
            workQueue.clear();
            interrupt();//中断所有的工作线程，该方法继承自ThreadGroup类
        }
    }
    public void join(){
        synchronized(this){
            isClosed=true;
            notifyAll();//唤醒所有还在getTask()方法中等待任务的工作进程
        }
        Thread[] threads=new Thread[activeCount()];
        int count=enumerate(threads);
        for(int i=0;i<count;i++){
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
            }
        }
    }
    /**
     * 内部类：工作线程
     */
    private class WorkThread extends  Thread{
        public WorkThread(){
            super(ThreadPool.this,"WorkThread-"+(threadID++));
        }
        public void run(){
            while (!isInterrupted()) {     //判断是线程是否被中断
                Runnable task=null;
                try {
                    task=getTask();
                } catch (InterruptedException ex) {
                }
                if(task==null)return;
                try {
                    task.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
