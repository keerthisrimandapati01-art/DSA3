package com.labmatch;
import com.labmatch.algorithm.SeatingOptimizer;
import com.google.gson.Gson;

import com.labmatch.algorithm.HopcroftKarp;

import com.labmatch.model.Lab;
import com.labmatch.model.Student;

import com.labmatch.service.ExcelService;
import com.labmatch.service.TextFileService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * ============================================================
 * LABMATCH
 * Automated Student-Lab Allocation System
 * ============================================================
 *
 * Core Algorithm:
 *
 *      Hopcroft-Karp Maximum Bipartite Matching
 *
 * Input:
 *      Students
 *      Laboratories
 *
 * Output:
 *      Student -> Lab -> Seat
 *
 * ============================================================
 */
public class Main {


    // =========================================================
    // SERVICES
    // =========================================================

   private static final Gson gson = new Gson();

private static final ExcelService excelService =
        new ExcelService();

private static final TextFileService textFileService =
        new TextFileService();

    // =========================================================
    // APPLICATION DATA
    // =========================================================

    private static List<Student> students =
            new ArrayList<>();


    private static List<Lab> labs =
            new ArrayList<>();


    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args
    ) throws Exception {


        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(
                                8080
                        ),
                        0
                );


        // =====================================================
        // API ENDPOINTS
        // =====================================================

        server.createContext(
                "/api/upload-students",
                Main::uploadStudents
        );


        server.createContext(
                "/api/upload-labs",
                Main::uploadLabs
        );


        server.createContext(
                "/api/allocate",
                Main::allocate
        );


        server.createContext(
                "/api/students",
                Main::getStudents
        );


        server.createContext(
                "/api/labs",
                Main::getLabs
        );


        server.createContext(
                "/api/health",
                Main::health
        );


        server.setExecutor(null);


        server.start();


        // =====================================================
        // STARTUP MESSAGE
        // =====================================================

        System.out.println();

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "          LABMATCH BACKEND"
        );

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Server running at:"
        );

        System.out.println(
                "http://localhost:8080"
        );

        System.out.println();

        System.out.println(
                "Available APIs:"
        );

        System.out.println(
                "GET  /api/health"
        );

        System.out.println(
                "GET  /api/students"
        );

        System.out.println(
                "GET  /api/labs"
        );

        System.out.println(
                "POST /api/upload-students"
        );

        System.out.println(
                "POST /api/upload-labs"
        );

        System.out.println(
                "POST /api/allocate"
        );

        System.out.println();

        System.out.println(
                "Waiting for data..."
        );

        System.out.println();
    }


    // =========================================================
    // UPLOAD STUDENTS
    // =========================================================

    private static void uploadStudents(
            HttpExchange exchange
    ) throws IOException {


        // -----------------------------------------------------
        // CORS PREFLIGHT
        // -----------------------------------------------------

        if (
                exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("OPTIONS")
        ) {

            sendJson(
                    exchange,
                    200,
                    Map.of(
                            "success",
                            true
                    )
            );

            return;
        }


        // -----------------------------------------------------
        // POST ONLY
        // -----------------------------------------------------

        if (
                !exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("POST")
        ) {

            sendJson(
                    exchange,
                    405,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "POST method required."
                    )
            );

            return;
        }


        try {


            // -------------------------------------------------
            // READ FILE
            // -------------------------------------------------

            byte[] fileBytes =
                    exchange
                            .getRequestBody()
                            .readAllBytes();


            if (
                    fileBytes.length == 0
            ) {

                sendJson(
                        exchange,
                        400,
                        Map.of(
                                "success",
                                false,
                                "error",
                                "File is empty."
                        )
                );

                return;
            }


            // -------------------------------------------------
            // FILE NAME
            // -------------------------------------------------

            String fileName =
                    exchange
                            .getRequestHeaders()
                            .getFirst(
                                    "X-File-Name"
                            );


            if (
                    fileName == null
            ) {

                fileName = "";
            }


            String lowerName =
                    fileName.toLowerCase();


            List<Student> imported;


            // -------------------------------------------------
            // TXT / CSV
            // -------------------------------------------------

            if (
                    lowerName.endsWith(".txt")
                    ||
                    lowerName.endsWith(".csv")
            ) {

                imported =
                        textFileService.readStudents(
                                new ByteArrayInputStream(
                                        fileBytes
                                )
                        );
            }


            // -------------------------------------------------
            // XLS / XLSX
            // -------------------------------------------------

            else if (
                    lowerName.endsWith(".xls")
                    ||
                    lowerName.endsWith(".xlsx")
            ) {

                imported =
                        excelService.readStudents(
                                new ByteArrayInputStream(
                                        fileBytes
                                )
                        );
            }


            // -------------------------------------------------
            // UNKNOWN FILE
            // -------------------------------------------------

            else {

                sendJson(
                        exchange,
                        400,
                        Map.of(
                                "success",
                                false,
                                "error",
                                "Unsupported file. Use TXT, CSV, XLS or XLSX."
                        )
                );

                return;
            }


            // -------------------------------------------------
            // VALIDATE
            // -------------------------------------------------

            if (
                    imported == null
                    ||
                    imported.isEmpty()
            ) {

                sendJson(
                        exchange,
                        400,
                        Map.of(
                                "success",
                                false,
                                "error",
                                "No valid student records found."
                        )
                );

                return;
            }


            // -------------------------------------------------
            // STORE
            // -------------------------------------------------

            students =
                    new ArrayList<>(
                            imported
                    );


            // -------------------------------------------------
            // RESPONSE
            // -------------------------------------------------

            Map<String, Object> response =
                    new HashMap<>();


            response.put(
                    "success",
                    true
            );


            response.put(
                    "message",
                    "Students uploaded successfully."
            );


            response.put(
                    "count",
                    students.size()
            );


            response.put(
                    "students",
                    students
            );


            sendJson(
                    exchange,
                    200,
                    response
            );


            System.out.println(
                    "Students loaded: "
                            +
                            students.size()
            );


        } catch (Exception e) {


            e.printStackTrace();


            sendJson(
                    exchange,
                    500,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "Student upload failed: "
                                    +
                                    getErrorMessage(e)
                    )
            );
        }
    }


    // =========================================================
    // UPLOAD LABS
    // =========================================================

    private static void uploadLabs(
            HttpExchange exchange
    ) throws IOException {


        // -----------------------------------------------------
        // CORS PREFLIGHT
        // -----------------------------------------------------

        if (
                exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("OPTIONS")
        ) {

            sendJson(
                    exchange,
                    200,
                    Map.of(
                            "success",
                            true
                    )
            );

            return;
        }


        // -----------------------------------------------------
        // POST ONLY
        // -----------------------------------------------------

        if (
                !exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("POST")
        ) {

            sendJson(
                    exchange,
                    405,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "POST method required."
                    )
            );

            return;
        }


        try {


            // -------------------------------------------------
            // READ FILE
            // -------------------------------------------------

            byte[] fileBytes =
                    exchange
                            .getRequestBody()
                            .readAllBytes();


            if (
                    fileBytes.length == 0
            ) {

                sendJson(
                        exchange,
                        400,
                        Map.of(
                                "success",
                                false,
                                "error",
                                "File is empty."
                        )
                );

                return;
            }


            // -------------------------------------------------
            // FILE NAME
            // -------------------------------------------------

            String fileName =
                    exchange
                            .getRequestHeaders()
                            .getFirst(
                                    "X-File-Name"
                            );


            if (
                    fileName == null
            ) {

                fileName = "";
            }


            String lowerName =
                    fileName.toLowerCase();


            List<Lab> imported;


            // -------------------------------------------------
            // TXT / CSV
            // -------------------------------------------------

            if (
                    lowerName.endsWith(".txt")
                    ||
                    lowerName.endsWith(".csv")
            ) {

                imported =
                        textFileService.readLabs(
                                new ByteArrayInputStream(
                                        fileBytes
                                )
                        );
            }


            // -------------------------------------------------
            // XLS / XLSX
            // -------------------------------------------------

            else if (
                    lowerName.endsWith(".xls")
                    ||
                    lowerName.endsWith(".xlsx")
            ) {

                imported =
                        excelService.readLabs(
                                new ByteArrayInputStream(
                                        fileBytes
                                )
                        );
            }


            // -------------------------------------------------
            // UNKNOWN
            // -------------------------------------------------

            else {

                sendJson(
                        exchange,
                        400,
                        Map.of(
                                "success",
                                false,
                                "error",
                                "Unsupported file. Use TXT, CSV, XLS or XLSX."
                        )
                );

                return;
            }


            // -------------------------------------------------
            // VALIDATE
            // -------------------------------------------------

            if (
                    imported == null
                    ||
                    imported.isEmpty()
            ) {

                sendJson(
                        exchange,
                        400,
                        Map.of(
                                "success",
                                false,
                                "error",
                                "No valid lab records found."
                        )
                );

                return;
            }


            // -------------------------------------------------
            // STORE
            // -------------------------------------------------

            labs =
                    new ArrayList<>(
                            imported
                    );


            // -------------------------------------------------
            // TOTAL CAPACITY
            // -------------------------------------------------

            int totalCapacity =
                    labs.stream()
                            .mapToInt(
                                    Lab::getCapacity
                            )
                            .sum();


            // -------------------------------------------------
            // RESPONSE
            // -------------------------------------------------

            Map<String, Object> response =
                    new HashMap<>();


            response.put(
                    "success",
                    true
            );


            response.put(
                    "message",
                    "Labs uploaded successfully."
            );


            response.put(
                    "count",
                    labs.size()
            );


            response.put(
                    "totalCapacity",
                    totalCapacity
            );


            response.put(
                    "labs",
                    labs
            );


            sendJson(
                    exchange,
                    200,
                    response
            );


            System.out.println(
                    "Labs loaded: "
                            +
                            labs.size()
                            +
                            " | Capacity: "
                            +
                            totalCapacity
            );


        } catch (Exception e) {


            e.printStackTrace();


            sendJson(
                    exchange,
                    500,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "Lab upload failed: "
                                    +
                                    getErrorMessage(e)
                    )
            );
        }
    }


    // =========================================================
    // ALLOCATE
    // =========================================================

    private static void allocate(
            HttpExchange exchange
    ) throws IOException {


        // -----------------------------------------------------
        // CORS PREFLIGHT
        // -----------------------------------------------------

        if (
                exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("OPTIONS")
        ) {

            sendJson(
                    exchange,
                    200,
                    Map.of(
                            "success",
                            true
                    )
            );

            return;
        }


        // -----------------------------------------------------
        // POST ONLY
        // -----------------------------------------------------

        if (
                !exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("POST")
        ) {

            sendJson(
                    exchange,
                    405,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "POST method required."
                    )
            );

            return;
        }


        // -----------------------------------------------------
        // STUDENTS CHECK
        // -----------------------------------------------------

        if (
                students.isEmpty()
        ) {

            sendJson(
                    exchange,
                    400,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "Import student records first."
                    )
            );

            return;
        }


        // -----------------------------------------------------
        // LABS CHECK
        // -----------------------------------------------------

        if (
                labs.isEmpty()
        ) {

            sendJson(
                    exchange,
                    400,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "Import lab data first."
                    )
            );

            return;
        }


        try {


            System.out.println();

            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "       STARTING ALLOCATION"
            );

            System.out.println(
                    "=========================================="
            );


            // =================================================
            // HOPCROFT-KARP
            // =================================================

            System.out.println(
                    "Running Hopcroft-Karp..."
            );


            /*
             * CORRECT FOR YOUR CURRENT CLASS:
             *
             *      HopcroftKarp(students, labs)
             *
             * followed by:
             *
             *      solve()
             */
            List<HopcroftKarp.Match> matches =
                    runHopcroftKarp(students, labs);
