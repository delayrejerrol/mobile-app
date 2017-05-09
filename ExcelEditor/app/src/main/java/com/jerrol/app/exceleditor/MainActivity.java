package com.jerrol.app.exceleditor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int READ_REQUEST_CODE = 42;
    private Uri fileUri = null;

    @BindView(R.id.tv_filename) TextView tvFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_read_excel)
    public void readExcel(View view) {
        readExcelFile(this, fileUri);
    }

    @OnClick(R.id.btn_write_excel)
    public void writeExcel(View view) {
        saveExcelFile(this, fileUri);
    }

    @OnClick(R.id.btn_load_template)
    public void loadTemplate(View view) {
        //ACTION_OPEN_DOCUMENT is the intent to choose a file via system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = null;
            if(data != null) {
                uri = data.getData();
                fileUri = uri;
                Log.i(TAG, "Uri: " + uri.toString());
                getFileInformation(uri);
            }
        }
    }

    private void getFileInformation(Uri contentUri) {
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                tvFileName.setText(String.format(getString(R.string.file_name), displayName));
                Log.i(TAG, "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                String size = null;
                if(!cursor.isNull(sizeIndex)) {
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i(TAG, "Size: " + size);
                readExcelFile(this, contentUri);
            }
        } finally {
            cursor.close();
        }
    }

    private static boolean saveExcelFile(Context context, /*String fileName*/ Uri fileUri) {
        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(TAG, "Storage not available or read only");
            return false;
        }

        boolean success = false;

        //Create a path where we will place our List of objects on external storage
        //File file = new File(context.getExternalFilesDir(null), fileName);
        ParcelFileDescriptor parcelFileDescriptor = null;
        FileOutputStream os = null;
        try {
            //New Workbook
            //Workbook wb = new XSSFWorkbook();
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);

            // Create a POIFSFileSystem object
            //POIFSFileSystem myFileSystem = new POIFSFileSystem(inputStream);
            OPCPackage myFileSystem = OPCPackage.open(inputStream);
            XSSFWorkbook wb = new XSSFWorkbook(myFileSystem);

            Cell c = null;

            //Cell style for header row
            CellStyle cs = wb.createCellStyle();
            cs.setFillForegroundColor(HSSFColor.LIME.index);
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND);

            //New sheet
            Sheet sheet1 = null;
            //sheet1 = wb.createSheet("myOrder"); // Create new sheet
            sheet1 = wb.getSheetAt(0); // Get Sheet

            //Generate column headings
            Row row = sheet1.createRow(0);

            c = row.createCell(0);
            c.setCellValue("Item number");
            c.setCellStyle(cs);

            c = row.createCell(1);
            c.setCellValue("Quantity");
            c.setCellStyle(cs);

            c = row.createCell(2);
            c.setCellValue("Price");
            c.setCellStyle(cs);

            c = row.createCell(3);
            c.setCellValue("Total");
            c.setCellStyle(cs);

            sheet1.setColumnWidth(0, (15 * 500));
            sheet1.setColumnWidth(1, (15 * 500));
            sheet1.setColumnWidth(2, (15 * 500));


            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(fileUri, "w");
            os = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
            wb.write(os);
            Log.w("FileUtils", "Writing file" + parcelFileDescriptor.toString());
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + parcelFileDescriptor, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
        return success;
    }

    private static void readExcelFile(Context context, /*String filename*/ Uri fileUri) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.e(TAG, "Storage not available or read only");
            return;
        }

        try{
            // Creating Input Stream
            //File file = new File(context.getExternalFilesDir(null), filename);

            //FileInputStream myInput = new FileInputStream(file);
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);

            // Create a POIFSFileSystem object
            //POIFSFileSystem myFileSystem = new POIFSFileSystem(inputStream);
            OPCPackage myFileSystem = OPCPackage.open(inputStream);

            // Create a workbook using the File System
            XSSFWorkbook myWorkBook = new XSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            /*Iterator rowIter = mySheet.rowIterator();

            while(rowIter.hasNext()){
                XSSFRow myRow = (XSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();
                while(cellIter.hasNext()){
                    XSSFCell myCell = (XSSFCell) cellIter.next();
                    Log.d(TAG, "Cell Value: " +  myCell.toString());
                    Toast.makeText(context, "cell Value: " + myCell.toString(), Toast.LENGTH_SHORT).show();
                }
            }*/

            // Get Name
            Row row = mySheet.getRow(3);
            Cell c = row.getCell(1);
            String name = c.getStringCellValue();


            // Get Project/Purpose
            row = mySheet.getRow(4);
            c = row.getCell(1);
            String purpose = c.getStringCellValue();

            // Get Date range
            row = mySheet.getRow(3);
            c = row.getCell(6);
            String date = c.getStringCellValue();
        }catch (Exception e){e.printStackTrace(); }

        return;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}