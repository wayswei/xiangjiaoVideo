package com.baby.app.service;
import com.hdl.m3u8.bean.BaseListener;

public interface MyOnDownloadLisetner extends BaseListener {
    /**
     * 下载m3u8文件.
     * 注意：这个方法是异步的（子线程中执行），所以不能在此方法中回调，其他方法为主线程中回调
     *
     * @param itemFileSize 单个文件的大小
     * @param totalTs      ts总数
     * @param curTs        当前下载完成的ts个数
     */
    void onDownloading(long itemFileSize, int totalTs, long curTs);

    /**
     * 下载成功
     */
    void onSuccess();

    /**
     * 当前已经下载的文件大小
     *
     * @param curLength
     */
    void onProgress(long curLength);
}