# DSA-3 — Data Structures & Algorithms

This repository contains the work developed as part of the **Data Structures & Algorithms (DSA-3)** course.

The repository includes the **Lab Allocation Project**, supporting documentation, sample datasets, and implementation of different algorithms used for student-lab allocation and optimization.

---

## 📌 Repository Contents

```text
DSA3/
│
├── Lab Allocation Project/
│   ├── backend/
│   ├── frontend/
│   ├── sample_data/
│   └── README.md
│
├── DSA3 Abstract.docx
├── DSA3 Review1 PPT.pdf
└── README.md
```

---

## 💻 Lab Allocation Project

The **Lab Allocation Project** is designed to automate and optimize the allocation of students to laboratory sessions based on available labs, student requirements, and allocation constraints.

### Key Features

* Student and laboratory data management
* Automated lab allocation
* Student-to-lab matching
* Seat allocation and optimization
* Constraint-based allocation
* Sample student and laboratory datasets
* File-based data processing
* Algorithm-based optimization

---

## 🧠 Algorithms Implemented

The project includes implementations of multiple Data Structures and Algorithms concepts:

### 1. Bitmask Dynamic Programming

Used for solving allocation and optimization problems by representing possible states using bitmasks.

### 2. Hopcroft–Karp Algorithm

Used for efficient **maximum bipartite matching**, helping match students with available laboratory seats/resources.

### 3. KMP Algorithm

The **Knuth–Morris–Pratt (KMP)** string matching algorithm is implemented for efficient pattern searching.

### 4. Seating Optimization

Used to optimize physical seat assignments based on laboratory capacity and allocation requirements.

---

## 🛠️ Technologies Used

* **Java**
* **Maven**
* **HTML / CSS / JavaScript**
* **Excel / TXT datasets**
* **Git & GitHub**

---

## 📂 Project Components

### Backend

The backend contains the Java implementation of the core algorithms, models, services, and allocation logic.

```text
backend/
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── labmatch/
└── target/
```

### Frontend

The frontend provides the user interface for interacting with the lab allocation system.

### Sample Data

The `sample_data` folder contains sample laboratory and student datasets used for testing the allocation system.

---

## 🧪 Sample Laboratory Setup

The project supports laboratory allocation using labs such as:

* 14A
* 14B
* 15A
* 15B
* 008
* 108
* HC-01
* 301
* 303

The sample datasets can be modified according to the required laboratory capacity and student information.

---

## 📄 Documentation

This repository also contains the project documentation:

* **DSA3 Abstract** — Project abstract and overview
* **DSA3 Review 1 PPT** — Initial project presentation
* **Lab Allocation Project README** — Detailed project information and setup

---

## ▶️ Running the Project

### Backend

Navigate to the backend directory:

```bash
cd "Lab Allocation Project/backend"
```

Build the project using Maven:

```bash
mvn clean package
```

Run the generated application according to the project's backend configuration.

### Frontend

Navigate to:

```bash
cd "Lab Allocation Project/frontend"
```

Open the frontend files in a browser or run them using the appropriate local development environment.

---

## 🎯 Project Objective

The main objective of this project is to apply **Data Structures and Algorithms concepts to a practical laboratory allocation problem** and develop an efficient system for handling student-lab matching and resource allocation.

---

## 👥 Team

**DSA-3 LabMatch / Lab Allocation Project**

This project was developed as part of the academic coursework for the DSA-3 course.

---

## 📌 Academic Project

This repository is maintained for **academic, learning, implementation, and project evaluation purposes**.

---

### ⭐ Topics Covered

`Data Structures` · `Algorithms` · `Dynamic Programming` · `Graph Algorithms` · `Bipartite Matching` · `String Matching` · `Optimization` · `Java` · `Maven` · `GitHub`
