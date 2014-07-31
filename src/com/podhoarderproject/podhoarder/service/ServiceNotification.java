package com.podhoarderproject.podhoarder.service;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.activity.MainActivity;
import com.podhoarderproject.podhoarder.util.Feed;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class ServiceNotification
{
  private final static int ID_REMOTESERVICE = 1;
  private RemoteViews _smallView, _bigView;
  private Notification _notification;
  private Intent _playIntent, _navigateIntent;
  private PendingIntent _playPendingIntent, _navigatePendingIntent;


  public ServiceNotification(PodHoarderService $context)
  {
	_navigateIntent = new Intent($context, MainActivity.class);
	_navigateIntent.setAction("navigate_player");
	_navigatePendingIntent = PendingIntent.getActivity($context, 0, _navigateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	  
    _playIntent = new Intent($context, PodHoarderService.class);
    _playIntent.setAction("play");
    _playPendingIntent = PendingIntent.getService($context, 0, _playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Feed currentFeed = $context.helper.getFeed($context.currentEpisode.getFeedId());
    
    _smallView = new RemoteViews($context.getPackageName(), R.layout.service_notification);
    _smallView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);
    _smallView.setImageViewBitmap(R.id.service_notification_image, currentFeed.getFeedImage().imageObject());
    _smallView.setTextViewText(R.id.service_notification_title, $context.currentEpisode.getTitle());
    _smallView.setTextViewText(R.id.service_notification_subtitle, currentFeed.getTitle());
    //_smallView.setTextViewText(R.id.service_notification_apptitle, $context.getText(R.string.app_name));

    _bigView = new RemoteViews($context.getPackageName(), R.layout.service_notification);
    _bigView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);
    _bigView.setImageViewBitmap(R.id.service_notification_image, currentFeed.getFeedImage().imageObject());
    _bigView.setTextViewText(R.id.service_notification_title, $context.currentEpisode.getTitle());
    _bigView.setTextViewText(R.id.service_notification_subtitle, currentFeed.getTitle());
    _bigView.setTextViewText(R.id.service_notification_apptitle, "by " + $context.getText(R.string.app_name));

    Resources res = $context.getResources();
    NotificationCompat.Builder builder = new NotificationCompat.Builder($context).setSmallIcon(R.drawable.ic_launcher)
                                                                                 .setLargeIcon(currentFeed.getFeedImage().imageObject())
                                                                                 .setTicker(res.getString(R.string.app_name))
                                                                                 .setContentTitle(res.getString(R.string.app_name))
                                                                                 .setContentIntent(_navigatePendingIntent);
    _notification = builder.build();
    _notification.contentView = _smallView;
    _notification.bigContentView = _bigView;
    _notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
  }


  public void showNotify(Service $context)
  {
//    _smallView.setImageViewResource(R.id.service_notification_playpausebutton, R.drawable.white_player_play);
//    _bigView.setImageViewResource(R.id.service_notification_playpausebutton, R.drawable.white_player_play);
	  _smallView.setInt(R.id.service_notification_playpausebutton, "setBackgroundResource", R.drawable.white_player_pause);
	  _bigView.setInt(R.id.service_notification_playpausebutton, "setBackgroundResource", R.drawable.white_player_pause);

    _playIntent.setAction("pause");
    _playPendingIntent = PendingIntent.getService($context, 0, _playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    _smallView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);
    _bigView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);

    $context.startForeground(ID_REMOTESERVICE, _notification);
  }


  public void pauseNotify(Service $context)
  {
//    _smallView.setImageViewResource(R.id.service_notification_playpausebutton, R.drawable.white_player_pause);
//    _bigView.setImageViewResource(R.id.service_notification_playpausebutton, R.drawable.white_player_pause);
	  _smallView.setInt(R.id.service_notification_playpausebutton, "setBackgroundResource", R.drawable.white_player_play);
	  _bigView.setInt(R.id.service_notification_playpausebutton, "setBackgroundResource", R.drawable.white_player_play);

    _playIntent.setAction("play");
    _playPendingIntent = PendingIntent.getService($context, 0, _playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    _smallView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);
    _bigView.setOnClickPendingIntent(R.id.service_notification_playpausebutton, _playPendingIntent);

    $context.startForeground(ID_REMOTESERVICE, _notification);
  }
}
