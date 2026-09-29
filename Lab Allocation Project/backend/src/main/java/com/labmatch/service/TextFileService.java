package com.labmatch.service;

import com.labmatch.model.Lab;
import com.labmatch.model.Student;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TextFileService {


    // =========================================================
    // STUDENTS
    // =========================================================

    public List<Student> readStudents(
            InputStream inputStream
    ) throws Exception {

        List<Student> students =
                new ArrayList<>();


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        )
                );


        String header =
                reader.readLine();


        if (header == null) {
            return students;
        }


        String[] headers =
                header.split(",");


        int rollColumn =
                findColumn(
                        headers,
                        "roll",
                        "rollno",
                        "rollnumber",
                        "studentid",
                        "registration",
                        "registrationnumber",
                        "regno",
                        "id"
                );


        int nameColumn =
                findColumn(
                        headers,
                        "name",
                        "studentname",
                        "fullname"
                );


        int branchColumn =
                findColumn(
                        headers,
                        "branch",
                        "department",
                        "dept",
                        "stream",
                        "program",
                        "course"
                );


        /*
         * If headers are not useful, try
         * detecting based on the data position.
         */

        if (rollColumn == -1) {
            rollColumn = 0;
        }

        if (nameColumn == -1) {
            nameColumn = 1;
        }

        if (branchColumn == -1) {
            branchColumn = 2;
        }


        String line;


        while (
                (line = reader.readLine()) != null
        ) {

            line = line.trim();


            if (line.isEmpty()) {
                continue;
            }


            String[] parts =
                    line.split(",");


            int required =
                    Math.max(
                            rollColumn,
                            Math.max(
                                    nameColumn,
                                    branchColumn
                            )
                    );


            if (
                    parts.length <= required
            ) {

                continue;
            }


            String rollNo =
                    parts[rollColumn]
                            .trim();


            String name =
                    parts[nameColumn]
                            .trim();


            String branch =
                    parts[branchColumn]
                            .trim();


            if (
                    rollNo.isEmpty() ||
                    name.isEmpty() ||
                    branch.isEmpty()
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


        return students;
    }


    // =========================================================
    // LABS
    // =========================================================

    public List<Lab> readLabs(
            InputStream inputStream
    ) throws Exception {

        List<Lab> labs =
                new ArrayList<>();


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        )
                );


        String header =
                reader.readLine();


        if (header == null) {
            return labs;
        }


        String[] headers =
                header.split(",");


        int labColumn =
                findColumn(
                        headers,
                        "lab",
                        "labname",
                        "laboratory",
                        "laboratoryname",
                        "room",
                        "roomname"
                );


        int capacityColumn =
                findColumn(
                        headers,
                        "capacity",
                        "seats",
                        "seatcapacity",
                        "maxstudents",
                        "maximumstudents",
                        "strength"
                );


        int rowsColumn =
                findColumn(
                        headers,
                        "rows",
                        "row"
                );


        int columnsColumn =
                findColumn(
                        headers,
                        "columns",
                        "column",
                        "cols"
                );


        if (labColumn == -1) {
            labColumn = 0;
        }


        String line;


        while (
                (line = reader.readLine()) != null
        ) {

            line = line.trim();


            if (line.isEmpty()) {
                continue;
            }


            String[] parts =
                    line.split(",");


            if (
                    parts.length <= labColumn
            ) {

                continue;
            }


            String labName =
                    parts[labColumn]
                            .trim();


            if (labName.isEmpty()) {
                continue;
            }


            int capacity = 0;


            if (
                    capacityColumn >= 0
                    &&
                    parts.length > capacityColumn
            ) {

                capacity =
                        parseNumber(
                                parts[capacityColumn]
                        );
            }


            /*
             * If Capacity isn't available,
             * calculate Rows × Columns.
             */

            if (
                    capacity == 0
                    &&
                    rowsColumn >= 0
                    &&
                    columnsColumn >= 0
                    &&
                    parts.length > rowsColumn
                    &&
                    parts.length > columnsColumn
            ) {

                int rows =
                        parseNumber(
                                parts[rowsColumn]
                        );


                int columns =
                        parseNumber(
                                parts[columnsColumn]
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


        return labs;
    }


    // =========================================================
    // FIND COLUMN
    // =========================================================

    private int findColumn(
            String[] headers,
            String... aliases
    ) {

        for (
                int i = 0;
                i < headers.length;
                i++
        ) {

            String header =
                    normalize(
                            headers[i]
                    );


            for (
                    String alias :
                    aliases
            ) {

                String normalizedAlias =
                        normalize(alias);


                if (
                        header.equals(
                                normalizedAlias
                        )
                        ||
                        header.contains(
                                normalizedAlias
                        )
                ) {

                    return i;
                }
            }
        }


        return -1;
    }


    // =========================================================
    // NORMALIZE
    // =========================================================

    private String normalize(
            String value
    ) {

        return value
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9]",
                        ""
                )
                .trim();
    }


    // =========================================================
    // NUMBER
    // =========================================================

    private int parseNumber(
            String value
    ) {

        try {

            String cleaned =
                    value
                            .replaceAll(
                                    "[^0-9.]",
                                    ""
                            );


            if (cleaned.isBlank()) {
                return 0;
            }


            return (int)
                    Double.parseDouble(
                            cleaned
                    );

        } catch (
                Exception e
        ) {

            return 0;
        }
    }
}