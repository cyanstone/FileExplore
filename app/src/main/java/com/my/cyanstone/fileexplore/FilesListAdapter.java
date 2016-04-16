package com.my.cyanstone.fileexplore;

import android.content.Context;
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
    private  static  HashMap<Integer,Boolean> isChecked;
    boolean isPackageRoot;

    public FilesListAdapter(Context context, ArrayList<File> files,boolean isPackageRoot){
        this.context = context;
        this.files = files;
        this.isPackageRoot = isPackageRoot;
        isChecked = new HashMap<Integer,Boolean>();
        initData();
    }

    private void initData() {
        for(int i = 0; i < files.size(); i++) {
            getIsChecked().put(i,false);
        }
    }

    public static HashMap<Integer,Boolean> getIsChecked() {
        return isChecked;
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
        if(position == 0 && !isPackageRoot) {
            viewHolder.fileName.setText("..");
            viewHolder.imageView.setImageResource(R.mipmap.icon_directory);
            viewHolder.fileInfo.setVisibility(View.GONE);
            viewHolder.isChoose.setVisibility(View.GONE);
        } else {
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
                }
            });
            viewHolder.isChoose.setChecked(getIsChecked().get(position));
        }
        return convertView;
    }

    class ViewHolder {
        ImageView imageView;
        TextView fileName;
        TextView fileInfo;
        CheckBox isChoose;
    }
}
