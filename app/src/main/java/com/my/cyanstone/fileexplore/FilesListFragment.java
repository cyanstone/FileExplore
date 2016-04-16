package com.my.cyanstone.fileexplore;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private final String PATH = "/";    // "/data/data/";// + getActivity().getPackageName();
    private ListView fileListView;
    private List<File> files;
    private FilesListAdapter adapter;
    List<File> sortFiles;

    private Button allChoose, cancelChoose, copy,delete;
    private TextView currentPahtTv, pathFilesNumTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files_list, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        fileListView = (ListView) v.findViewById(R.id.files_list);
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
        files = new ArrayList<File>();
        String pathRoot = "chmod 777 " + PATH;
        RootCommand(pathRoot);
        File file = new File(PATH);
        sortFiles = new ArrayList<File>();
        initData(file);
    }

    private void initData(File file) {
        currentPahtTv.setText(PATH);
        boolean isRoot = file.getAbsolutePath() == PATH;
        sortFiles = Arrays.asList(file.listFiles());
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
        if (null != sortFiles && sortFiles.size() > 0) {
            for (File c : sortFiles) {
                files.add(c);
            }
        }
        pathFilesNumTv.setText(sortFiles.size() + "项");
        adapter = new FilesListAdapter(getActivity(), (ArrayList<File>) files, isRoot);
        fileListView.setAdapter(adapter);
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
