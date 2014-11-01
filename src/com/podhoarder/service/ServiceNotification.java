package com.podhoarder.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.object.Feed;
import com.podhoarderproject.podhoarder.R;

public class ServiceNotification
{
  private final static int ID_REMOTESERVICE = 1;
  private RemoteViews _smallView, _bigView;
  private Notification _notification;
  private Intent _playIntent, _navigateIntent, _forwardIntent, _backwardIntent, _closeIntent;
  private PendingIntent _playPendingIntent, _navigatePendingIntent, _forwardPendingIntent, _backwardPendingIntent, _closePendingIntent;


  public ServiceNotification(PodHoarderService $context)
  {
	_navigateIntent = new Intent($context, LibraryActivity.class);
	_navigateIntent.setAction("navigate_player");
	_navigatePendingIntent = PendingIntent.getActivity($context, 0, _navigateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	  
    _playIntent = new Intent($context, PodHoarderService.class);
    _playIntent.setAction("play");
    _playPendingIntent = PendingIntent.getService($context, 0, _playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    
    _forwardIntent = new Intent($context, PodHoarderService.class);
    _forwardIntent.setAction("forward");
    _forwardPendingIntent = PendingIntent.getService($context, 0, _forwardIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    
    _backwardIntent = new Intent($context, PodHoarderService.class);
    _backwardIntent.setAction("backward");
    _backwardPendingIntent = PendingIntent.getService($context, 0, _backwardIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    
    _closeIntent = new Intent($context, PodHoarderService.class);
    _closeIntent.setAction("close");
    _closePendingIntent = PendingIntent.getService($context, 0, _closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    
    Feed currentFeed = $context.mDataManager.getFeed($context.mCurrentEpisode.getFeedId());
    
    _smallView = new RemoteViews($context.getPackageName(), R.layout.service_notification);
    _smallView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);
    _smallView.setOnClickPendingIntent(R.id.service_notification_closeButton, _closePendingIntent);
    _smallView.setImageViewBitmap(R.id.service_notification_image, currentFeed.getFeedImage().imageObject());
    _smallView.setTextViewText(R.id.service_notification_title, $context.mCurrentEpisode.getTitle());
    _smallView.setTextViewText(R.id.service_notification_subtitle, currentFeed.getTitle());

    _bigView = new RemoteViews($context.getPackageName(), R.layout.service_notification_expanded);
    _bigView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);
    _bigView.setOnClickPendingIntent(R.id.service_notification_forwardbutton, _forwardPendingIntent);
    _bigView.setOnClickPendingIntent(R.id.service_notification_backwardbutton, _backwardPendingIntent);
    _bigView.setOnClickPendingIntent(R.id.service_notification_closeButton, _closePendingIntent);
    _bigView.setImageViewBitmap(R.id.service_notification_image, currentFeed.getFeedImage().imageObject());
    _bigView.setTextViewText(R.id.service_notification_title, $context.mCurrentEpisode.getTitle());
    _bigView.setTextViewText(R.id.service_notification_subtitle, currentFeed.getTitle());
    //_bigView.setTextViewText(R.id.service_notification_apptitle, "by " + $context.getText(R.string.app_name));

    Resources res = $context.getResources();
    NotificationCompat.Builder builder = new NotificationCompat.Builder($context).setSmallIcon(R.drawable.ic_launcher)
                                                                                 .setLargeIcon(currentFeed.getFeedImage().thumbnail())
                                                                                 .setTicker(res.getString(R.string.app_name))
                                                                                 .setContentTitle(res.getString(R.string.app_name))
                                                                                 .setCategory(Notification.CATEGORY_TRANSPORT)
                                                                                 .setContentIntent(_navigatePendingIntent);
    _notification = builder.build();
    _notification.contentView = _smallView;
    _notification.bigContentView = _bigView;
    _notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
  }


  public void showNotify(Service $context)
  {
      if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
          _smallView.setInt(R.id.service_notification_playpausebutton, "setImageResource", R.drawable.ic_pause_white_24dp);
          _bigView.setInt(R.id.service_notification_playpausebutton, "setImageResource", R.drawable.ic_pause_white_24dp);
      }
	  else {
          _smallView.setInt(R.id.service_notification_playpausebutton, "setImageResource", R.drawable.ic_pause_black_24dp);
          _bigView.setInt(R.id.service_notification_playpausebutton, "setImageResource", R.drawable.ic_pause_black_24dp);
      }

    _playIntent.setAction("pause");
    _playPendingIntent = PendingIntent.getService($context, 0, _playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    _smallView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);
    _bigView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);

    $context.startForeground(ID_REMOTESERVICE, _notification);
  }


  public void pauseNotify(Service $context)
  {
      if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
          _smallView.setInt(R.id.service_notification_playpausebutton, "setImageResource", R.drawable.ic_play_arrow_white_24dp);
          _bigView.setInt(R.id.service_notification_playpausebutton, "setImageResource", R.drawable.ic_play_arrow_white_24dp);
      }
      else {
          _smallView.setInt(R.id.service_notification_playpausebutton, "setImageResource", R.drawable.ic_play_arrow_black_24dp);
          _bigView.setInt(R.id.service_notification_playpausebutton, "setImageResource", R.drawable.ic_play_arrow_black_24dp);
      }


    _playIntent.setAction("play");
    _playPendingIntent = PendingIntent.getService($context, 0, _playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    _smallView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);
    _bigView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);

    $context.startForeground(ID_REMOTESERVICE, _notification);
  }
}
