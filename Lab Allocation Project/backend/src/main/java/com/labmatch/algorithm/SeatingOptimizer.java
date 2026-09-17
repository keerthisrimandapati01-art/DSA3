package com.labmatch.algorithm;

import com.labmatch.model.Student;

import java.util.*;

/**
 * ============================================================
 * SEATING OPTIMIZER
 * ============================================================
 *
 * Assigns already-allocated students to physical seats.
 *
 * Objectives:
 *
 * 1. Never allocate more students than available seats.
 * 2. Never remove a student from the allocation.
 * 3. Keep physical seat numbering fixed.
 * 4. Spread students across the available physical space.
 * 5. Reduce nearby students from the same branch.
 * 6. Automatically adapt to occupancy.
 *
 * IMPORTANT:
 *
 * Hopcroft-Karp decides:
 *
 *      Student -> Lab
 *
 * This class decides:
 *
 *      Student -> Physical Seat
 *
 * ============================================================
 */
public class SeatingOptimizer {


    // =========================================================
    // PHYSICAL SEAT
    // =========================================================

    public static class PhysicalSeat {

        private final String seatId;

        private final int row;

        private final int column;


        public PhysicalSeat(
                String seatId,
                int row,
                int column
        ) {

            this.seatId = seatId;
            this.row = row;
            this.column = column;
        }


        public String getSeatId() {

            return seatId;
        }


        public int getRow() {

            return row;
        }


        public int getColumn() {

            return column;
        }
    }


    // =========================================================
    // SEAT ASSIGNMENT
    // =========================================================

    public static class SeatAssignment {

        private final Student student;

        private final PhysicalSeat seat;


        public SeatAssignment(
                Student student,
                PhysicalSeat seat
        ) {

            this.student = student;
            this.seat = seat;
        }


        public Student getStudent() {

            return student;
        }


        public PhysicalSeat getSeat() {

            return seat;
        }
    }


    // =========================================================
    // MAIN OPTIMIZATION METHOD
    // =========================================================

