package com.jungcle.appropin;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;

/** 공유할 이미지(앱 캐시 폴더의 PNG)를 다른 앱이 읽을 수 있게 넘겨 주는 작은 제공자. 읽기 전용이에요. */
public class ShareProvider extends ContentProvider {

    static final String AUTHORITY = "com.jungcle.appropin.share";
    static final String DIR = "share";

    static Uri uriFor(String fileName) {
        return new Uri.Builder().scheme("content").authority(AUTHORITY).appendPath(fileName).build();
    }

    private File fileFor(Uri uri) throws FileNotFoundException {
        String name = uri.getLastPathSegment();
        if (name == null || name.contains("/") || name.contains("..")) throw new FileNotFoundException();
        File f = new File(new File(getContext().getCacheDir(), DIR), name);
        if (!f.isFile()) throw new FileNotFoundException(name);
        return f;
    }

    @Override public boolean onCreate() { return true; }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(fileFor(uri), ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public String getType(Uri uri) { return "image/png"; }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        File f;
        try { f = fileFor(uri); } catch (FileNotFoundException e) { return null; }
        String[] cols = projection != null ? projection : new String[] { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };
        MatrixCursor c = new MatrixCursor(cols, 1);
        Object[] row = new Object[cols.length];
        for (int i = 0; i < cols.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(cols[i])) row[i] = f.getName();
            else if (OpenableColumns.SIZE.equals(cols[i])) row[i] = f.length();
        }
        c.addRow(row);
        return c;
    }

    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String s, String[] a) { return 0; }
    @Override public int update(Uri uri, ContentValues v, String s, String[] a) { return 0; }
}
