package com.my.cyanstone.fileexplore;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by bjshipeiqing on 2016/4/15.
 */
public class FilesListFragment extends Fragment implements View.OnClickListener{
    private final String ROOT_PATH = "/";
    private List<File> files,srcFiles;
    private FilesListAdapter adapter;

    private ListView fileListView;
    private Button allChoose, cancelChoose, copy,delete,paste,mkdir,cancelCopy;
    private TextView currentPahtTv, pathFilesNumTv, checkedFileNumTv,pasteModeTitleTv;
    private LinearLayout buttonsLayout,copyDeleteModeButtons,pastModeButtons;
    private Context context;
    private File currentPath,pasteRecordePath,sdDir,dirFile;
    private HashMap<Integer,Boolean> recordMap;
    private boolean isPasteMode;
    private EditText editFileName;
    private ProgressDialog progressDialog;

    private String TAG = "FilesListFragment";
    private static final int NO_FILE_CHECKED = 0;
    private static final int NO_SD_CARDS = 1;
    private static final int MK_DIR = 2;

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
                pathFilesNumTv.setVisibility(View.GONE);
                checkedFileNumTv.setVisibility(View.VISIBLE);
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
                if(!file.canRead()) {
                    RootCommand("chmod 777 " + file.getAbsolutePath() );
                }
                if (file.isDirectory()) {
                    initData(file);
                } else if (file.isFile()) {
                    openFile(file);
                }
            }
        });

        TextView emptyView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;
        emptyView.setLayoutParams(params);
        emptyView.setTextSize(24);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setText("空文件夹");
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

        paste = (Button) v.findViewById(R.id.paste);
        paste.setOnClickListener(this);

        mkdir = (Button) v.findViewById(R.id.mkdir);
        mkdir.setOnClickListener(this);

        cancelCopy = (Button) v.findViewById(R.id.cancel_copy);
        cancelCopy.setOnClickListener(this);

        currentPahtTv = (TextView) v.findViewById(R.id.current_path);
        pathFilesNumTv = (TextView) v.findViewById(R.id.path_file_nums);
        checkedFileNumTv = (TextView) v.findViewById(R.id.checked_file_nums);
        pasteModeTitleTv = (TextView) v.findViewById(R.id.paste_mode_title_tv);

        buttonsLayout = (LinearLayout) v.findViewById(R.id.buttons_layout);
        copyDeleteModeButtons = (LinearLayout) v.findViewById(R.id.copy_delete_chooseAll_cancel);
        pastModeButtons = (LinearLayout) v.findViewById(R.id.paste_mkdir_cancel);

        files = new ArrayList<File>();
        String pathRoot = "chmod 777 " + ROOT_PATH;
        RootCommand(pathRoot);
        File file = new File(ROOT_PATH);

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("提示信息");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        initData(file);
    }

    private void initData(File file) {
        currentPath = file;
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
        adapter.setOnCheckBoxChangedListener(new CheckBoxChangedListener() {
            @Override
            public void onChanged() {
                checkedFileNumTv.setText("已选择" + adapter.getCheckedNum() + "项");
            }
        });
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
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (adapter.getCheckBoxVisible() == true) {
                        buttonsLayout.setVisibility(View.GONE);
                        checkedFileNumTv.setVisibility(View.GONE);
                        pathFilesNumTv.setVisibility(View.VISIBLE);
                        adapter.setCheckBoxVisible(false);
                        adapter.notifyDataSetChanged();
                        adapter.initData();
                        return true;
                    } else if ( !isPasteMode &&  adapter.getCheckBoxVisible() == false && !currentPath.getPath().equals(ROOT_PATH)) {
                        Log.d("Back", currentPath.getParent());
                        initData(currentPath.getParentFile());
                        return true;
                    } else if(isPasteMode &&  adapter.getCheckBoxVisible() == false) {
                        if(currentPath.getPath().equals(sdDir.getPath())) {
                            isPasteMode = false;
                            pasteModeTitleTv.setVisibility(View.GONE);
                            copyDeleteModeButtons.setVisibility(View.VISIBLE);
                            pastModeButtons.setVisibility(View.GONE);
                            initData(pasteRecordePath);
                            adapter.setCheckBoxVisible(true);
                            checkedFileNumTv.setText("已选择" + adapter.getCheckedNum() + "项");
                            return true;
                        } else {
                            initData(currentPath.getParentFile());
                            return true;
                        }
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
                srcFiles = new ArrayList<File>(files);
                Log.d(TAG,"copy srcFiles:" + srcFiles.toString());
                if(adapter.getCheckedNum() == 0) {
                    createDialog(NO_FILE_CHECKED);
                } else {
                    isPasteMode = true;
                    pasteModeTitleTv.setVisibility(View.VISIBLE);
                    copyDeleteModeButtons.setVisibility(View.GONE);
                    pastModeButtons.setVisibility(View.VISIBLE);
                    boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                    if(sdCardExist) {
                        sdDir = Environment.getExternalStorageDirectory();
                        pasteRecordePath = currentPath;
                        recordMap = FilesListAdapter.getIsChecked();
                        initData(sdDir);
                        adapter.setCheckBoxVisible(false);
                        Log.d("CurrentPath", currentPath.getPath());
                    } else {
                        createDialog(NO_SD_CARDS);
                    }
                }

                break;

            case R.id.delete_files:
                HashMap<Integer,Boolean> map = FilesListAdapter.getIsChecked();
                for(int i = 0; i < files.size(); i++) {
                    if(map.get(i) == true) {
                        if(files.get(i).isDirectory()) {
                            boolean  flag = deleteDirectory(files.get(i).getPath());
                            initData(currentPath);
                            adapter.notifyDataSetChanged();
                            if (!flag) break;
                        } else {
                            boolean flag = deleteFile(files.get(i));
                            if(!flag) break;
                            initData(currentPath);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                break;

            case R.id.paste:
                    dirFile = currentPath;
                    new CopyTask().execute();
                break;

            case R.id.mkdir:
                createDialog(MK_DIR);
                break;

            case R.id.cancel_copy:
                isPasteMode = false;
                pasteModeTitleTv.setVisibility(View.GONE);
                copyDeleteModeButtons.setVisibility(View.VISIBLE);
                pastModeButtons.setVisibility(View.GONE);
                initData(pasteRecordePath);
                adapter.setCheckBoxVisible(true);
                checkedFileNumTv.setText("已选择" + adapter.getCheckedNum() + "项");
                break;
            default:
                break;
        }
    }

    public boolean deleteFile(File file) {
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    public boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i]);
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    private void createDialog(int i) {
        switch (i) {
            case NO_FILE_CHECKED:
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("没有选择任何文件")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                break;
            case NO_SD_CARDS:
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("未检测到sd卡")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
            case MK_DIR:
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_mkdir,null);
                dialog.setView(layout);
                editFileName = (EditText) layout.findViewById(R.id.edit_dialog_mkdir);
                dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = editFileName.getText().toString();
                        File newDir = new File(currentPath.getAbsolutePath() + "//" + fileName);
                        Log.d(TAG, newDir.getPath());
                        if(!newDir.exists()){
                            newDir.mkdir();
                            Log.d(TAG, "新建的目录为 " + newDir.getAbsolutePath());
                            Log.d(TAG, "当前目录为 " + currentPath.getPath());
                            initData(currentPath);
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "当前目录为 " + currentPath.getPath());
                        } else {
                            Toast.makeText(context, "文件夹已存在", Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setTitle("新建文件夹")
                        .setMessage("请输入文件夹名")
                        .show();
                break;
            default:
                break;
        }
    }

    private class CopyTask extends AsyncTask<Void,Void , Void> {

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("正在复制中，请稍后...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG,"doInBackground");
            Log.d(TAG, "task:" + srcFiles.toString());
            if(srcFiles!= null) {
                for(int i = 0; i < srcFiles.size(); i++) {
                    Log.d(TAG,recordMap.get(i) + "");
                    Log.d(TAG,srcFiles.get(i).getAbsolutePath());
                    if(recordMap.get(i)) {
                        if(!srcFiles.get(i).canRead()) {
                            RootCommand("chmod 777 " + srcFiles.get(i).getAbsolutePath());
                        }
                        if(srcFiles.get(i).isFile()) {
                            try {
                                copyFile(srcFiles.get(i),dirFile);
                                Log.d(TAG, "复制文件"  + srcFiles.get(i) + "完毕");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if(srcFiles.get(i).isDirectory()) {
                            try {
                                copyDirectiory(srcFiles.get(i).getAbsolutePath(),dirFile.getAbsolutePath());
                                Log.d(TAG, "复制目录" + srcFiles.get(i) + "完毕");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            initData(dirFile);
            adapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }

    // 复制文件
    public static void copyFile(File sourceFile,File targetFile)
            throws IOException {
        // 新建文件输入流并对它进行缓冲
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff=new BufferedInputStream(input);

        // 新建文件输出流并对它进行缓冲
        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff=new BufferedOutputStream(output);

        // 缓冲数组
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len =inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        // 刷新此缓冲的输出流
        outBuff.flush();

        //关闭流
        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    }

    // 复制文件夹
    public static void copyDirectiory(String sourceDir, String targetDir)
            throws IOException {

        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(sourceDir)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件
                File sourceFile=file[i];
                // 目标文件
                File targetFile=new
                        File(new File(targetDir).getAbsolutePath()
                        +File.separator+file[i].getName());
                copyFile(sourceFile,targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹
                String dir1=sourceDir + "/" + file[i].getName();
                // 准备复制的目标文件夹
                String dir2=targetDir + "/"+ file[i].getName();
                copyDirectiory(dir1, dir2);
            }
        }
    }
}