    public List<SeatAssignment> optimize(
            List<Student> students,
            int capacity
    ) {

        List<SeatAssignment> result =
                new ArrayList<>();


        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (
                students == null
                ||
                students.isEmpty()
                ||
                capacity <= 0
        ) {

            return result;
        }


        /*
         * Never try to seat more students than the
         * physical capacity.
         *
         * Normally Hopcroft-Karp already guarantees this.
         */
        int studentCount =
                Math.min(
                        students.size(),
                        capacity
                );


        // -----------------------------------------------------
        // COPY STUDENTS
        // -----------------------------------------------------

        List<Student> workingStudents =
                new ArrayList<>(
                        students.subList(
                                0,
                                studentCount
                        )
                );


        // -----------------------------------------------------
        // BUILD FIXED PHYSICAL GRID
        // -----------------------------------------------------

        List<PhysicalSeat> seats =
                buildPhysicalSeats(
                        capacity
                );


        /*
         * Deterministic ordering.
         *
         * Branch is considered only for seating distribution.
         * It is NOT an eligibility restriction.
         */
        workingStudents.sort(
                Comparator
                        .comparing(
                                Student::getBranch,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .thenComparing(
                                Student::getRollNo,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
        );


        // -----------------------------------------------------
        // SELECTED SEATS
        // -----------------------------------------------------

        List<PhysicalSeat> selectedSeats =
                new ArrayList<>();


        List<Student> seatedStudents =
                new ArrayList<>();


        // -----------------------------------------------------
        // ASSIGN EACH STUDENT
        // -----------------------------------------------------

        for (
                Student student :
                workingStudents
        ) {


            PhysicalSeat bestSeat =
                    chooseBestSeat(
                            student,
                            seats,
                            selectedSeats,
                            seatedStudents
                    );


            /*
             * This should only happen when the physical
             * capacity has been exhausted.
             */
            if (
                    bestSeat == null
            ) {

                break;
            }


            selectedSeats.add(
                    bestSeat
            );


            seatedStudents.add(
                    student
            );


            result.add(
                    new SeatAssignment(
                            student,
                            bestSeat
                    )
            );
        }


        return result;
    }


    // =========================================================
    // BUILD PHYSICAL GRID
    // =========================================================

    private List<PhysicalSeat> buildPhysicalSeats(
            int capacity
    ) {

        List<PhysicalSeat> seats =
                new ArrayList<>();


        int columns =
                calculateColumns(
                        capacity
                );


        for (
                int seatNumber = 1;
                seatNumber <= capacity;
                seatNumber++
        ) {


            int row =
                    (
                            (seatNumber - 1)
                                    / columns
                    )
                    + 1;


            int column =
                    (
                            (seatNumber - 1)
                                    % columns
                    )
                    + 1;


            seats.add(
                    new PhysicalSeat(
                            String.valueOf(
                                    seatNumber
                            ),
                            row,
                            column
                    )
            );
        }


        return seats;
    }


    // =========================================================
    // PHYSICAL GRID COLUMNS
    // =========================================================

    private int calculateColumns(
            int capacity
    ) {

        /*
         * These define the actual physical grid.
         *
         * 15 seats:
         *
         *  1  2  3  4  5
         *  6  7  8  9 10
         * 11 12 13 14 15
         */

        if (
                capacity <= 6
        ) {

            return 2;
        }


        if (
                capacity <= 12
        ) {

            return 3;
        }


        /*
         * Labs up to 80 seats use 5 columns.
         */
        if (
                capacity <= 80
        ) {

            return 5;
        }


        /*
         * Larger labs.
         */
        if (
                capacity <= 150
        ) {

            return 10;
        }


        return 12;
    }


    // =========================================================
    // CHOOSE BEST SEAT
    // =========================================================

    private PhysicalSeat chooseBestSeat(
            Student student,
            List<PhysicalSeat> allSeats,
            List<PhysicalSeat> selectedSeats,
            List<Student> seatedStudents
    ) {


        // -----------------------------------------------------
        // FIND AVAILABLE SEATS
        // -----------------------------------------------------

        List<PhysicalSeat> availableSeats =
                new ArrayList<>();


        for (
                PhysicalSeat seat :
                allSeats
        ) {


            if (
                    !selectedSeats.contains(
                            seat
                    )
            ) {

                availableSeats.add(
                        seat
                );
            }
        }


        if (
                availableSeats.isEmpty()
        ) {

            return null;
        }


        // -----------------------------------------------------
        // FIRST STUDENT
        // -----------------------------------------------------

        if (
                selectedSeats.isEmpty()
        ) {

            return chooseInitialSeat(
                    availableSeats
            );
        }


        // -----------------------------------------------------
        // FIND BEST CANDIDATE
        // -----------------------------------------------------

        PhysicalSeat bestSeat =
                null;


        double bestScore =
                Double.NEGATIVE_INFINITY;


        for (
                PhysicalSeat candidate :
                availableSeats
        ) {


            // -------------------------------------------------
            // SPACING
            // -------------------------------------------------

            double spacingScore =
                    calculateSpacingScore(
                            candidate,
                            selectedSeats
                    );


            // -------------------------------------------------
            // BRANCH SEPARATION
            // -------------------------------------------------

            double branchScore =
                    calculateBranchScore(
                            student,
                            candidate,
                            selectedSeats,
                            seatedStudents
                    );


            // -------------------------------------------------
            // EDGE DISTRIBUTION
            // -------------------------------------------------

            double edgeScore =
                    calculateEdgeScore(
                            candidate,
                            allSeats
                    );


            /*
             * Main objective:
             *
             * SPACING > BRANCH SEPARATION > EDGE BALANCE
             *
             * This means seating will primarily spread students
             * across the physical room.
             */
            double finalScore =
                    (spacingScore * 10.0)
                            +
                            (branchScore * 3.0)
                            +
                            edgeScore;


            if (
                    finalScore > bestScore
            ) {

                bestScore =
                        finalScore;

                bestSeat =
                        candidate;
            }
        }


        return bestSeat;
    }


    // =========================================================
    // INITIAL SEAT
    // =========================================================

    private PhysicalSeat chooseInitialSeat(
            List<PhysicalSeat> seats
    ) {


        PhysicalSeat best =
                null;


        double bestScore =
                Double.NEGATIVE_INFINITY;


        // -----------------------------------------------------
        // FIND CENTER OF ROOM
        // -----------------------------------------------------

        double centerRow =
                0;


        double centerColumn =
                0;


        for (
                PhysicalSeat seat :
                seats
        ) {

            centerRow +=
                    seat.getRow();


            centerColumn +=
                    seat.getColumn();
        }


        centerRow /=
                seats.size();


        centerColumn /=
                seats.size();


        // -----------------------------------------------------
        // PICK FAR EDGE / CORNER
        // -----------------------------------------------------

        for (
                PhysicalSeat seat :
                seats
        ) {


            double distanceFromCenter =
                    Math.sqrt(
                            Math.pow(
                                    seat.getRow()
                                            - centerRow,
                                    2
                            )
                                    +
                                    Math.pow(
                                            seat.getColumn()
                                                    - centerColumn,
                                            2
                                    )
                    );


            if (
                    distanceFromCenter > bestScore
            ) {

                bestScore =
                        distanceFromCenter;

                best =
                        seat;
            }
        }


        return best;
    }


    // =========================================================
    // SPACING SCORE
    // =========================================================

    private double calculateSpacingScore(
            PhysicalSeat candidate,
            List<PhysicalSeat> selectedSeats
    ) {


        double minimumDistance =
                Double.MAX_VALUE;


        for (
                PhysicalSeat occupied :
                selectedSeats
        ) {


            double distance =
                    euclideanDistance(
                            candidate,
                            occupied
                    );


            minimumDistance =
                    Math.min(
                            minimumDistance,
                            distance
                    );
        }


        /*
         * Maximize distance from the nearest occupied seat.
         */
        return minimumDistance;
    }


    // =========================================================
    // BRANCH SCORE
    // =========================================================

    private double calculateBranchScore(
            Student student,
            PhysicalSeat candidate,
            List<PhysicalSeat> selectedSeats,
            List<Student> seatedStudents
    ) {


        if (
                selectedSeats.isEmpty()
        ) {

            return 0;
        }


        double score =
                0;


        String currentBranch =
                safe(
                        student.getBranch()
                );


        for (
                int i = 0;
                i < selectedSeats.size();
                i++
        ) {


            PhysicalSeat occupiedSeat =
                    selectedSeats.get(
                            i
                    );


            Student occupiedStudent =
                    seatedStudents.get(
                            i
                    );


            double distance =
                    euclideanDistance(
                            candidate,
                            occupiedSeat
                    );


            String occupiedBranch =
                    safe(
                            occupiedStudent.getBranch()
                    );


            /*
             * Nearby students have greater influence.
             */
            double influence =
                    1.0
                            /
                            (
                                    1.0
                                            + distance
                            );


            // -------------------------------------------------
            // SAME BRANCH
            // -------------------------------------------------

            if (
                    !currentBranch.isEmpty()
                    &&
                    currentBranch.equalsIgnoreCase(
                            occupiedBranch
                    )
            ) {


                /*
                 * Same branch nearby = penalty.
                 */
                score -=
                        5.0
                                * influence;


            }

            // -------------------------------------------------
            // DIFFERENT BRANCH
            // -------------------------------------------------

            else {


                /*
                 * Different branch nearby = small reward.
                 */
                score +=
                        1.5
                                * influence;
            }
        }


        return score;
    }


    // =========================================================
    // EDGE SCORE
    // =========================================================

    private double calculateEdgeScore(
            PhysicalSeat candidate,
            List<PhysicalSeat> allSeats
    ) {


        int maxRow =
                0;


        int maxColumn =
                0;


        for (
                PhysicalSeat seat :
                allSeats
        ) {


            maxRow =
                    Math.max(
                            maxRow,
                            seat.getRow()
                    );


            maxColumn =
                    Math.max(
                            maxColumn,
                            seat.getColumn()
                    );
        }


        double edgeDistance =
                Math.min(
                        Math.min(
                                candidate.getRow() - 1,
                                maxRow
                                        - candidate.getRow()
                        ),
                        Math.min(
                                candidate.getColumn() - 1,
                                maxColumn
                                        - candidate.getColumn()
                        )
                );


        /*
         * Small influence only.
         */
        return -
                edgeDistance
                        * 0.15;
    }


    // =========================================================
    // EUCLIDEAN DISTANCE
    // =========================================================

    private double euclideanDistance(
            PhysicalSeat a,
            PhysicalSeat b
    ) {


        int rowDifference =
                a.getRow()
                        - b.getRow();


        int columnDifference =
                a.getColumn()
                        - b.getColumn();


        return Math.sqrt(
                (
                        rowDifference
                                * rowDifference
                )
                        +
                        (
                                columnDifference
                                        * columnDifference
                        )
        );
    }


    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }
}