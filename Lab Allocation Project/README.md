**# LabMatch – Smart Lab Allocation System**

## Overview

**LabMatch** is a smart lab allocation and scheduling system designed to efficiently assign students, faculty, and practical sessions to available computer and specialized laboratories.

The system helps reduce manual lab allocation work by considering laboratory capacity, available labs, student groups, and scheduling requirements.

## Objectives

* Automate the laboratory allocation process.
* Assign students to suitable laboratories based on capacity.
* Avoid laboratory overloading.
* Provide organized lab and student information.
* Reduce manual scheduling effort.
* Improve efficiency and accuracy of laboratory allocation.

## Key Features

* Student and laboratory data management
* Laboratory capacity management
* Automated lab allocation
* Student-to-lab assignment
* Lab availability handling
* Sample dataset support
* Backend API for application logic
* Frontend interface for users
* Structured allocation results

## Technology Stack

### Frontend

* HTML
* CSS
* JavaScript
* React

### Backend

* Java
* Spring Boot

### Database / Data

* Structured sample datasets
* CSV-based input data

### Development Tools

* Visual Studio Code
* Git
* GitHub
* Maven

## Project Structure

```text
project/
│
├── backend/
│   └── Backend source code and APIs
│
├── frontend/
│   └── Frontend application
│
├── sample_data/
│   └── Sample student and laboratory datasets
│
└── README.md
```

## System Workflow

```text
Student Data
     ↓
Lab Data
     ↓
Capacity & Availability Analysis
     ↓
Allocation Algorithm
     ↓
Lab Assignment
     ↓
Allocation Results
```

## Lab Allocation

The system considers laboratory-related constraints such as:

* Number of students
* Laboratory capacity
* Available laboratories
* Laboratory availability
* Lab type and requirements
* Student group/session requirements

The allocation process aims to distribute students appropriately while avoiding capacity violations.

## Sample Laboratories

The project dataset can contain laboratories such as:

* 14A
* 14B
* 15A
* 15B
* 008
* 108
* HC-01
* 301
* 303

The dataset can be extended with additional laboratories when required.

## How to Run

### Backend

Navigate to the backend directory:

```bash
cd backend
```

Build the project using Maven:

```bash
mvn clean install
```

Run the backend application:

```bash
mvn spring-boot:run
```

### Frontend

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies if required:

```bash
npm install
```

Start the frontend:

```bash
npm run dev
```

Open the local URL displayed in the terminal.

## Project Team

**LabMatch – DSA-3 Project**

Developed as part of the academic DSA-3 project.

## Future Enhancements

* Advanced optimization algorithms for allocation
* Faculty scheduling
* Real-time laboratory availability
* Role-based authentication
* Database integration
* Automated timetable generation
* Conflict detection and resolution
* Dashboard with allocation statistics

## Conclusion

LabMatch provides a structured approach to laboratory allocation by combining student information, laboratory capacity, and scheduling requirements. The system is designed to make lab allocation faster, more organized, and less dependent on manual processing.
