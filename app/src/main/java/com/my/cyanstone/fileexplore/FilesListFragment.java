package com.my.cyanstone.fileexplore;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by bjshipeiqing on 2016/4/15.
 */
public class FilesListFragment extends Fragment implements View.OnClickListener{
    private final String ROOT_PATH = "/";    // "/data/data/";// + context.getPackageName();
    private List<File> files;
    private FilesListAdapter adapter;

    private ListView fileListView;
    private Button allChoose, cancelChoose, copy,delete;
    private TextView currentPahtTv, pathFilesNumTv;
    private LinearLayout buttonsLayout;
    private Context context;
    private File fileParent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files_list, container, false);
        initView(v);
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.setOnKeyListener(backListener);
        return v;
    }

    private void initView(View v) {
        fileListView = (ListView) v.findViewById(R.id.files_list);
        fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                buttonsLayout.setVisibility(View.VISIBLE);
                adapter.setCheckBoxVisible(true);
                FilesListAdapter.getIsChecked().put(position, true);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final File file = (File) adapter.getItem(position);
                fileParent = file;
                if (file.isDirectory()) {
                    initData(file);
                } else if (file.isFile()) {
                    openFile(file);
                }
            }
        });
        TextView emptyView = new TextView(context);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        emptyView.setTextSize(24);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setText("`T_T`什么都没有`T_T`");
        emptyView.setVisibility(View.GONE);
        ((ViewGroup)fileListView.getParent()).addView(emptyView);
        fileListView.setEmptyView(emptyView);
        allChoose = (Button) v.findViewById(R.id.all_choose);
        allChoose.setOnClickListener(this);

        cancelChoose = (Button) v.findViewById(R.id.cancel_choose);
        cancelChoose.setOnClickListener(this);

        copy = (Button) v.findViewById(R.id.copy_files);
        copy.setOnClickListener(this);

        delete = (Button) v.findViewById(R.id.delete_files);
        delete.setOnClickListener(this);

        currentPahtTv = (TextView) v.findViewById(R.id.current_path);
        pathFilesNumTv = (TextView) v.findViewById(R.id.path_file_nums);

        buttonsLayout = (LinearLayout) v.findViewById(R.id.buttons_layout);

        files = new ArrayList<File>();
        String pathRoot = "chmod 777 " + ROOT_PATH;
        RootCommand(pathRoot);
        File file = new File(ROOT_PATH);
        initData(file);
    }

    private void initData(File file) {
        List<File> sortFiles = new ArrayList<File>();
        if (file.listFiles() != null) {
            sortFiles = Arrays.asList(file.listFiles());
        }
        if (sortFiles != null && sortFiles.size() > 0) {
            Collections.sort(sortFiles, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    if (lhs.isDirectory() && rhs.isFile()) {
                        return -1;
                    }
                    if (lhs.isFile() && rhs.isDirectory()) {
                        return 1;
                    }
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
        }
        if (null != sortFiles ) {
            files.clear();
            for (File c : sortFiles) {
                files.add(c);
            }
        }
        currentPahtTv.setText("当前路径为:" + file.getAbsolutePath());
        pathFilesNumTv.setText(files.size() + "项");
        adapter = new FilesListAdapter(context, (ArrayList<File>) files);
        fileListView.setAdapter(adapter);
    }

    private void openFile(File file) {

    }

    public static boolean RootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    View.OnKeyListener backListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(adapter.getCheckBoxVisible() == true && event.getAction() == KeyEvent.ACTION_DOWN) {
                    buttonsLayout.setVisibility(View.GONE);
                    adapter.setCheckBoxVisible(false);
                    adapter.notifyDataSetChanged();
                    adapter.initData();
                    return true;
            } else if(!fileParent.getPath().equals(ROOT_PATH)) {
                if(fileParent.getParent() == null) {
                    initData(new File(ROOT_PATH));
                    return true;
                } else {
                    initData(fileParent.getParentFile());
                    return true;
                }
            }
            return false;
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.all_choose:
                for(int i = 0; i < FilesListAdapter.getIsChecked().size(); i++) {
                    FilesListAdapter.getIsChecked().put(i,true);
                    adapter.notifyDataSetChanged();
                }
                break;

            case R.id.cancel_choose:
                for(int i = 0; i < FilesListAdapter.getIsChecked().size(); i++) {
                    FilesListAdapter.getIsChecked().put(i, false);
                    adapter.notifyDataSetChanged();
                }
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