System.out.println(
                    "Matching completed."
            );


            System.out.println(
                    "Matched students: "
                            +
                            matches.size()
            );


            // =================================================
            // MATCH LOOKUP
            // =================================================

            Map<String, HopcroftKarp.Match>
                    matchMap =
                    new HashMap<>();


            for (
                    HopcroftKarp.Match match :
                    matches
            ) {


                matchMap.put(
                        match
                                .getStudent()
                                .getRollNo(),
                        match
                );
            }


            // =================================================
            // SEATING OPTIMIZATION
            // =================================================

            /*
             * Hopcroft-Karp decides the LAB.
             * SeatingOptimizer decides the physical SEAT inside
             * that already-selected lab.
             */
            SeatingOptimizer seatingOptimizer =
                    new SeatingOptimizer();

            Map<String, List<Student>> studentsByLab =
                    new HashMap<>();

            for (HopcroftKarp.Match match : matches) {

                studentsByLab
                        .computeIfAbsent(
                                match.getLab(),
                                key -> new ArrayList<>()
                        )
                        .add(match.getStudent());
            }

            Map<String, SeatingOptimizer.SeatAssignment> optimizedSeats =
                    new HashMap<>();

            for (Lab lab : labs) {

                List<Student> labStudents =
                        studentsByLab.get(lab.getName());

                if (labStudents == null || labStudents.isEmpty()) {
                    continue;
                }

                List<SeatingOptimizer.SeatAssignment> assignments =
                        seatingOptimizer.optimize(
                                labStudents,
                                lab.getCapacity()
                        );

                for (SeatingOptimizer.SeatAssignment assignment : assignments) {

                    optimizedSeats.put(
                            assignment.getStudent().getRollNo(),
                            assignment
                    );
                }
            }


            // =================================================
            // FINAL ALLOCATION
            // =================================================

            List<Allocation> result =
                    new ArrayList<>();

            for (Student student : students) {

                HopcroftKarp.Match match =
                        matchMap.get(student.getRollNo());

                if (match == null) {

                    result.add(
                            new Allocation(
                                    student.getRollNo(),
                                    student.getName(),
                                    student.getBranch(),
                                    "Unallocated",
                                    "-",
                                    0,
                                    0
                            )
                    );

                    continue;
                }

                SeatingOptimizer.SeatAssignment optimized =
                        optimizedSeats.get(student.getRollNo());

                if (optimized != null) {

                    SeatingOptimizer.PhysicalSeat physicalSeat =
                            optimized.getSeat();

                    int physicalSeatNumber =
                            Integer.parseInt(
                                    physicalSeat.getSeatId()
                            );

                    String optimizedSeatId =
                            match.getLab()
                                    + "-"
                                    + String.format(
                                            "%02d",
                                            physicalSeatNumber
                                    );

                    result.add(
                            new Allocation(
                                    student.getRollNo(),
                                    student.getName(),
                                    student.getBranch(),
                                    match.getLab(),
                                    optimizedSeatId,
                                    physicalSeat.getRow(),
                                    physicalSeat.getColumn()
                            )
                    );

                } else {

                    /*
                     * Safety fallback.
                     */
                    result.add(
                            new Allocation(
                                    student.getRollNo(),
                                    student.getName(),
                                    student.getBranch(),
                                    match.getLab(),
                                    match.getSeat(),
                                    0,
                                    0
                            )
                    );
                }
            }


            // =================================================
            // STATISTICS
            // =================================================

            int totalStudents =
                    students.size();


            int totalCapacity =
                    labs.stream()
                            .mapToInt(
                                    Lab::getCapacity
                            )
                            .sum();


            int allocated =
                    matches.size();


            int unallocated =
                    Math.max(
                            0,
                            totalStudents
                                    - allocated
                    );


            double percentage =
                    totalStudents == 0
                            ?
                            0
                            :
                            (
                                    allocated
                                            * 100.0
                                            / totalStudents
                            );


            // =================================================
            // RESPONSE
            // =================================================

            Map<String, Object> response =
                    new HashMap<>();


            response.put(
                    "success",
                    true
            );


            response.put(
                    "totalStudents",
                    totalStudents
            );


            response.put(
                    "totalCapacity",
                    totalCapacity
            );


            response.put(
                    "allocated",
                    allocated
            );


            response.put(
                    "unallocated",
                    unallocated
            );


            response.put(
                    "allocationPercentage",
                    Math.round(
                            percentage
                    )
            );


            response.put(
                    "allocations",
                    result
            );


            sendJson(
                    exchange,
                    200,
                    response
            );


            // =================================================
            // CONSOLE
            // =================================================

            System.out.println();

            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "       ALLOCATION COMPLETED"
            );

            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "Total Students : "
                            +
                            totalStudents
            );

            System.out.println(
                    "Total Capacity : "
                            +
                            totalCapacity
            );

            System.out.println(
                    "Allocated      : "
                            +
                            allocated
            );

            System.out.println(
                    "Unallocated    : "
                            +
                            unallocated
            );

            System.out.println(
                    "Success Rate   : "
                            +
                            Math.round(
                                    percentage
                            )
                            +
                            "%"
            );

            System.out.println(
                    "=========================================="
            );

            System.out.println();


        } catch (Exception e) {


            e.printStackTrace();


            sendJson(
                    exchange,
                    500,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "Allocation failed: "
                                    +
                                    getErrorMessage(e)
                    )
            );
        }
    }


    // =========================================================
    // GET STUDENTS
    // =========================================================

    private static void getStudents(
            HttpExchange exchange
    ) throws IOException {


        if (
                exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("OPTIONS")
        ) {

            sendJson(
                    exchange,
                    200,
                    Map.of(
                            "success",
                            true
                    )
            );

            return;
        }


        if (
                !exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("GET")
        ) {

            sendJson(
                    exchange,
                    405,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "GET method required."
                    )
            );

            return;
        }


        sendJson(
                exchange,
                200,
                students
        );
    }


    // =========================================================
    // GET LABS
    // =========================================================

    private static void getLabs(
            HttpExchange exchange
    ) throws IOException {


        if (
                exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("OPTIONS")
        ) {

            sendJson(
                    exchange,
                    200,
                    Map.of(
                            "success",
                            true
                    )
            );

            return;
        }


        if (
                !exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("GET")
        ) {

            sendJson(
                    exchange,
                    405,
                    Map.of(
                            "success",
                            false,
                            "error",
                            "GET method required."
                    )
            );

            return;
        }


        sendJson(
                exchange,
                200,
                labs
        );
    }


    // =========================================================
    // HEALTH
    // =========================================================

    private static void health(
            HttpExchange exchange
    ) throws IOException {


        if (
                exchange
                        .getRequestMethod()
                        .equalsIgnoreCase("OPTIONS")
        ) {

            sendJson(
                    exchange,
                    200,
                    Map.of(
                            "success",
                            true
                    )
            );

            return;
        }


        sendJson(
                exchange,
                200,
                Map.of(
                        "status",
                        "running"
                )
        );
    }


    // =========================================================
    // SEND JSON
    // =========================================================

    private static void sendJson(
            HttpExchange exchange,
            int status,
            Object object
    ) throws IOException {


        String json =
                gson.toJson(
                        object
                );


        byte[] bytes =
                json.getBytes(
                        StandardCharsets.UTF_8
                );


        // -----------------------------------------------------
        // CORS
        // -----------------------------------------------------

        exchange
                .getResponseHeaders()
                .set(
                        "Access-Control-Allow-Origin",
                        "*"
                );


        exchange
                .getResponseHeaders()
                .set(
                        "Access-Control-Allow-Methods",
                        "GET, POST, OPTIONS"
                );


        exchange
                .getResponseHeaders()
                .set(
                        "Access-Control-Allow-Headers",
                        "*"
                );


        exchange
                .getResponseHeaders()
                .set(
                        "Access-Control-Max-Age",
                        "3600"
                );


        // -----------------------------------------------------
        // CONTENT TYPE
        // -----------------------------------------------------

        exchange
                .getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        exchange.sendResponseHeaders(
                status,
                bytes.length
        );


        try (
                OutputStream output =
                        exchange.getResponseBody()
        ) {

            output.write(bytes);
        }
    }


    // =========================================================
    // ERROR MESSAGE
    // =========================================================

    private static String getErrorMessage(
            Exception e
    ) {


        if (
                e == null
        ) {

            return "Unknown error";
        }


        if (
                e.getMessage() == null
                ||
                e.getMessage().isBlank()
        ) {

            return e
                    .getClass()
                    .getSimpleName();
        }


        return e.getMessage();
    }


    // =========================================================
    // ALLOCATION MODEL
    // =========================================================

    private static class Allocation {


        private final String rollNo;

        private final String name;

        private final String branch;

        private final String lab;

        private final String seat;

        private final int row;

        private final int column;


        public Allocation(
                String rollNo,
                String name,
                String branch,
                String lab,
                String seat,
                int row,
                int column
        ) {


            this.rollNo =
                    rollNo;


            this.name =
                    name;


            this.branch =
                    branch;


            this.lab =
                    lab;


            this.seat =
                    seat;

            this.row =
                    row;

            this.column =
                    column;
        }


        public String getRollNo() {

            return rollNo;
        }


        public String getName() {

            return name;
        }


        public String getBranch() {

            return branch;
        }


        public String getLab() {

            return lab;
        }


        public String getSeat() {

            return seat;
        }


        public int getRow() {

            return row;
        }


        public int getColumn() {

            return column;
        }
    }


    // =========================================================
    // HOPCROFT-KARP COMPATIBILITY HELPER
    // =========================================================

    /*
     * Supports either of the two HopcroftKarp APIs that have been
     * used in this project:
     *
     *   A) HopcroftKarp(List<Student>, List<Lab>) + solve()
     *   B) HopcroftKarp() + solve(List<Student>, List<Lab>)
     *
     * This keeps Main.java compatible with the actual compiled class.
     */
    private static List<HopcroftKarp.Match> runHopcroftKarp(
            List<Student> students,
            List<Lab> labs
    ) throws Exception {

        try {

            Constructor<HopcroftKarp> constructor =
                    HopcroftKarp.class.getConstructor(
                            List.class,
                            List.class
                    );

            HopcroftKarp algorithm =
                    constructor.newInstance(
                            students,
                            labs
                    );

            Method solveMethod =
                    HopcroftKarp.class.getMethod(
                            "solve"
                    );

            @SuppressWarnings("unchecked")
            List<HopcroftKarp.Match> matches =
                    (List<HopcroftKarp.Match>)
                            solveMethod.invoke(
                                    algorithm
                            );

            return matches == null
                    ? new ArrayList<>()
                    : matches;

        } catch (NoSuchMethodException firstApiNotFound) {

            Constructor<HopcroftKarp> constructor =
                    HopcroftKarp.class.getConstructor();

            HopcroftKarp algorithm =
                    constructor.newInstance();

            Method solveMethod =
                    HopcroftKarp.class.getMethod(
                            "solve",
                            List.class,
                            List.class
                    );

            @SuppressWarnings("unchecked")
            List<HopcroftKarp.Match> matches =
                    (List<HopcroftKarp.Match>)
                            solveMethod.invoke(
                                    algorithm,
                                    students,
                                    labs
                            );

            return matches == null
                    ? new ArrayList<>()
                    : matches;
        }
    }
}