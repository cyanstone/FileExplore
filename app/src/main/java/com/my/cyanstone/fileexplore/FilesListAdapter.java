package com.my.cyanstone.fileexplore;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by bjshipeiqing on 2016/4/15.
 */
public class FilesListAdapter extends BaseAdapter {
    private Context context;
    private List<File> files;
    private  static  HashMap<Integer,Boolean> checkMap;
    private boolean isCheckBoxVisible;
    private int checkedNum;
    private CheckBoxChangedListener listener;

    public  void setCheckBoxVisible(boolean b){
        isCheckBoxVisible = b;
    }

    public  boolean getCheckBoxVisible() {
        return isCheckBoxVisible;
    }

    public FilesListAdapter(Context context, ArrayList<File> files){
        this.context = context;
        this.files = files;
        checkMap = new HashMap<Integer,Boolean>();
        isCheckBoxVisible = false;
        listener = new CheckBoxChangedListener() {
            @Override
            public void onChanged() {

            }
        };
        initData();
    }

    public void  setOnCheckBoxChangedListener(CheckBoxChangedListener listener) {
        this.listener = listener;
    }

    public void initData() {
        for(int i = 0; i < files.size(); i++) {
            getIsChecked().put(i,false);
        }
        checkedNum = 0;
    }

    public int getCheckedNum() {
        checkedNum = 0;
        for(int i = 0; i < files.size(); i++){
            if(getIsChecked().get(i) == true) {
                checkedNum++;
            }
        }
        return checkedNum;
    }

    public static HashMap<Integer,Boolean> getIsChecked() {
        return checkMap;
    }
    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.files_list_view_item,null);
            convertView.setTag(viewHolder);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.file_image);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.file_name_text);
            viewHolder.fileInfo = (TextView) convertView.findViewById(R.id.file_info_text);
            viewHolder.isChoose = (CheckBox) convertView.findViewById(R.id.file_isCheck);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        File file = files.get(position);
        if(isCheckBoxVisible == false) {
            viewHolder.isChoose.setVisibility(View.GONE);
        } else {
            viewHolder.isChoose.setVisibility(View.VISIBLE);
        }

       // if(!file.canRead()) {
        //    FilesListFragment.RootCommand("chmod 777 " + file.getAbsolutePath());
       // }

        if(file.isDirectory()) {
            viewHolder.imageView.setImageResource(R.mipmap.icon_directory);
            File subDirectory = new File(file.getPath());
            int num = 0;
            if(subDirectory != null && subDirectory.list() != null) {
                num = subDirectory.listFiles().length;
            }
            viewHolder.fileInfo.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(file.lastModified()) + " " + num + "个文件");
        } else {
            viewHolder.imageView.setImageResource(R.mipmap.icon_file);
            String sizeInfo;
            long fileSize = file.length();
            if(fileSize > 1024 * 1024) {
                float size = fileSize / (1024 * 1024f);
                sizeInfo = new DecimalFormat("#.00").format(size) + "MB";
            } else if(fileSize >= 1024) {
                float size = fileSize / 1024;
                sizeInfo = new DecimalFormat("#.00").format(size) + "KB";
            } else {
                sizeInfo = new DecimalFormat("#.00").format(fileSize) + "B";
            }
            viewHolder.fileInfo.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(file.lastModified()) + " " + sizeInfo);
        }
        viewHolder.fileName.setText(file.getName());
        viewHolder.isChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getIsChecked().put(position, isChecked);
                listener.onChanged();
                Log.d("CheckedNum : ",checkedNum + "");
                Log.d("HashMap:", checkMap.toString());
                Log.d("Files:", files.toString());
            }
        });
        viewHolder.isChoose.setChecked(getIsChecked().get(position));
        return convertView;
    }

    class ViewHolder {
        ImageView imageView;
        TextView fileName;
        TextView fileInfo;
        CheckBox isChoose;
    }
}
