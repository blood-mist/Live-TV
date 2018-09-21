package androidtv.livetv.stb.utils;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class DisposableManager {

    private static CompositeDisposable compositeDisposable;
    private static CompositeDisposable  liveCompositeDisposable;
    private static CompositeDisposable epgCompositeDisposable;

    public static void add(Disposable disposable) {
        getCompositeDisposable().add(disposable);
    }

    public static void addVideoPlayDisposable(Disposable liveStreamDisposable){
        getLiveStreamDisposable().add(liveStreamDisposable);

    }

    public static void addEpgDisposable(Disposable epgDisposable){
        getEpgCompositeDisposable().add(epgDisposable);
    }

    public static void dispose() {
        getCompositeDisposable().dispose();
    }

    public static  void disposeLive(){
        getLiveStreamDisposable().dispose();
    }

    public static void disposeEpg(){
        getEpgCompositeDisposable().dispose();
    }

    private static CompositeDisposable getCompositeDisposable() {
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        return compositeDisposable;
    }

    private static CompositeDisposable getLiveStreamDisposable() {
        if (liveCompositeDisposable == null || liveCompositeDisposable.isDisposed()) {
            liveCompositeDisposable = new CompositeDisposable();
        }
        return liveCompositeDisposable;
    }

    private static  CompositeDisposable getEpgCompositeDisposable(){
        if(epgCompositeDisposable==null ||  epgCompositeDisposable.isDisposed())
            epgCompositeDisposable=new CompositeDisposable();
        return epgCompositeDisposable;
    }
    private DisposableManager() {}
}
