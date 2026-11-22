# GPU Utilization Management System

**COSC230 – Object-Oriented Programming**  
**Khalifa University | Fall 2026**

## Project Overview

The GPU Utilization Management System is a Java Swing desktop application that simulates the management of four NVIDIA H100 GPUs.

The system allows students to register, submit computational jobs, and monitor their GPU time usage. Administrators can manage student accounts, allocate GPU minutes, control the simulation clock, and monitor GPU utilization.

All GPU operations are simulated without requiring physical GPU hardware.

## Team Members

| Member | Name | Responsibilities |
|---|---|---|
| A | Mohamed Alabdouli | Account management, registration, login, GPU allowances, and AppState |
| B | Mohamed Alwahedi | Job management, GPU scheduling, cancellations, and predictions |
| C | Zayed Almaghrebi | Application integration, dashboards, simulation clock, and data persistence |

## Main Features

- Student registration and administrator approval.
- Student and administrator login.
- GPU time allocation and balance tracking.
- Job submission with four task sizes (5, 15, 30, and 60 minutes).
- First Come First Served (FCFS) scheduling across four GPUs.
- Job cancellation and waiting-time predictions.
- Administrator dashboard and GPU utilization monitoring.
- Manual simulation clock advancement.
- Saving and restoring application data.

## Technologies

- **Language:** Java
- **GUI:** Java Swing
- **IDE:** Eclipse
- **Version Control:** Git and GitHub
- **Storage:** Local file-based persistence

## Project Structure

The application is organized into four packages:

- `model` – Data classes and application state.
- `logic` – Account management and GPU scheduling.
- `ui` – Java Swing interfaces.
- `storage` – Saving and loading application data.

## How to Run

1. Install Java JDK 17 or later and Eclipse IDE.
2. Clone the repository:

   `git clone https://github.com/iiim7md/GPU-Utilization-Management.git`

3. Open Eclipse and select **File → Import → General → Existing Projects into Workspace**.
4. Select the cloned project folder.
5. Run `ui.Main` as a Java Application.

## Administrator Login

**Username:** `admin`  
**Password:** `gpu2026`

Student accounts require administrator approval before login.

## GitHub Repository

https://github.com/iiim7md/GPU-Utilization-Management

---

**COSC230 – Object-Oriented Programming | Fall 2026**  
**Khalifa University**
