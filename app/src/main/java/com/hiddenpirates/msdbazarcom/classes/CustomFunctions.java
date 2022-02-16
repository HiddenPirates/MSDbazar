package com.hiddenpirates.msdbazarcom.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hiddenpirates.msdbazarcom.BuildConfig;
import com.hiddenpirates.msdbazarcom.R;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Objects;

public class CustomFunctions {

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void checkForUpdate(Context context) {

        new Thread(() -> {

            String latestVersion;
            String currentVersion;

            try {
                latestVersion = Objects.requireNonNull(Jsoup.connect("https://play.google.com/store/apps/details?id=" + context.getPackageName() + "&hl=en")
                        .timeout(30000).get()
                        .select("div.hAyfc:nth-child(4)>span:nth-child(2) > div:nth-child(1)> span:nth-child(1)").first()).ownText();

                if (!latestVersion.equals("")) {

                    currentVersion = BuildConfig.VERSION_NAME;

                    double cVersion = Double.parseDouble(currentVersion);
                    double lVersion = Double.parseDouble(latestVersion);

                    if (lVersion > cVersion) {

                        ((AppCompatActivity) context).runOnUiThread(() -> {

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(context.getResources().getString(R.string.app_name));
                            builder.setMessage("New update found!");
                            builder.setCancelable(false);

                            builder.setPositiveButton("Update", (dialog, which) -> {
                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
                                dialog.dismiss();
                            });

                            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                            AlertDialog alert = builder.create();
                            alert.show();

                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
