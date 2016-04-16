package com.my.cyanstone.fileexplore;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bjshipeiqing on 2016/4/15.
 */
public class FilesListFragment extends Fragment {
    private ListView fileListView;
    private List<File>  files;
    private FilesListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files_list,container,false);
        fileListView = (ListView) v.findViewById(R.id.files_list);
        files = new ArrayList<File>();
        String rootPath = "/"; //"/data/data/";// + getActivity().getPackageName();
        File file = new File(rootPath);
        File [] filesArray = file.listFiles();
        for(File c : filesArray) {
            files.add(c);
        }
        adapter = new FilesListAdapter(getActivity(), (ArrayList<File>) files,false);
        fileListView.setAdapter(adapter);
        return v;
    }
}
