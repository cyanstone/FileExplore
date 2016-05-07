package com.my.cyanstone.fileexplore;

import android.app.Application;
import android.content.Context;


public class MyApplication extends Application {
  private static MyApplication instance;
  private static Context context;

  @Override
  public void onCreate() {
    super.onCreate();
    //registerMockServices();
    instance = this;
    context = getApplicationContext();
  }

  public static Context getContext(){
    return context;
  }

  public static MyApplication getInstance() {
    return instance;
  }

}
