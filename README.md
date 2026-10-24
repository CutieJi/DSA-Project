# Queue and System Flow

Here is a step-by-step explanation of how the Registrar Queueing System works from start to finish:

---

### 1. Sign In
The system begins with an administrator login (`admin` / `admin123`). Only authorized registrar staff can access the main menu to manage students and windows.

---

### 2. Adding a Student to the Queue (`Add Queue`)
* **What happens:** When a student arrives at the registrar's office, the admin looks up the student from the pre-loaded records (`students.txt`) and enters their student number.
* **Checks:**
  * The queue can only hold up to **5 students** at a time. If it is already full, the system will not accept more students until a spot opens up.
  * The system checks whether the student is already in the line to prevent duplicate entries.
* **Result:** The student is placed in the queue with the status **`WAITING FOR SERVICE`**, and the action is saved to `queueing_records.txt`.

---

### 3. Assigning a Registrar Service (`Add Service`)
* **What happens:** A student cannot proceed directly to a service window without first specifying what transaction they need. The admin selects a student currently waiting without an assigned service.
* **Service options:** The admin chooses from options such as *Diploma*, *Transcript of Records (TOR)*, *Certificate of Enrollment*, *Good Moral Certificate*, *Grades*, or enters a custom registrar concern.
* **Result:** Their status updates to **`WAITING FOR WINDOW`**. This indicates the student is now fully ready and eligible to be called to a window.

---

### 4. Serving the Student at a Window (`Serve Window 1 / 2`)
* **What happens:** The office operates two counters: **Window 1** and **Window 2**. When staff at either window are ready, the admin selects *Serve for Window 1* or *Serve for Window 2*.
* **Checks:**
  * The window must be currently `AVAILABLE`. If it is already busy serving someone, the system prompts that it is occupied.
  * The system automatically scans the queue in **First-In, First-Out (FIFO)** order to find the first student whose status is `WAITING FOR WINDOW`.
* **Result:**
  * The eligible student is assigned to that window (status becomes **`IN WINDOW 1`** or **`IN WINDOW 2`**).
  * The student is moved out of the 5-person general waiting list, which immediately opens up a slot for the next incoming student.
  * The event is logged in `queueing_records.txt`.

---

### 5. Completing the Service (`Done Service for Window 1 / 2`)
* **What happens:** Once the transaction at the counter is finished, the admin triggers *Done Service* for that window.
* **Result:**
  * The transaction is logged as `SERVICE COMPLETED AT WINDOW <number>`.
  * The window resets back to **`AVAILABLE`**, ready to take the next waiting student.

---

### 6. Checking Line Status and Records Anytime
* **View Line / Waiting List:** Staff can check the live status at any time to see how many people are in the 5-slot queue, what services they requested, and whether Window 1 and Window 2 are `AVAILABLE` or `BUSY`.
* **Record of Queueing:** Every step (arrival, service assignment, window assignment, and completion) is permanently logged in `queueing_records.txt` with student details and timestamps for a complete audit trail.