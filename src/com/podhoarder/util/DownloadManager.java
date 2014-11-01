package com.podhoarder.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.podhoarder.datamanager.DataManager;
import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarderproject.podhoarder.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {

    private static          String      LOG_TAG = "com.podhoarder.util.DownloadManager";

    private                 Context     mContext;
    private                 android.app.DownloadManager mDownloadManager;

    private                 List<BroadcastReceiver> mBroadcastReceivers;
    private                 EpisodeDBHelper         mEpisodesDBHelper;

    private 				String 		storagePath;
    private 				String 		podcastDir;

    private DataManager mDataManager;


    public DownloadManager (Context mContext, DataManager mParentManager, EpisodeDBHelper mEPH)
    {
        this.mContext = mContext;
        mDownloadManager = (android.app.DownloadManager) this.mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        mBroadcastReceivers = new ArrayList<BroadcastReceiver>();

        mEpisodesDBHelper = mEPH;

        mDataManager = mParentManager;

        //TODO: Select external storage (preferable) or internal app memory depending on availability.
        storagePath = Environment.DIRECTORY_PODCASTS;
        podcastDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath();
    }

    /**
     * Checks the status of the latest download.
     * @param dwnId Id of the download request.
     * @param ep Episode to check status for.
     * @param source BroadCast receiver that is listening to the download.
     */
    private void checkDownloadStatus(long dwnId, Episode ep, BroadcastReceiver source)
    {
        android.app.DownloadManager.Query query = new android.app.DownloadManager.Query();
        query.setFilterById(dwnId);
        Cursor cursor = mDownloadManager.query(query);
        if(cursor.moveToFirst())
        {
            int columnIndex = cursor.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            int columnReason = cursor.getColumnIndex(android.app.DownloadManager.COLUMN_REASON);
            int reason = cursor.getInt(columnReason);

            switch(status)
            {
                case android.app.DownloadManager.STATUS_FAILED:
                    String failedReason = "";
                    switch(reason){
                        case android.app.DownloadManager.ERROR_CANNOT_RESUME:
                            failedReason = "ERROR_CANNOT_RESUME";
                            break;
                        case android.app.DownloadManager.ERROR_DEVICE_NOT_FOUND:
                            failedReason = "ERROR_DEVICE_NOT_FOUND";
                            break;
                        case android.app.DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                            failedReason = "ERROR_FILE_ALREADY_EXISTS";
                            break;
                        case android.app.DownloadManager.ERROR_FILE_ERROR:
                            failedReason = "ERROR_FILE_ERROR";
                            break;
                        case android.app.DownloadManager.ERROR_HTTP_DATA_ERROR:
                            failedReason = "ERROR_HTTP_DATA_ERROR";
                            break;
                        case android.app.DownloadManager.ERROR_INSUFFICIENT_SPACE:
                            failedReason = "ERROR_INSUFFICIENT_SPACE";
                            break;
                        case android.app.DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                            failedReason = "ERROR_TOO_MANY_REDIRECTS";
                            break;
                        case android.app.DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                            failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                            break;
                        case android.app.DownloadManager.ERROR_UNKNOWN:
                            failedReason = "ERROR_UNKNOWN";
                            break;
                    }
                    ToastMessages.DownloadFailed(this.mContext).show();
                    Log.i(LOG_TAG, "FAILED: " + failedReason);
                    break;

                case android.app.DownloadManager.STATUS_PAUSED:
                    String pausedReason = "";
                    switch(reason)
                    {
                        case android.app.DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                            pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                            break;
                        case android.app.DownloadManager.PAUSED_UNKNOWN:
                            pausedReason = "PAUSED_UNKNOWN";
                            break;
                        case android.app.DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                            pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                            break;
                        case android.app.DownloadManager.PAUSED_WAITING_TO_RETRY:
                            pausedReason = "PAUSED_WAITING_TO_RETRY";
                            break;
                    }
                    Log.i(LOG_TAG, "DOWNLOAD PAUSED: " + pausedReason);
                    break;
                case android.app.DownloadManager.STATUS_PENDING:
                    Log.i(LOG_TAG, "DOWNLOAD PENDING");
                    break;
                case android.app.DownloadManager.STATUS_RUNNING:
                    Log.i(LOG_TAG, "DOWNLOAD RUNNING");
                    break;
                case android.app.DownloadManager.STATUS_SUCCESSFUL:
                    //Download was successful. We should update db etc.
                    downloadCompleted(ep, source);
                    break;
            }
        }
    }

    /**
     * Downloads a Podcast using the a stored URL in the db.
     * Podcasts are placed in the public Podcasts-directory.
     * @param ep Episode object to download file for.
     */
    public void downloadEpisode(Episode ep)
    {
        if (NetworkUtils.isDownloadManagerAvailable(this.mContext) && !new File(ep.getLocalLink()).exists())
        {
            String url = ep.getLink();
            android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(Uri.parse(url));
            request.setDescription(this.mContext.getString(R.string.notification_download_in_progress));
            request.setTitle(ep.getTitle());
            // in order for this if to run, you must use the android 3.2 to compile your app
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE);
            }
            //TODO: If there is no sdcard, DownloadManager will throw an exception because it cannot save to a non-existing directory. Make sure the directory is valid or something.
            request.setDestinationInExternalPublicDir(this.storagePath, FileUtils.sanitizeFileName(ep.getTitle())  + ".mp3");

            // register broadcast receiver for when the download is done.
            final Episode epTemp = ep;
            mBroadcastReceivers.add(new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    // .mp3 files was successfully downloaded. We should update db and list objects to reflect this.
                    Long dwnId = intent.getLongExtra(android.app.DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    checkDownloadStatus(dwnId, epTemp, this);
                }
            });
            mContext.registerReceiver(mBroadcastReceivers.get(mBroadcastReceivers.size()-1), new IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            mDownloadManager.enqueue(request);
            ToastMessages.DownloadingPodcast(this.mContext).show();
        }
        else
        {
            Log.w(LOG_TAG, "Podcast already exists locally. No need to download.");
        }
    }

    /**
     * A function that is called when a Podcast download is completed.
     * @param ep Episode that was downloaded.
     * @param receiver The broadcast receiver that receiver the broadcast.
     */
    private void downloadCompleted(Episode ep, BroadcastReceiver receiver)
    {
        //update list adapter object
        ep.setLocalLink(this.podcastDir + "/" + FileUtils.sanitizeFileName(ep.getTitle()) + ".mp3");

        //update db entry
        mEpisodesDBHelper.updateEpisode(ep);
        Log.i(LOG_TAG, "download completed: " + ep.getTitle());
        mContext.unregisterReceiver(receiver);
        mBroadcastReceivers.remove(receiver);

        mDataManager.forceReloadListData(true);
    }
}
