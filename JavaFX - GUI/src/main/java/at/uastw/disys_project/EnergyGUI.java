package at.uastw.disys_project;

import at.uastw.disys_project.dto.CurrentPercentageEntity;
import at.uastw.disys_project.dto.TotalEnergyBetweenDates;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EnergyGUI {

    @FXML private Label labelCommunityPercent;
    @FXML private Label labelGridPercent;
    @FXML private DatePicker startDate;
    @FXML private TextField startTime;
    @FXML private DatePicker endDate;
    @FXML private TextField endTime;
    @FXML private Label labelProduced;
    @FXML private Label labelUsed;
    @FXML private Label labelGridUsed;

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();


    @FXML
    protected void handleRefresh() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/energy/current"))
                .build();



        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<CurrentPercentageEntity>>() {}.getType();
                        java.util.List<CurrentPercentageEntity> dataList = gson.fromJson(json, listType);

                        if (dataList != null && !dataList.isEmpty()) {
                            CurrentPercentageEntity latest = dataList.get(dataList.size() - 1);

                            double communityDepleted = latest.getCommunityDepleted();
                            double gridPortion = latest.getGridPortion();

                            Platform.runLater(() -> {
                                labelCommunityPercent.setText(String.format("%.2f kWh verbraucht", communityDepleted));
                                labelGridPercent.setText(String.format("%.2f %% aus Netz", gridPortion));
                            });
                        } else {
                            Platform.runLater(() -> {
                                labelCommunityPercent.setText("Keine aktuellen Daten gefunden!");
                                labelGridPercent.setText("");
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }



    @FXML
    protected void handleShowData() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        String startTimeText = startTime.getText();
        String endTimeText = endTime.getText();

        if (start == null || end == null || startTimeText.isEmpty() || endTimeText.isEmpty()) {
            System.out.println("Bitte Datum und Uhrzeit eingeben.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String dateStart = start.atTime(LocalTime.parse(startTimeText)).format(formatter);
        String dateEnd = end.atTime(LocalTime.parse(endTimeText)).format(formatter);

        String url = "http://localhost:8080/energy/historical?dateStart=" + dateStart + "&dateEnd=" + dateEnd;
        System.out.println("Sende Request an:\n" + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();


        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        TotalEnergyBetweenDates totals =
                                gson.fromJson(json, TotalEnergyBetweenDates.class);

                        if (totals == null) {
                            System.out.println("Keine Daten aus /historical erhalten.");
                            return;
                        }

                        double produced = totals.getTotalCommunityProduced();
                        double used     = totals.getTotalCommunityUsed();
                        double grid     = totals.getTotalGridUsed();

                        Platform.runLater(() -> {
                            labelProduced.setText(String.format("%.3f kWh", produced));
                            labelUsed.setText    (String.format("%.3f kWh", used));
                            labelGridUsed.setText(String.format("%.3f kWh", grid));
                        });

                    } catch (Exception e) {
                        System.out.println("Fehler beim Parsen von /historical:");
                        e.printStackTrace();
                    }
                });


    }
}
