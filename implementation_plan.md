# SmartAttend Implementation Plan - Phase 4: Admin Dashboard

## Goal Description
With the foundation, Google Sign-In, and role-based routing completed (Phases 1-3), the next goal is to build out the **Admin Dashboard**. This allows administrators to approve pending users, assign them roles (Teacher/Student), manage courses, and manage subjects.

## Approved
The user has approved starting with the User Management module for the Admin Dashboard.

## Proposed Changes

### 1. Backend Admin API (`backend/routes/admin.py`)
- **[NEW]** `GET /api/admin/users`: Fetch all users.
- **[NEW]** `PATCH /api/admin/users/<id>`: Update user role and status (Approve/Suspend).
- Register the `admin_bp` blueprint in `app.py`.

### 2. Android Admin UI & Navigation (`android/.../ui/admin/`)
- **[MODIFY]** `activity_admin_dashboard.xml`: Add a `BottomNavigationView` and a `FragmentContainerView`.
- **[NEW]** `AdminNavGraph.xml`: Jetpack Navigation graph linking Admin fragments.
- **[NEW]** `UserManagementFragment.java` & `fragment_user_management.xml`: Layout with a `RecyclerView` to list users.
- **[NEW]** `UserAdapter.java`: Adapter for binding user data to the RecyclerView.

### 3. Android Data Layer
- **[MODIFY]** `ApiService.java`: Add `@GET("api/admin/users")` and `@PATCH("api/admin/users/{id}")`.
- **[NEW]** `AdminViewModel.java`: ViewModel to fetch users and handle state using `LiveData`.

## Verification Plan
1. Send an authenticated request to `/api/admin/users` via Postman/curl.
2. Launch the Android app, login as an Admin, and verify the Bottom Navigation appears.
3. Navigate to the User Management tab and verify the mock "pending" users are displayed and can be approved.
