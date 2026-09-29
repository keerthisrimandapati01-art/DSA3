package com.labmatch.algorithm;

import com.labmatch.model.Lab;
import com.labmatch.model.Student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class HopcroftKarp {

    /*
     * =========================================================
     * MATCH RESULT
     * =========================================================
     */

    public static class Match {

        private final Student student;
        private final String lab;
        private String seat;
private int row;
private int column;
public void setSeat(String seat) {
    this.seat = seat;
}

public void setRow(int row) {
    this.row = row;
}

public void setColumn(int column) {
    this.column = column;
}


        public Match(
                Student student,
                String lab,
                String seat,
                int row,
                int column
        ) {

            this.student = student;
            this.lab = lab;
            this.seat = seat;
            this.row = row;
            this.column = column;
        }


        public Student getStudent() {
            return student;
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


    /*
     * =========================================================
     * SEAT
     * =========================================================
     */

    private static class Seat {

        private final String lab;
        private final String seatId;

        private final int row;
        private final int column;


        public Seat(
                String lab,
                String seatId,
                int row,
                int column
        ) {

            this.lab = lab;
            this.seatId = seatId;
            this.row = row;
            this.column = column;
        }
    }


    /*
     * =========================================================
     * DATA
     * =========================================================
     */

    private final List<Student> students;
    private final List<Lab> labs;


    public HopcroftKarp(
            List<Student> students,
            List<Lab> labs
    ) {

        this.students = students;
        this.labs = labs;
    }


    /*
     * =========================================================
     * MAIN SOLVER
     * =========================================================
     *
     * Maximum Bipartite Matching using Hopcroft-Karp.
     *
     * LEFT SIDE:
     *      Students
     *
     * RIGHT SIDE:
     *      Individual lab seats
     *
     * EDGE:
     *      Student can be assigned to that seat.
     *
     * IMPORTANT:
     *      Every student can use every available seat.
     *
     * Therefore this is a complete bipartite graph.
     *
     * We control the ORDER of seats so that labs are
     * utilized in a balanced manner.
     */

    public List<Match> solve() {

        if (
                students == null
                ||
                labs == null
                ||
                students.isEmpty()
                ||
                labs.isEmpty()
        ) {

            return new ArrayList<>();
        }


        /*
         * -----------------------------------------------------
         * STEP 1
         * -----------------------------------------------------
         *
         * Arrange students branch-wise.
         *
         * Example:
         *
         * CSE
         * ECE
         * EEE
         * IT
         * CSE
         * ECE
         * EEE
         * IT
         */

        List<Student> orderedStudents =
                arrangeStudentsByBranch();


        /*
         * -----------------------------------------------------
         * STEP 2
         * -----------------------------------------------------
         *
         * Create seats using BALANCED LAB ordering.
         *
         * Example:
         *
         * 14A-R1C1
         * 14B-R1C1
         * 15A-R1C1
         *
         * 14A-R1C2
         * 14B-R1C2
         * 15A-R1C2
         *
         * ...
         *
         * This is the important improvement.
         */

        List<Seat> seats =
                buildBalancedSeats();


        if (
                orderedStudents.isEmpty()
                ||
                seats.isEmpty()
        ) {

            return new ArrayList<>();
        }


        /*
         * -----------------------------------------------------
         * STEP 3
         * -----------------------------------------------------
         *
         * Create the bipartite graph.
         *
         * Every student can use every seat.
         */

        int studentCount =
                orderedStudents.size();

        int seatCount =
                seats.size();


        List<List<Integer>> graph =
                new ArrayList<>();


        for (
                int student = 0;
                student < studentCount;
                student++
        ) {

            List<Integer> edges =
                    new ArrayList<>();


            for (
                    int seat = 0;
                    seat < seatCount;
                    seat++
            ) {

                edges.add(seat);
            }


            graph.add(edges);
        }


        /*
         * -----------------------------------------------------
         * STEP 4
         * -----------------------------------------------------
         *
         * Matching arrays.
         *
         * pairStudent[s] = seat assigned to student s
         *
         * pairSeat[seat] = student assigned to seat
         *
         * -1 = unmatched
         */

        int[] pairStudent =
                new int[studentCount];

        int[] pairSeat =
                new int[seatCount];


        Arrays.fill(
                pairStudent,
                -1
        );

        Arrays.fill(
                pairSeat,
                -1
        );


        int[] distance =
                new int[studentCount];


        int matchingSize = 0;


        /*
         * -----------------------------------------------------
         * STEP 5
         * -----------------------------------------------------
         *
         * HOPCROFT-KARP
         *
         * Repeatedly:
         *
         *      BFS → create layers
         *      DFS → find augmenting paths
         */

        while (
                bfs(
                        graph,
                        pairStudent,
                        pairSeat,
                        distance
                )
        ) {

            for (
                    int student = 0;
                    student < studentCount;
                    student++
            ) {

                if (
                        pairStudent[student] == -1
                        &&
                        dfs(
                                student,
                                graph,
                                pairStudent,
                                pairSeat,
                                distance
                        )
                ) {

                    matchingSize++;
                }
            }
        }


        /*
         * -----------------------------------------------------
         * STEP 6
         * -----------------------------------------------------
         *
         * Convert matching into our final result.
         */

        List<Match> result =
                new ArrayList<>();


        for (
                int studentIndex = 0;
                studentIndex < studentCount;
                studentIndex++
        ) {

            int seatIndex =
                    pairStudent[studentIndex];


            if (seatIndex == -1) {
                continue;
            }


            Student student =
                    orderedStudents.get(
                            studentIndex
                    );


            Seat seat =
                    seats.get(
                            seatIndex
                    );


            result.add(
                    new Match(
                            student,
                            seat.lab,
                            seat.seatId,
                            seat.row,
                            seat.column
                    )
            );
        }


        /*
         * Console information.
         */

        System.out.println(
                "Hopcroft-Karp matching size: "
                        + matchingSize
        );


        return result;
    }


    /*
     * =========================================================
     * BFS
     * =========================================================
     */

    private boolean bfs(
            List<List<Integer>> graph,
            int[] pairStudent,
            int[] pairSeat,
            int[] distance
    ) {

        Queue<Integer> queue =
                new LinkedList<>();


        /*
         * Start BFS from every unmatched student.
         */

        for (
                int student = 0;
                student < pairStudent.length;
                student++
        ) {

            if (
                    pairStudent[student] == -1
            ) {

                distance[student] = 0;

                queue.add(student);

            } else {

                distance[student] = -1;
            }
        }


        boolean foundFreeSeat =
                false;


        /*
         * Explore alternating paths.
         */

        while (!queue.isEmpty()) {

            int student =
                    queue.poll();


            for (
                    int seat :
                    graph.get(student)
            ) {

                int matchedStudent =
                        pairSeat[seat];


                /*
                 * We found an unmatched seat.
                 */

                if (
                        matchedStudent == -1
                ) {

                    foundFreeSeat = true;

                }


                /*
                 * Follow the matching edge.
                 */

                else if (
                        distance[matchedStudent] == -1
                ) {

                    distance[matchedStudent] =
                            distance[student] + 1;

                    queue.add(
                            matchedStudent
                    );
                }
            }
        }


        return foundFreeSeat;
    }


    /*
     * =========================================================
     * DFS
     * =========================================================
     */

    private boolean dfs(
            int student,
            List<List<Integer>> graph,
            int[] pairStudent,
            int[] pairSeat,
            int[] distance
    ) {

        for (
                int seat :
                graph.get(student)
        ) {

            int matchedStudent =
                    pairSeat[seat];


            /*
             * Case 1:
             * Seat is free.
             */

            if (
                    matchedStudent == -1
            ) {

                pairStudent[student] =
                        seat;

                pairSeat[seat] =
                        student;

                return true;
            }


            /*
             * Case 2:
             * Follow the next BFS layer.
             */

            if (
                    distance[matchedStudent]
                    ==
                    distance[student] + 1
            ) {

                if (
                        dfs(
                                matchedStudent,
                                graph,
                                pairStudent,
                                pairSeat,
                                distance
                        )
                ) {

                    pairStudent[student] =
                            seat;

                    pairSeat[seat] =
                            student;

                    return true;
                }
            }
        }


        /*
         * No augmenting path from this student.
         */

        distance[student] = -1;

        return false;
    }


    /*
     * =========================================================
     * BALANCED SEAT GENERATION
     * =========================================================
     *
     * THIS IS THE IMPORTANT PART.
     *
     * Instead of:
     *
     *      14A seats
     *      14B seats
     *      15A seats
     *
     * we interleave the labs:
     *
     *      14A seat 1
     *      14B seat 1
     *      15A seat 1
     *
     *      14A seat 2
     *      14B seat 2
     *      15A seat 2
     *
     * This encourages the maximum matching to use all labs
     * before filling one lab excessively.
     */

    private List<Seat> buildBalancedSeats() {

        List<Seat> result =
                new ArrayList<>();


        /*
         * Number of seats required for each lab.
         */

        Map<String, Integer> remaining =
                new LinkedHashMap<>();


        /*
         * Store current row/column for every lab.
         */

        Map<String, Integer> currentSeatNumber =
                new LinkedHashMap<>();


        for (Lab lab : labs) {

            int capacity =
                    Math.max(
                            0,
                            lab.getCapacity()
                    );


            remaining.put(
                    lab.getName(),
                    capacity
            );


            currentSeatNumber.put(
                    lab.getName(),
                    1
            );
        }


        /*
         * Keep cycling through labs.
         *
         * Example:
         *
         * Round 1:
         *      14A
         *      14B
         *      15A
         *
         * Round 2:
         *      14A
         *      14B
         *      15A
         *
         * Continue until all seats are generated.
         */

        boolean seatsRemaining =
                true;


        while (seatsRemaining) {

            seatsRemaining = false;


            for (Lab lab : labs) {

                String labName =
                        lab.getName();


                int left =
                        remaining.get(
                                labName
                        );


                if (left <= 0) {
                    continue;
                }


                seatsRemaining = true;


                int seatNumber =
                        currentSeatNumber.get(
                                labName
                        );


                /*
                 * Calculate seating position.
                 *
                 * Four actual student columns are used.
                 *
                 * The frontend can visually place gaps
                 * between these columns.
                 */

                int columns =
                        calculateColumns(
                                lab.getCapacity()
                        );


                int row =
                        ((seatNumber - 1)
                                / columns)
                                + 1;


                int column =
                        ((seatNumber - 1)
                                % columns)
                                + 1;


                String seatId =
                        labName
                                + "-R"
                                + row
                                + "C"
                                + column;


                result.add(
                        new Seat(
                                labName,
                                seatId,
                                row,
                                column
                        )
                );


                remaining.put(
                        labName,
                        left - 1
                );


                currentSeatNumber.put(
                        labName,
                        seatNumber + 1
                );
            }
        }


        return result;
    }


    /*
     * =========================================================
     * SEATING COLUMNS
     * =========================================================
     */

    private int calculateColumns(
            int capacity
    ) {

        /*
         * Similar to the exam seating layout.
         */

        if (capacity <= 6) {
            return 2;
        }


        if (capacity <= 12) {
            return 3;
        }


        if (capacity <= 80) {
            return 4;
        }


        if (capacity <= 150) {
            return 5;
        }


        return 6;
    }


    /*
     * =========================================================
     * BRANCH-AWARE STUDENT ORDERING
     * =========================================================
     *
     * Branches are NOT eligibility restrictions.
     *
     * They are only used to spread students.
     *
     * Example:
     *
     * CSE → ECE → EEE → IT
     * CSE → ECE → EEE → IT
     *
     * This helps prevent one lab from containing one branch
     * exclusively.
     */

    private List<Student> arrangeStudentsByBranch() {

        Map<String, Queue<Student>>
                branchQueues =
                new LinkedHashMap<>();


        /*
         * Put students into branch queues.
         */

        for (Student student : students) {

            String branch =
                    student.getBranch();


            if (
                    branch == null
                    ||
                    branch.trim().isEmpty()
            ) {

                branch = "UNKNOWN";
            }


            branchQueues
                    .computeIfAbsent(
                            branch,
                            key -> new LinkedList<>()
                    )
                    .add(student);
        }


        /*
         * Round-robin extraction.
         */

        List<Student> result =
                new ArrayList<>();


        boolean studentsRemaining =
                true;


        while (studentsRemaining) {

            studentsRemaining = false;


            for (
                    Queue<Student> queue :
                    branchQueues.values()
            ) {

                if (!queue.isEmpty()) {

                    result.add(
                            queue.poll()
                    );

                    studentsRemaining = true;
                }
            }
        }


        return result;
    }
}