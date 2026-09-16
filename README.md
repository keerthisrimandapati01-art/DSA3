# LabMatch — Phase 1

Automated Student–Lab Allocation using Maximum Bipartite Matching.

## Workflow
1. Start the Java backend on port 8080.
2. Open `frontend/index.html` with VS Code Live Server.
3. Import the student Excel/TXT file.
4. Import the lab Excel/TXT file.
5. Click **Allocate Students**.
6. View allocation, utilization, graph and report.

## Student Excel
Columns: `Roll No | Name | Preference 1 | Preference 2`

## Lab Excel
Columns: `Lab Name | Capacity`

## TXT
Students: `23A01,Aarav,Lab A,Lab B`
Labs: `Lab A,25`

## Backend
From `backend`:
`mvn clean compile`
`mvn exec:java`

Server: `http://localhost:8080`

## Algorithms
- Hopcroft–Karp: core maximum bipartite matching.
- KMP: implemented for CO2.
- Bitmask DP: implemented for CO3.

KMP and Bitmask DP are retained for the academic implementation but are not exposed as user actions. The actual allocation is data-driven and uses Hopcroft–Karp.
