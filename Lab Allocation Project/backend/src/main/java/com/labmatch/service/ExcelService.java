package com.labmatch.service;

import com.labmatch.model.Lab;
import com.labmatch.model.Student;
import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelService {

    private final DataFormatter formatter =
            new DataFormatter();


    // =========================================================
    // STUDENT EXCEL
    // =========================================================

    public List<Student> readStudents(
            InputStream inputStream
    ) throws Exception {

        List<Student> students =
                new ArrayList<>();

        try (
                Workbook workbook =
                        WorkbookFactory.create(inputStream)
        ) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            if (sheet == null) {
                return students;
            }

            Row header =
                    findHeaderRow(sheet);

            if (header == null) {
                throw new Exception(
                        "Could not detect student header row."
                );
            }

            int rollColumn =
                    findColumn(
                            header,
                            "roll",
                            "rollno",
                            "rollnumber",
                            "studentid",
                            "studentnumber",
                            "registration",
                            "registrationnumber",
                            "regno",
                            "regnumber",
                            "id"
                    );

            int nameColumn =
                    findColumn(
                            header,
                            "name",
                            "studentname",
                            "fullname",
                            "student"
                    );

            int branchColumn =
                    findColumn(
                            header,
                            "branch",
                            "department",
                            "dept",
                            "stream",
                            "program",
                            "course"
                    );


            if (rollColumn == -1) {
                throw new Exception(
                        "Could not detect Roll Number column."
                );
            }

            if (nameColumn == -1) {
                throw new Exception(
                        "Could not detect Student Name column."
                );
            }

            if (branchColumn == -1) {
                throw new Exception(
                        "Could not detect Branch/Department column."
                );
            }


            int headerRowNumber =
                    header.getRowNum();


            for (
                    int i = headerRowNumber + 1;
                    i <= sheet.getLastRowNum();
                    i++
            ) {

                Row row =
                        sheet.getRow(i);

                if (row == null) {
                    continue;
                }


                String rollNo =
                        getCellValue(
                                row.getCell(
                                        rollColumn
                                )
                        );

                String name =
                        getCellValue(
                                row.getCell(
                                        nameColumn
                                )
                        );

                String branch =
                        getCellValue(
                                row.getCell(
                                        branchColumn
                                )
                        );


                if (
                        rollNo.isBlank() ||
                        name.isBlank() ||
                        branch.isBlank()
                ) {
                    continue;
                }


                students.add(
                        new Student(
                                rollNo,
                                name,
                                branch
                        )
                );
            }
        }


        return students;
    }


    // =========================================================
    // LAB EXCEL
    // =========================================================

    public List<Lab> readLabs(
            InputStream inputStream
    ) throws Exception {

        List<Lab> labs =
                new ArrayList<>();


        try (
                Workbook workbook =
                        WorkbookFactory.create(inputStream)
        ) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            if (sheet == null) {
                return labs;
            }


            Row header =
                    findHeaderRow(sheet);


            if (header == null) {
                throw new Exception(
                        "Could not detect lab header row."
                );
            }


            int labColumn =
                    findColumn(
                            header,
                            "lab",
                            "labname",
                            "laboratory",
                            "laboratoryname",
                            "room",
                            "roomname",
                            "labid"
                    );


            int capacityColumn =
                    findColumn(
                            header,
                            "capacity",
                            "seats",
                            "seatcapacity",
                            "maxstudents",
                            "maximumstudents",
                            "students",
                            "strength"
                    );


            int rowsColumn =
                    findColumn(
                            header,
                            "rows",
                            "row"
                    );


            int columnsColumn =
                    findColumn(
                            header,
                            "columns",
                            "column",
                            "cols"
                    );


            /*
             * If Capacity is missing but Rows and Columns
             * exist, calculate:
             *
             * capacity = rows × columns
             */

            if (
                    capacityColumn == -1
                    &&
                    rowsColumn != -1
                    &&
                    columnsColumn != -1
            ) {

                capacityColumn = -2;
            }


            if (labColumn == -1) {

                throw new Exception(
                        "Could not detect Lab Name column."
                );
            }


            if (
                    capacityColumn == -1
            ) {

                throw new Exception(
                        "Could not detect lab capacity/seats."
                );
            }


            int headerRowNumber =
                    header.getRowNum();


            for (
                    int i = headerRowNumber + 1;
                    i <= sheet.getLastRowNum();
                    i++
            ) {

                Row row =
                        sheet.getRow(i);


                if (row == null) {
                    continue;
                }


                String labName =
                        getCellValue(
                                row.getCell(
                                        labColumn
                                )
                        );


                if (labName.isBlank()) {
                    continue;
                }


                int capacity;


                /*
                 * Direct Capacity column
                 */

                if (capacityColumn >= 0) {

                    String capacityText =
                            getCellValue(
                                    row.getCell(
                                            capacityColumn
                                    )
                            );


                    capacity =
                            parseNumber(
                                    capacityText
                            );

                }

                /*
                 * Capacity calculated from
                 * Rows × Columns.
                 */

                else {

                    int rows =
                            parseNumber(
                                    getCellValue(
                                            row.getCell(
                                                    rowsColumn
                                            )
                                    )
                            );


                    int columns =
                            parseNumber(
                                    getCellValue(
                                            row.getCell(
                                                    columnsColumn
                                            )
                                    )
                            );


                    capacity =
                            rows * columns;
                }


                if (capacity > 0) {

                    labs.add(
                            new Lab(
                                    labName,
                                    capacity
                            )
                    );
                }
            }
        }


        return labs;
    }


    // =========================================================
    // FIND HEADER ROW
    // =========================================================

    private Row findHeaderRow(
            Sheet sheet
    ) {

        /*
         * Search the first 10 rows.
         *
         * This allows files to have a title
         * or blank rows before the actual header.
         */

        int maxRows =
                Math.min(
                        10,
                        sheet.getLastRowNum() + 1
                );


        for (
                int i = 0;
                i < maxRows;
                i++
        ) {

            Row row =
                    sheet.getRow(i);


            if (row == null) {
                continue;
            }


            int nonEmpty = 0;


            for (
                    Cell cell :
                    row
            ) {

                if (
                        !getCellValue(cell)
                                .isBlank()
                ) {

                    nonEmpty++;
                }
            }


            if (nonEmpty >= 2) {
                return row;
            }
        }


        return null;
    }


    // =========================================================
    // FIND COLUMN
    // =========================================================

    private int findColumn(
            Row header,
            String... aliases
    ) {

        for (
                Cell cell :
                header
        ) {

            String headerText =
                    normalize(
                            getCellValue(cell)
                    );


            if (headerText.isBlank()) {
                continue;
            }


            for (
                    String alias :
                    aliases
            ) {

                String normalizedAlias =
                        normalize(alias);


                /*
                 * Exact match
                 */

                if (
                        headerText.equals(
                                normalizedAlias
                        )
                ) {

                    return cell.getColumnIndex();
                }


                /*
                 * Also allow useful variations.
                 *
                 * Example:
                 * "Student Roll Number"
                 * contains "rollnumber"
                 */

                if (
                        headerText.contains(
                                normalizedAlias
                        )
                ) {

                    return cell.getColumnIndex();
                }
            }
        }


        return -1;
    }


    // =========================================================
    // NORMALIZE HEADER
    // =========================================================

    private String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }


        return value
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9]",
                        ""
                )
                .trim();
    }


    // =========================================================
    // CELL VALUE
    // =========================================================

    private String getCellValue(
            Cell cell
    ) {

        if (cell == null) {
            return "";
        }


        return formatter
                .formatCellValue(cell)
                .trim();
    }


    // =========================================================
    // NUMBER PARSER
    // =========================================================

    private int parseNumber(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {

            return 0;
        }


        try {

            /*
             * Handles:
             *
             * 30
             * 30.0
             * "30 seats"
             */

            String cleaned =
                    value
                            .replaceAll(
                                    "[^0-9.]",
                                    ""
                            );


            if (cleaned.isBlank()) {
                return 0;
            }


            double number =
                    Double.parseDouble(
                            cleaned
                    );


            return (int) number;

        } catch (
                NumberFormatException e
        ) {

            return 0;
        }
    }
}