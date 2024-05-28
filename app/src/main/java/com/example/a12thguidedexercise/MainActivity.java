package com.example.a12thguidedexercise;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText studentId, studentName, studentSemGrade;
    Button addRecord, deleteRecord, editRecord, viewRecord, viewAllRecord;

    SQLiteDatabase db;
    Cursor cursor;
    AlertDialog.Builder builder;
    StringBuffer buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        addStudentRecord();
        deleteStudentRecord();
        editStudentRecord();
        viewStudentRecord();
        viewAllStudentRecord();
    }

    public void init() {
        studentId = findViewById(R.id.studentId);
        studentName = findViewById(R.id.studentName);
        studentSemGrade = findViewById(R.id.studentSemGrade);

        addRecord = findViewById(R.id.addRecord);
        deleteRecord = findViewById(R.id.deleteRecord);
        editRecord = findViewById(R.id.editRecord);
        viewRecord = findViewById(R.id.viewRecord);
        viewAllRecord = findViewById(R.id.viewAllRecord);

        builder = new AlertDialog.Builder(this);

        db = openOrCreateDatabase("StudentDB", MODE_PRIVATE, null);
        db.execSQL("DROP TABLE IF EXISTS tbl_student");
        db.execSQL("CREATE TABLE IF NOT EXISTS tbl_student (student_id INTEGER PRIMARY KEY AUTOINCREMENT, student_name TEXT, student_semGrade INTEGER)");

        studentId.setEnabled(false);
        studentName.requestFocus();
    }

    public void displayMessage(String title, String message) {
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void clearEntries() {
        studentId.setText("");
        studentName.setText("");
        studentSemGrade.setText("");
        studentName.requestFocus();
    }

    public void addStudentRecord() {
        addRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (studentName.getText().toString().isEmpty() ||
                        studentSemGrade.getText().toString().isEmpty()) {
                    displayMessage("Error!", "Please enter all the fields");
                    return;
                }

                try {
                    int grade = Integer.parseInt(studentSemGrade.getText().toString());
                    if (grade < 75 || grade > 100) {
                        displayMessage("Error!", "Please enter a valid grade between 75 and 100");
                        return;
                    }
                } catch (NumberFormatException e) {
                    displayMessage("Error!", "Please enter a valid numeric grade between 75 and 100");
                    return;
                }

                String sql = "INSERT INTO tbl_student(student_name, student_semGrade) VALUES(?, ?)";
                try {
                    db.execSQL(sql, new Object[]{studentName.getText().toString(), Integer.parseInt(studentSemGrade.getText().toString())});
                    displayMessage("Information!", "Student record has been successfully added!");
                    clearEntries();
                } catch (SQLException e) {
                    displayMessage("Error!", "Failed to add student record: " + e.getMessage());
                }
            }
        });
    }

    public void deleteStudentRecord() {
        deleteRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (studentId.getText().toString().isEmpty()) {
                    displayMessage("Error!", "Please enter student ID");
                    studentId.requestFocus();
                    return;
                }

                try {
                    cursor = db.rawQuery("SELECT * FROM tbl_student WHERE student_id = ?", new String[]{studentId.getText().toString()});
                    if (cursor.moveToFirst()) {
                        db.execSQL("DELETE FROM tbl_student WHERE student_id = ?", new Object[]{studentId.getText().toString()});
                        displayMessage("Success!", "Student record deleted successfully");
                        clearEntries();
                    } else {
                        displayMessage("Error!", "Student record not found");
                    }

                } catch (SQLException e) {
                    displayMessage("Error!", "Failed to delete student record: " + e.getMessage());
                }
                studentId.setEnabled(false);
            }
        });
    }

    public void viewStudentRecord() {
        viewRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (studentId.getText().toString().isEmpty()) {
                    studentId.setEnabled(true);
                    displayMessage("Error!", "Please enter student ID");
                    studentId.requestFocus();
                    return;
                }

                cursor = db.rawQuery("SELECT * FROM tbl_student WHERE student_id = ?", new String[]{studentId.getText().toString()});
                if (cursor.moveToFirst()) {
                    buffer = new StringBuffer();
                    buffer.append("Student ID: ").append(cursor.getString(0)).append("\n");
                    buffer.append("Student Name: ").append(cursor.getString(1)).append("\n");
                    buffer.append("Student Semester Grade: ").append(cursor.getString(2)).append("\n=================");
                    displayMessage("Student Record", buffer.toString());
                } else {
                    displayMessage("Error!", "Student record not found");
                }
                studentId.setEnabled(false);
                clearEntries();
            }
        });
    }

    public void editStudentRecord() {
        editRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (studentId.getText().toString().isEmpty()) {
                    studentId.setEnabled(true);
                    displayMessage("Error!", "Please enter student ID");
                    editRecord.setText("SAVE");
                    studentId.requestFocus();
                    return;
                }

                cursor = db.rawQuery("SELECT * FROM tbl_student WHERE student_id = ?", new String[]{studentId.getText().toString()});
                if (cursor.moveToFirst()) {
                    db.execSQL("UPDATE tbl_student SET student_name = ?, student_semGrade = ? WHERE student_id = ?",
                            new Object[]{studentName.getText().toString(), Integer.parseInt(studentSemGrade.getText().toString()), studentId.getText().toString()});
                    displayMessage("Success!", "Student record updated successfully");
                } else {
                    displayMessage("Error!", "Student record not found");
                }
                editRecord.setText("EDIT");
                studentId.setEnabled(false);
                clearEntries();
            }
        });
    }

    public void viewAllStudentRecord() {
        viewAllRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor = db.rawQuery("SELECT * FROM tbl_student", null);
                if (cursor.getCount() == 0) {
                    displayMessage("Error!", "No student records found");
                    return;
                }
                buffer = new StringBuffer();
                while (cursor.moveToNext()) {
                    buffer.append("Student ID: ").append(cursor.getString(0)).append("\n");
                    buffer.append("Student Name: ").append(cursor.getString(1)).append("\n");
                    buffer.append("Student Semester Grade: ").append(cursor.getString(2)).append("\n===========\n");
                }
                displayMessage("Student Records", buffer.toString());
            }
        });
    }
}
