# 🚌 MengTracking – UiTM Shuttle Bus Tracker

A mobile Android application built for **Universiti Teknologi MARA (UiTM)** that allows **students** to track shuttle buses in real-time and enables **drivers** to share their live location through QR-triggered tracking.

This project integrates **Firebase**, **Google Maps**, **QR Scanning**, and **Background Services**, and includes additional features outside of ICT602 lab learning activities.

---

## 👥 Project Contribution & Acknowledgement

This mobile application was developed by the **MengTracking Group** for the **ICT602 Mobile Technology** course at UiTM.

**Project Supervisor:** Dr. Norkhushaini Bt Awang

| Name              | Role / Contribution                                 |
|-------------------|-----------------------------------------------------|
| Izat Aiman        | Project Leader, Driver-side functionality, Backend  |
| Ahmad Faqih       | UI/UX Designing , About Us Page                                    |
| Muhammad Hadif    | UI/UX Designing                                     |
| Mohamad Firdaus   | Student-side functionality                          |
| Aina Syahirah   |                          |

---

## 🛠️ Project Setup Instructions 

To run this project successfully on your local machine, follow these steps:

### 1. 🔐 Add Google Maps API Key

Find a file named `local.properties` in the **root directory** of the project (same level as your `build.gradle`) and add:

MAPS_API_KEY= [Your Google Map API KEY]
<img width="1412" height="293" alt="image" src="https://github.com/user-attachments/assets/5395bb5c-3dba-40d5-9e9c-e6487ebf5770" />

## 📱 Features

### 🔐 Authentication
- Firebase Authentication (email & password)
- Role-based access: Student / Driver
- Auto-login & session persistence

### 🗺️ Tracking
- **Students** can view bus locations live on Google Maps
- **Drivers** can start tracking via QR code
- Foreground service with non-dismissible notification for safety

### 🧭 Navigation
- Fragment-based UI with BottomNavigationView
- Separate dashboards for students and drivers

### 👤 Profile Management
- Registration includes profile image upload
- Profile data and role stored in Firebase Realtime Database
- Change password and logout functionality

### ☁️ Cloud Integration
- Firebase Authentication
- Firebase Realtime Database (locations, user info)
- Firebase Storage (profile pictures)

---

## 🧪 Labs Referenced (ICT602)

| Feature                    | Lab | Notes                            |
|----------------------------|-----|----------------------------------|
| Layouts & Inputs           | 1, 2| Basic form design and layout     |
| Firebase Realtime Database | 4   | For location and user data       |
| Authentication             | 5   | Login and registration           |
| Google Maps API            | 7   | Student-side tracking            |
| QR Code                    | 8   | Driver-side scan to start        |
| Splash Screen              | 8   | Auto-login loader on startup     |

---

## 🚀 Additional Features Included

- ✅ Role-based login 
- ✅ Persistent authentication (auto-login)
- ✅ Firebase Storage image upload
- ✅ QR scan to trigger location tracking
- ✅ Foreground service with **non-dismissible notification**
- ✅ Background tracking continuity
- ✅ Real-time multi-bus tracking with marker updates
- ✅ Google Map Marker Timestamp 

---

## 🔧 Tech Stack

- **Android (Java)**
- **Firebase** (Auth, Realtime DB, Storage)
- **Google Maps API**
- **ZXing QR Scanner**
- **Glide (Image loading)**

---
## 📚 References

This project was inspired by real-world transit and mobility apps:

- **Moovit** – https://moovitapp.com  
  A global public transport app offering real-time bus tracking, route planning, and location-aware features.

- **MyRapid Pulse** – https://www.myrapid.com.my/traveling-with-us/myrapid-pulse-app  
  Malaysia’s official app for RapidKL, enabling real-time bus locations and service updates.
---

This project is developed for educational purposes as part of the ICT602 Mobile Technology course at UiTM.



