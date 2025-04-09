package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    private static int appointmentID = 1;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        // TODO: Part 1
        // create_patient <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Create patient failed.");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        // extra check: check if the password is strong
        if (!isStrongPassword(password)) {
            System.out.println("Password does not meet the strength requirements. Please contain: ");
            System.out.println("a. At least 8 characters.");
            System.out.println("b. A mixture of both uppercase and lowercase letters.");
            System.out.println("c. A mixture of letters and numbers.");
            System.out.println("d. Inclusion of at least one special character, from “!”, “@”, “#”, “?”.");
            return;
        }

        // check 2: check if the username has been taken already
        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Patient patient = new Patient.PatientBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            patient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Create patient failed.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Create caregiver failed.");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        // extra check: check if the password is strong
        if (!isStrongPassword(password)) {
            System.out.println("Password does not meet the strength requirements. Please contain: ");
            System.out.println("a. At least 8 characters.");
            System.out.println("b. A mixture of both uppercase and lowercase letters.");
            System.out.println("c. A mixture of letters and numbers.");
            System.out.println("d. Inclusion of at least one special character, from “!”, “@”, “#”, “?”.");
            return;
        }

        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Create caregiver failed.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static boolean isStrongPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if ("!@#?".indexOf(c) >= 0) {
                hasSpecial = true;
            }
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private static void loginPatient(String[] tokens) {
        // TODO: Part 1
        // login_patient <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in, try again.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login patient failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login patient failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login patient failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in, try again.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login caregiver failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login caregiver failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login caregiver failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static Date parseDateToSqlDate(String inputDate) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy");
        java.util.Date date = inputFormat.parse(inputDate);
        return new Date(date.getTime());
    }

    private static String formatDateFromSqlDate(Date sqlDate) {
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd-yyyy");
        return outputFormat.format(sqlDate);
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // TODO: Part 2
        // search_caregiver_schedule <date>
        // check 1: the user must be logged in first
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first.");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again.");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try {
            String date = parseDateToSqlDate(tokens[1]).toString();
            PreparedStatement getAvailableCaregiver =
                    con.prepareStatement("SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username");
            getAvailableCaregiver.setString(1, date);
            ResultSet result1 = getAvailableCaregiver.executeQuery();
            while (result1.next()) {
                System.out.println(result1.getString(1));
            }

            PreparedStatement getVaccines = con.prepareStatement("SELECT * FROM Vaccines");
            ResultSet result2 = getVaccines.executeQuery();
            while (result2.next()) {
                System.out.println(result2.getString(1) + " " + result2.getInt(2));
            }
        } catch (SQLException e) {
            System.out.println("Please try again.");
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } finally {
            cm.closeConnection();
        }
    }


    private static void reserve(String[] tokens) {
        // TODO: Part 2
        // reserve <date> <vaccine>
        if (currentPatient == null) {
            // check 1: if the user is logged in
            if (currentCaregiver == null) {
                System.out.println("Please login first.");
            } else { // check 2: if the current user is a patient
                System.out.println("Please login as a patient.");
            }
            return;
        }

        // check 3: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again.");
            return;
        }

        String inputDate = tokens[1];
        String vaccineName = tokens[2];
        Vaccine vaccine;

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();


        try {
            String formattedDate = parseDateToSqlDate(inputDate).toString();
            PreparedStatement getAvailableCaregiver =
                    con.prepareStatement("SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username");
            getAvailableCaregiver.setString(1, formattedDate);
            ResultSet rs1 = getAvailableCaregiver.executeQuery();
            String availableCaregiver;
            if (rs1.next()) {
                availableCaregiver = rs1.getString(1);
            } else {
                System.out.println("No Caregiver is available.");
                return;
            }

            PreparedStatement getAvailableVaccine =
                    con.prepareStatement("SELECT Name, Doses FROM Vaccines WHERE Name = ? AND Doses > 0");
            getAvailableVaccine.setString(1, vaccineName);
            ResultSet rs2 = getAvailableVaccine.executeQuery();
            if (!rs2.next()) {
                System.out.println("Not enough available doses.");
                return;
            } else {
                try {
                    vaccine = new Vaccine.VaccineGetter(vaccineName).get();
                    vaccine.decreaseAvailableDoses(1);
                } catch (SQLException e) {
                    System.out.println("Please try again.");
                    e.printStackTrace();
                }
            }

            PreparedStatement maxIdStatement = con.prepareStatement("SELECT MAX(ID) FROM Appointments");
            ResultSet rsMaxId = maxIdStatement.executeQuery();
            if (rsMaxId.next()) {
                appointmentID = rsMaxId.getInt(1) + 1;
            } else {
                appointmentID = 1;
            }

            PreparedStatement addAppointment = con.prepareStatement("INSERT INTO Appointments (ID, Date, Caregiver, Patient, Vaccine) VALUES (?, ?, ?, ?, ?)");
            addAppointment.setInt(1, appointmentID);
            addAppointment.setString(2, formattedDate);
            addAppointment.setString(3, availableCaregiver);
            addAppointment.setString(4, currentPatient.getUsername());
            addAppointment.setString(5, vaccineName);
            addAppointment.executeUpdate();


            PreparedStatement getAppointment = con.prepareStatement(
                    "SELECT ID, Caregiver FROM Appointments WHERE Patient = ? ORDER BY Caregiver");
            getAppointment.setString(1, currentPatient.getUsername());
            ResultSet rs3 = getAppointment.executeQuery();

            int lastId = 0;
            String lastCaregiver = "";
            while (rs3.next()) {
                lastId = rs3.getInt(1);
                lastCaregiver = rs3.getString(2);
            }
            if (lastId != 0) {
                System.out.println("Appointment ID: " + lastId + ", Caregiver username: " + lastCaregiver);
            }

            // Remove availability
            PreparedStatement removeAvailability = con.prepareStatement(
                    "DELETE FROM Availabilities WHERE Time = ? AND Username = ?");
            removeAvailability.setString(1, formattedDate);
            removeAvailability.setString(2, availableCaregiver);
            removeAvailability.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Please try again.");
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } finally {
            cm.closeConnection();
        }
    }


    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String inputDate = tokens[1];

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try {
            String formattedDate = parseDateToSqlDate(inputDate).toString();
            // Check if the date is already available for this caregiver
            PreparedStatement checkAvailability = con.prepareStatement(
                    "SELECT COUNT(*) FROM Availabilities WHERE Username = ? AND Time = ?");
            checkAvailability.setString(1, currentCaregiver.getUsername());
            checkAvailability.setString(2, formattedDate);
            ResultSet rs = checkAvailability.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Availability for this date already exists.");
                return;  // Exit if the date is already present
            }

            currentCaregiver.uploadAvailability(Date.valueOf(formattedDate));
            System.out.println("Availability uploaded!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
        // cancel <appointment_id>
        // check 1: the user must be logged in
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first.");
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again.");
            return;
        }

        int appointmentId = Integer.parseInt(tokens[1]);
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try {
            // Step 1: Retrieve the appointment details
            PreparedStatement getAppointment = con.prepareStatement(
                    "SELECT ID, Patient, Caregiver, Vaccine, Date FROM Appointments WHERE ID = ?");
            getAppointment.setInt(1, appointmentId);
            ResultSet rs = getAppointment.executeQuery();

            if (!rs.next()) {
                System.out.println("No such appointment found.");
                return;
            }

            String patient = rs.getString("Patient");
            String caregiver = rs.getString("Caregiver");
            String vaccineName = rs.getString("Vaccine");
            Date date = rs.getDate("Date");

            // Step 2: Check if the logged-in user is related to the appointment
            if ((currentPatient != null && !currentPatient.getUsername().equals(patient)) ||
                    (currentCaregiver != null && !currentCaregiver.getUsername().equals(caregiver))) {
                System.out.println("You do not have permission to cancel this appointment.");
                return;
            }

            // Step 3: Delete the appointment
            PreparedStatement deleteAppointment = con.prepareStatement("DELETE FROM Appointments WHERE ID = ?");
            deleteAppointment.setInt(1, appointmentId);
            deleteAppointment.executeUpdate();

            // Step 4: Increment the vaccine dose
            PreparedStatement addDose = con.prepareStatement("UPDATE Vaccines SET Doses = Doses + 1 WHERE Name = ?");
            addDose.setString(1, vaccineName);
            addDose.executeUpdate();

            //String formattedDate = parseDateToSqlDate(date).toString();
            // Step 5: Add back the availability
            PreparedStatement addAvailability = con.prepareStatement("INSERT INTO Availabilities (Time, Username) VALUES (?, ?)");
            addAvailability.setDate(1, date);
            addAvailability.setString(2, caregiver);
            addAvailability.executeUpdate();

            System.out.println("Appointment cancelled successfully.");

        } catch (SQLException e) {
            System.out.println("Error occurred while cancelling the appointment.");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        // TODO: Part 2
        // check 1: the user must be logged in
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first.");
        }
        // check 2: the length for tokens need to be exactly 1 to include all information (operation name)
        if (tokens.length != 1) {
            System.out.println("Please try again.");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        if (currentCaregiver != null) {
            try {
                String caregiver = currentCaregiver.getUsername();
                PreparedStatement getCaregiverAppointments = con.prepareStatement("SELECT ID, " +
                        "Vaccine, Date, Patient FROM Appointments " +
                        "WHERE Caregiver = ? ORDER BY ID");
                getCaregiverAppointments.setString(1, caregiver);
                ResultSet rs = getCaregiverAppointments.executeQuery();
                while (rs.next()) {
                    System.out.println(rs.getInt(1) + " " + rs.getString(2) +
                            " " + rs.getString(3) + " " + rs.getString(4));
                }
            } catch (SQLException e) {
                System.out.println("Please try again.");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        } else {
            try {
                String patient = currentPatient.getUsername();
                PreparedStatement getPatientAppointments = con.prepareStatement("SELECT ID, " +
                        "Vaccine, Date, Caregiver FROM Appointments " +
                        "WHERE Patient = ? ORDER BY ID");
                getPatientAppointments.setString(1, patient);
                ResultSet rs = getPatientAppointments.executeQuery();
                while (rs.next()) {
                    System.out.println(rs.getInt(1) + " " + rs.getString(2) +
                            " " + rs.getString(3) + " " + rs.getString(4));
                }
            } catch (SQLException e) {
                System.out.println("Please try again.");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        }
    }

    private static void logout(String[] tokens) {
        // TODO: Part 2
        // logout
        // check 1: check if the user is not logged in
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first.");
            return;
        }
        // check 2: the length for tokens need to be exactly 1 to include all information (with the operation name)
        if (tokens.length != 1) {
            System.out.println("Please try again.");
            return;
        }
        currentCaregiver = null;
        currentPatient = null;
        System.out.println("Successfully logged out.");
    }


}
