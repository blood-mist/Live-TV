package androidtv.livetv.stb.ui.utc;

import androidtv.livetv.stb.entity.TimeStampEntity;
import androidtv.livetv.stb.ui.splash.SplashApiInterface;
import androidtv.livetv.stb.utils.ApiManager;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GetUtc {
    private static SplashApiInterface apiInterface;
    private TimeStampEntity timeStampEntity;

    private GetUtc(SplashApiInterface apiInterface) {
        GetUtc.apiInterface = apiInterface;
    }

    public static GetUtc getInstance() {
        GetUtc getUtc= new GetUtc(apiInterface);
        return getUtc;

    }

    public TimeStampEntity getTimestamp(){
        timeStampEntity=new TimeStampEntity();
        Retrofit utcRetrofit = ApiManager.getAdapter();
        apiInterface = utcRetrofit.create(SplashApiInterface.class);
      Observable<Response<TimeStampEntity>> timeStamp = apiInterface.getTimestamp();
      timeStamp.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
              .subscribe(new Observer<Response<TimeStampEntity>>() {

                  @Override
                  public void onSubscribe(Disposable d) {

                  }

                  @Override
                  public void onNext(Response<TimeStampEntity> timeStampEntityResponse) {
                      if (timeStampEntityResponse.body() != null) {
                        timeStampEntity.setUtc((int) timeStampEntityResponse.body().getUtc());
                      }
                  }

                  @Override
                  public void onError(Throwable e) {
                    timeStampEntity.setUtc((int) System.currentTimeMillis());
                  }

                  @Override
                  public void onComplete() {
                  }
              });
      return timeStampEntity;

    }

}
