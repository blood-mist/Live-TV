package androidtv.livetv.stb.ui.videoplay;


import java.util.Observable;

public class ChannelChangeObserver extends Observable {
    private Boolean playNextChannel=null;
    public void setChannelNext(boolean playNext){
      playNextChannel= playNext;
    }

    public boolean  getChannelNext(){
        return playNextChannel;
    }

    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }
}
