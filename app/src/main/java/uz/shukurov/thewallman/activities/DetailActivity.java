package uz.shukurov.thewallman.activities;

import android.app.WallpaperManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import uz.shukurov.thewallman.R;
import uz.shukurov.thewallman.databinding.ActivityDetailsBinding;
import uz.shukurov.thewallman.models.PixabayImage;
import uz.shukurov.thewallman.viewmodels.PixabayImageViewModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;



public class DetailActivity extends AppCompatActivity {
    ActivityDetailsBinding activityDetailsBinding;
    public final static String PIXABAY_IMAGE = "PIXABAY_IMAGE";
    private PixabayImage image;
    private Button setWallpaper;
    private WallpaperManager mWallpaperManager;
    private Bitmap mResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        initImage();
        activityDetailsBinding.setViewmodel(new PixabayImageViewModel(image));
        setWallpaper = (Button) findViewById(R.id.setWall);
        setWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       try {
                           String imageUri = image.getWebformatURL();
                           mResult = Picasso.with(DetailActivity.this).load(imageUri).get();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }

                       mWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                       try {
                           mWallpaperManager.setBitmap(mResult);
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
               }).start();



            }
        });

    }


    private void initImage() {

        image = new Gson().fromJson(getIntent().getStringExtra(PIXABAY_IMAGE), PixabayImage.class);
    }
}
