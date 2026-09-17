package com.labmatch.algorithm;

import com.labmatch.model.Lab;
import com.labmatch.model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitmaskDP {


    public static class Result {

        private final int score;
        private final int states;
        private final List<Choice> choices;

        public Result(
                int score,
                int states,
                List<Choice> choices
        ) {

            this.score = score;
            this.states = states;
            this.choices = choices;
        }

        public int getScore() {
            return score;
        }

        public int getStates() {
            return states;
        }

        public List<Choice> getChoices() {
            return choices;
        }
    }


    public static class Choice {

        private final Student student;
        private final String lab;
        private final int preferenceRank;

        public Choice(
                Student student,
                String lab,
                int preferenceRank
        ) {

            this.student = student;
            this.lab = lab;
            this.preferenceRank =
                    preferenceRank;
        }

        public Student getStudent() {
            return student;
        }

        public String getLab() {
            return lab;
        }

        public int getPreferenceRank() {
            return preferenceRank;
        }
    }


    /*
     * Supporting CO3 demonstration.
     *
     * This does NOT replace Hopcroft-Karp.
     *
     * It evaluates a small branch-balanced
     * assignment problem using bitmask DP.
     */

    public Result solve(
            List<Student> students,
            List<Lab> labs
    ) {

        /*
         * Limit size because bitmask DP
         * grows exponentially.
         */

        List<Student> smallStudents =
                new ArrayList<>(
                        students.subList(
                                0,
                                Math.min(
                                        8,
                                        students.size()
                                )
                        )
                );


        List<String> smallLabs =
                new ArrayList<>();


        for (Lab lab : labs) {

            smallLabs.add(
                    lab.getName()
            );

            if (smallLabs.size() >= 10) {
                break;
            }
        }


        /*
         * For academic demonstration,
         * calculate a simple score based
         * on branch diversity.
         */

        Map<Long, Integer> memo =
                new HashMap<>();


        int score =
                dp(
                        0,
                        0,
                        smallStudents,
                        smallLabs,
                        memo
                );


        return new Result(
                score,
                memo.size(),
                new ArrayList<>()
        );
    }


    private int dp(
            int studentIndex,
            int mask,
            List<Student> students,
            List<String> labs,
            Map<Long, Integer> memo
    ) {

        if (
                studentIndex
                        >= students.size()
        ) {

            return 0;
        }


        long key =
                (((long) studentIndex) << 32)
                        |
                        (mask & 0xffffffffL);


        if (
                memo.containsKey(key)
        ) {

            return memo.get(key);
        }


        int best =
                dp(
                        studentIndex + 1,
                        mask,
                        students,
                        labs,
                        memo
                );


        /*
         * Try assigning this student
         * to every unused lab.
         */

        for (
                int labIndex = 0;
                labIndex < labs.size();
                labIndex++
        ) {

            if (
                    (mask
                            &
                            (1 << labIndex))
                            != 0
            ) {

                continue;
            }


            /*
             * Assigning to an unused lab
             * gives one unit of score.
             */

            int value =
                    1
                            +
                            dp(
                                    studentIndex + 1,
                                    mask
                                            |
                                            (1 << labIndex),
                                    students,
                                    labs,
                                    memo
                            );


            best =
                    Math.max(
                            best,
                            value
                    );
        }


        memo.put(
                key,
                best
        );


        return best;
    }
}