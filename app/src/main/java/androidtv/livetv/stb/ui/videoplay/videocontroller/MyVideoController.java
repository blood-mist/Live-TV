package androidtv.livetv.stb.ui.videoplay.videocontroller;

import android.content.Context;
import android.media.MediaPlayer;

import androidtv.livetv.stb.entity.ChannelItem;

public class MyVideoController implements VideoControllerView.MediaPlayerControl {

    private Context context;
    private MediaPlayer player;
    private ChannelItem channelItem;
    private MediaPlayerLis listener;
    public MyVideoController(Context context, MediaPlayer player,ChannelItem item,MediaPlayerLis lis){
        this.context = context;
        this.player = player;
        this.channelItem = item;
        this.listener = lis;
    }

    @Override
    public void start() {
      listener.onDvrStart();
    }

    @Override
    public void pause() {
     listener.onDvrPause();
    }

    @Override
    public int getDuration() {
        try {
            return player.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        try {
            return player.getCurrentPosition();
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
       player.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public ChannelItem channel() {
        return this.channelItem;
    }

    public interface MediaPlayerLis{
       void onDvrStart();
       void onDvrPause();

    }
}
