package com.my.cyanstone.fileexplore;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button allChoose, cancelChoose, copy,delete;

    private FragmentManager fm;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_files);
        init();
    }
    private void init(){
        allChoose = (Button) findViewById(R.id.all_choose);
        allChoose.setOnClickListener(this);

        cancelChoose = (Button) findViewById(R.id.cancel_choose);
        cancelChoose.setOnClickListener(this);

        copy = (Button) findViewById(R.id.copy_files);
        copy.setOnClickListener(this);

        delete = (Button) findViewById(R.id.delete_files);
        delete.setOnClickListener(this);

        fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null) {
            fragment = new FilesListFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container,fragment)
                    .commit();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.all_choose:
                break;

            case R.id.cancel_choose:
                break;

            case R.id.copy_files:
                break;

            case R.id.delete_files:
                break;

            default:
                break;
        }
    }
}
