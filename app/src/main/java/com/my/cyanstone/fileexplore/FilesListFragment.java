package com.my.cyanstone.fileexplore;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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
    //private String ROOT_PATH;
    private List<File> files, srcFiles;
    private FilesListAdapter adapter;

    private ListView fileListView;
    private Button allChoose, cancelChoose, copy, delete, paste, mkdir,
            cancelCopy;
    private TextView currentPahtTv, pathFilesNumTv, checkedFileNumTv,
            pasteModeTitleTv;
    private LinearLayout buttonsLayout, copyDeleteModeButtons, pastModeButtons;
    private Context context;
    private File currentPath, pasteRecordePath, sdDir, dirFile;
    private HashMap<Integer, Boolean> recordMap;
    private boolean isPasteMode;
    private EditText editFileName;
    private ProgressDialog progressDialog;

    private String TAG = "FilesListFragment";
    private static final int NO_FILE_CHECKED = 0;
    private static final int NO_SD_CARDS = 1;
    private static final int MK_DIR = 2;
    private static final int DELETE_FILES = 3;
    private static final int UNKNOWN_FILE = 4;

    private static final int UI_STATE_INIT = 5;
    private static final int UI_STATE_BEFORE_COPY_DELETE = 6;
    private static final int UI_STATE_CANCEL_COPY_DELETE = 7;
    private static final int UI_STATE_BEFORE_PASTE = 8;
    private static final int UI_STATE_AFTER_PASTE = 9;
    private static final int UI_STATE_AFTER_DELETE = 10;
    private static final int UI_STATE_PASTE_BACK_COPY = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        setHasOptionsMenu(true);
       // ROOT_PATH = File.separator + "data" + File.separator + "data" + File.separator + context.getPackageName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files_list, container, false);
        initView(v);
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.setOnKeyListener(backListener);
        return v;
    }

    private void initView(View v) {
        fileListView = (ListView) v.findViewById(R.id.files_list);
        fileListView
                .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                   int position, long id) {
                        uiStateConvert(UI_STATE_BEFORE_COPY_DELETE);
                        adapter.notifyDataSetChanged();
                        FilesListAdapter.getIsChecked().put(position, true);
                        return true;
                    }
                });
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                final File file = (File) adapter.getItem(position);
                if (!file.canRead()) {
                    RootCommand("chmod -R 777 " + file.getAbsolutePath());
                }
                if (file.isDirectory()) {
                    initData(file);
                } else if (file.isFile()) {
                    openFile(file);
                }
            }
        });

        TextView emptyView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;
        emptyView.setLayoutParams(params);
        emptyView.setTextSize(24);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setText("空文件夹");
        ((ViewGroup) fileListView.getParent()).addView(emptyView);
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
        copyDeleteModeButtons = (LinearLayout) v
                .findViewById(R.id.copy_delete_chooseAll_cancel);
        pastModeButtons = (LinearLayout) v.findViewById(R.id.paste_mkdir_cancel);

        files = new ArrayList<File>();
        String pathRoot = "chmod -R 777 " + ROOT_PATH;
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
        if (null != sortFiles) {
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
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        try {
            startActivity(intent);
        } catch (Exception e) {
            createDialog(UNKNOWN_FILE);
        }
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
                        uiStateConvert(UI_STATE_CANCEL_COPY_DELETE);
                        adapter.notifyDataSetChanged();
                        adapter.initData();
                        return true;
                    } else if (!isPasteMode && adapter.getCheckBoxVisible() == false
                            && !currentPath.getPath().equals(ROOT_PATH)) {
                        Log.d("Back", currentPath.getParent());
                        initData(currentPath.getParentFile());
                        return true;
                    } else if (isPasteMode && adapter.getCheckBoxVisible() == false) {
                        if (currentPath.getPath().equals(sdDir.getPath())) {
                            isPasteMode = false;
                            uiStateConvert(UI_STATE_PASTE_BACK_COPY);
                            initData(pasteRecordePath);
                            Toast.makeText(context,"返回到应用包路径",Toast.LENGTH_LONG).show();
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
        switch (v.getId()) {
            case R.id.all_choose:
                for (int i = 0; i < FilesListAdapter.getIsChecked().size(); i++) {
                    FilesListAdapter.getIsChecked().put(i, true);
                    adapter.notifyDataSetChanged();
                }
                break;

            case R.id.cancel_choose:
                for (int i = 0; i < FilesListAdapter.getIsChecked().size(); i++) {
                    FilesListAdapter.getIsChecked().put(i, false);
                    adapter.notifyDataSetChanged();
                }
                break;

            case R.id.copy_files:
                srcFiles = new ArrayList<File>(files);
                Log.d(TAG, "copy srcFiles:" + srcFiles.toString());
                if (adapter.getCheckedNum() == 0) {
                    createDialog(NO_FILE_CHECKED);
                } else {
                    isPasteMode = true;
                    uiStateConvert(UI_STATE_BEFORE_PASTE);
                    boolean sdCardExist = Environment.getExternalStorageState().equals(
                            android.os.Environment.MEDIA_MOUNTED);
                    if (sdCardExist) {
                        sdDir = Environment.getExternalStorageDirectory();
                        pasteRecordePath = currentPath;
                        recordMap = new HashMap<Integer, Boolean>(
                                FilesListAdapter.getIsChecked());
                        initData(sdDir);
                        //adapter.setCheckBoxVisible(false);
                        Log.d("CurrentPath", currentPath.getPath());
                    } else {
                        createDialog(NO_SD_CARDS);
                    }
                }

                break;

            case R.id.delete_files:
                if (adapter.getCheckedNum() == 0) {
                    createDialog(NO_FILE_CHECKED);
                } else {
                    recordMap = new HashMap<Integer, Boolean>(
                            FilesListAdapter.getIsChecked());
                    srcFiles = new ArrayList<File>(files);
                    createDialog(DELETE_FILES);
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
                uiStateConvert(UI_STATE_CANCEL_COPY_DELETE);
                initData(pasteRecordePath);
                checkedFileNumTv.setText("已选择" + adapter.getCheckedNum() + "项");
                break;
            default:
                break;
        }
    }

    private void uiStateConvert(int code) {
        switch (code) {
            case UI_STATE_INIT :
                adapter.setCheckBoxVisible(false);
                pasteModeTitleTv.setVisibility(View.GONE);
                pathFilesNumTv.setVisibility(View.VISIBLE);
                checkedFileNumTv.setVisibility(View.GONE);
                copyDeleteModeButtons.setVisibility(View.VISIBLE);
                pastModeButtons.setVisibility(View.GONE);
                buttonsLayout.setVisibility(View.GONE);
                break;
            case UI_STATE_BEFORE_COPY_DELETE :
                adapter.setCheckBoxVisible(true);
                pathFilesNumTv.setVisibility(View.GONE);
                checkedFileNumTv.setVisibility(View.VISIBLE);
                pasteModeTitleTv.setVisibility(View.GONE);
                buttonsLayout.setVisibility(View.VISIBLE);
                copyDeleteModeButtons.setVisibility(View.VISIBLE);
                pastModeButtons.setVisibility(View.GONE);
                // adapter.notifyDataSetChanged();
                break;
            case UI_STATE_CANCEL_COPY_DELETE:
                adapter.setCheckBoxVisible(false);
                pasteModeTitleTv.setVisibility(View.GONE);
                pathFilesNumTv.setVisibility(View.VISIBLE);
                checkedFileNumTv.setVisibility(View.GONE);
                copyDeleteModeButtons.setVisibility(View.VISIBLE);
                pastModeButtons.setVisibility(View.GONE);
                buttonsLayout.setVisibility(View.GONE);
                break;
            case UI_STATE_BEFORE_PASTE:
                pasteModeTitleTv.setVisibility(View.VISIBLE);
                pathFilesNumTv.setVisibility(View.GONE);
                checkedFileNumTv.setVisibility(View.VISIBLE);
                adapter.setCheckBoxVisible(false);
                buttonsLayout.setVisibility(View.VISIBLE);
                copyDeleteModeButtons.setVisibility(View.GONE);
                pastModeButtons.setVisibility(View.VISIBLE);
                break;
            case UI_STATE_AFTER_PASTE :
                adapter.setCheckBoxVisible(false);
                pasteModeTitleTv.setVisibility(View.GONE);
                pathFilesNumTv.setVisibility(View.VISIBLE);
                checkedFileNumTv.setVisibility(View.GONE);
                copyDeleteModeButtons.setVisibility(View.VISIBLE);
                pastModeButtons.setVisibility(View.GONE);
                buttonsLayout.setVisibility(View.GONE);
                break;
            case UI_STATE_AFTER_DELETE:
                adapter.setCheckBoxVisible(false);
                pasteModeTitleTv.setVisibility(View.GONE);
                pathFilesNumTv.setVisibility(View.VISIBLE);
                checkedFileNumTv.setVisibility(View.GONE);
                copyDeleteModeButtons.setVisibility(View.VISIBLE);
                pastModeButtons.setVisibility(View.GONE);
                buttonsLayout.setVisibility(View.GONE);
                break;
            case UI_STATE_PASTE_BACK_COPY:
                adapter.setCheckBoxVisible(false);
                pasteModeTitleTv.setVisibility(View.GONE);
                pathFilesNumTv.setVisibility(View.VISIBLE);
                checkedFileNumTv.setVisibility(View.GONE);
                copyDeleteModeButtons.setVisibility(View.VISIBLE);
                pastModeButtons.setVisibility(View.GONE);
                buttonsLayout.setVisibility(View.GONE);
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
        // 如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        // 遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                // 删除子文件
                flag = deleteFile(files[i]);
                if (!flag)
                    break;
            } else {
                // 删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;
        // 删除当前空目录
        return dirFile.delete();
    }

    private void createDialog(int i) {
        switch (i) {
            case NO_FILE_CHECKED:
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("没有选择任何文件")
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                break;
            case NO_SD_CARDS:
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("未检测到sd卡")
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                break;
            case MK_DIR:
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout layout = (LinearLayout) inflater.inflate(
                        R.layout.dialog_mkdir, null);
                dialog.setView(layout);
                editFileName = (EditText) layout.findViewById(R.id.edit_dialog_mkdir);
                dialog
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String fileName = editFileName.getText().toString();
                                        File newDir = new File(currentPath.getAbsolutePath()
                                                + File.separator + fileName);
                                        Log.d(TAG, newDir.getPath());
                                        if (!newDir.exists()) {
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
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).setTitle("新建文件夹").setMessage("请输入文件夹名").show();
                break;
            case DELETE_FILES:
                new AlertDialog.Builder(context)
                        .setTitle("警告")
                        .setMessage("确定要删除文件？")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteTask().execute();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                break;
            case UNKNOWN_FILE:
                new AlertDialog.Builder(context)
                        .setTitle("提示信息")
                        .setMessage("未知文件类型，无法打开")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            default:
                break;
        }
    }

    private class CopyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("正在复制中，请稍后...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            Log.d(TAG, "task:" + srcFiles.toString());
            Log.d(TAG, "recordMap" + recordMap.toString());
            if (srcFiles != null) {
                for (int i = 0; i < srcFiles.size(); i++) {
                    Log.d(TAG, recordMap.get(i) + "");
                    Log.d(TAG, srcFiles.get(i).getAbsolutePath());
                    if (recordMap.get(i)) {
                        if (!srcFiles.get(i).canRead()) {
                            RootCommand("chmod 777 " + srcFiles.get(i).getAbsolutePath());
                        }
                        if (srcFiles.get(i).isFile()) {
                            try {
                                copyFile(srcFiles.get(i), dirFile);
                                Log.d(TAG, "复制文件" + srcFiles.get(i) + "完毕");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (srcFiles.get(i).isDirectory()) {
                            try {
                                copyDirectiory(srcFiles.get(i), dirFile);
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
            uiStateConvert(UI_STATE_AFTER_PASTE);
            initData(dirFile);
            adapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }

    class DeleteTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("正在删除文件，请稍后...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < files.size(); i++) {
                if (recordMap.get(i) == true) {
                    if (srcFiles.get(i).isDirectory()) {
                        boolean flag = deleteDirectory(srcFiles.get(i).getPath());
                        if (!flag)
                            break;
                    } else {
                        boolean flag = deleteFile(srcFiles.get(i));
                        if (!flag)
                            break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            uiStateConvert(UI_STATE_AFTER_DELETE);
            initData(currentPath);

        }
    }

    // 复制文件
    public static void copyFile(File sourceFile, File destPah) throws IOException {
    /*
     * if(!sourceFile.canRead()) { RootCommand("chmod 777 " +
     * sourceFile.getAbsolutePath()); }
     */
        File destFile = new File(destPah.getPath() + File.separator
                + sourceFile.getName());
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    // 复制文件夹
    public static void copyDirectiory(File sourceDir, File targetDir)
            throws IOException {
        File subTargetDir = new File(targetDir.getAbsolutePath() + File.separator + sourceDir.getName());
        if(!subTargetDir.exists()) {
            subTargetDir.mkdir();
        }
    /*
     * if(!sourceDir.canRead()) { RootCommand("chmod -R 777 " +
     * sourceDir.getAbsolutePath()); }
     */
        File[] file = (sourceDir).listFiles();
        if(file != null) {
            for (int i = 0; i < file.length; i++) {
                if (file[i].isFile()) {
                    File sourceFile = file[i];
                    copyFile(sourceFile, subTargetDir);
                } else {
                    File subSourceDir = new File(sourceDir.getAbsolutePath()
                            + File.separator + file[i].getName());
                    File subTargetPath = new File(subTargetDir.getAbsolutePath()
                            + File.separator + file[i].getName());
                    if (!subTargetPath.exists()) {
                        subTargetDir.mkdir();
                    }
                    copyDirectiory(subSourceDir, subTargetDir);
                }
            }
        }
    }

    public static  String getMIMEType(File file) {
        String type = "*/*";
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return type;
        }
        String end = fileName.substring(dotIndex, fileName.length()).toLowerCase();
        if (end == "") {
            return type;
        }
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0])) {
                type = MIME_MapTable[i][1];
            }
        }
        return type;
    }

    public static  final String[][] MIME_MapTable = {
            // {后缀名， MIME类型}
            { ".3gp", "video/3gpp" },
            { ".apk", "application/vnd.android.package-archive" },
            { ".asf", "video/x-ms-asf" },
            { ".avi", "video/x-msvideo" },
            { ".bin", "application/octet-stream" },
            { ".bmp", "image/bmp" },
            { ".c", "text/plain" },
            { ".class", "application/octet-stream" },
            { ".conf", "text/plain" },
            { ".cpp", "text/plain" },
            { ".doc", "application/msword" },
            { ".docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
            { ".xls", "application/vnd.ms-excel" },
            { ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
            { ".exe", "application/octet-stream" },
            { ".gif", "image/gif" },
            { ".gtar", "application/x-gtar" },
            { ".gz", "application/x-gzip" },
            { ".h", "text/plain" },
            { ".htm", "text/html" },
            { ".html", "text/html" },
            { ".jar", "application/java-archive" },
            { ".java", "text/plain" },
            { ".jpeg", "image/jpeg" },
            { ".jpg", "image/jpeg" },
            { ".js", "application/x-javascript" },
            { ".log", "text/plain" },
            { ".m3u", "audio/x-mpegurl" },
            { ".m4a", "audio/mp4a-latm" },
            { ".m4b", "audio/mp4a-latm" },
            { ".m4p", "audio/mp4a-latm" },
            { ".m4u", "video/vnd.mpegurl" },
            { ".m4v", "video/x-m4v" },
            { ".mov", "video/quicktime" },
            { ".mp2", "audio/x-mpeg" },
            { ".mp3", "audio/x-mpeg" },
            { ".mp4", "video/mp4" },
            { ".mpc", "application/vnd.mpohun.certificate" },
            { ".mpe", "video/mpeg" },
            { ".mpeg", "video/mpeg" },
            { ".mpg", "video/mpeg" },
            { ".mpg4", "video/mp4" },
            { ".mpga", "audio/mpeg" },
            { ".msg", "application/vnd.ms-outlook" },
            { ".ogg", "audio/ogg" },
            { ".pdf", "application/pdf" },
            { ".png", "image/png" },
            { ".pps", "application/vnd.ms-powerpoint" },
            { ".ppt", "application/vnd.ms-powerpoint" },
            { ".pptx",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" },
            { ".prop", "text/plain" }, { ".rc", "text/plain" },
            { ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" },
            { ".sh", "text/plain" }, { ".tar", "application/x-tar" },
            { ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
            { ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
            { ".wmv", "audio/x-ms-wmv" }, { ".wps", "application/vnd.ms-works" },
            { ".xml", "text/plain" }, { ".z", "application/x-compress" },
            { ".zip", "application/x-zip-compressed" }, { "", "*/*" } };
}
