package androidtv.livetv.stb.ui.videoplay;

import android.app.Application;

import androidtv.livetv.stb.ui.splash.SplashRepository;
import androidtv.livetv.stb.utils.ApiManager;
import retrofit2.Retrofit;

public class VideoPlayRepository {

    private static VideoPlayRepository mInstance;
    private VideoPlayApiInterface videoPlayApiInterface;

    public VideoPlayRepository(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
        videoPlayApiInterface = retrofitInstance.create(VideoPlayApiInterface.class);

    }

    public static VideoPlayRepository getInstance(final Application application) {
        if (mInstance == null) {
            synchronized (SplashRepository.class) {
                if (mInstance == null) {
                    mInstance = new VideoPlayRepository(application);
                }
            }
        }
        return mInstance;
    }

}
