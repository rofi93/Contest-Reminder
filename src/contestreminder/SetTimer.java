/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contestreminder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import org.json.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author USER PC
 */
public class SetTimer {

    boolean codeforces, hackerrank, topcoder, codechef, hackerearth, atcoder, JSONParsed;
    URL api;
    URLConnection connection;
    String JSON, AC;
    JSONObject contests, result;
    JSONArray upcoming;
    ArrayList<OnlineJudges> contestList;

    public class OnlineJudges {

        String name;
        String startTime;
        String platform;
        String url;
    }

    SetTimer(boolean cf, boolean hr, boolean tc, boolean cc, boolean he, boolean ac) throws UnsupportedOperationException, IOException, URISyntaxException, MalformedURLException, JSONException {

        codeforces = cf;
        hackerrank = hr;
        topcoder = tc;
        codechef = cc;
        hackerearth = he;
        atcoder = ac;
        JSONParsed = false;
        getContestTime();
    }

    private void getContestTime() throws IOException, URISyntaxException, MalformedURLException, JSONException {

        int i, index = -1;
        long difference, delay = -1;
        String contestURL = null;
        if (!JSONParsed) {
            getJSON();
            getAC();
        }

        for (i = 0; i < contestList.size(); i++) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, d MMM yyyy HH:mm");
            LocalDateTime localContest = LocalDateTime.parse(contestList.get(i).startTime, formatter);
            LocalDateTime localCurrent = LocalDateTime.now();
            ZonedDateTime contestTime;
            if (contestList.get(i).platform.equalsIgnoreCase("ATCODER")) {
                contestTime = ZonedDateTime.of(localContest, ZoneId.of("+09:00"));
            } else {
                contestTime = ZonedDateTime.of(localContest, ZoneId.of("+05:30"));
            }

            ZonedDateTime currentTime = ZonedDateTime.of(localCurrent, ZoneId.systemDefault());
            localContest = contestTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            localCurrent = currentTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

            difference = Duration.between(currentTime, contestTime).get(ChronoUnit.SECONDS) * 1000;
            if ((delay == -1 || difference < delay) && difference>0) {
                index = i;
                delay = difference;
                contestURL = contestList.get(i).url;
            }
        }
        if (index != -1) {
            contestList.remove(index);
        }
        doScheduledWork(delay, contestURL);
    }

    private void doScheduledWork(long delay, String Url) {

        java.util.Timer myTimer = new java.util.Timer();
        TimerTask newTask;

        if (delay > 600000) {
            delay -= 600000;
        } else {
            delay = 3000;
        }

        newTask = new TimerTask() {
            @Override
            public void run() throws UnsupportedOperationException {

                try {

                    String fileName = "audio.wav";
                    File yourFile = new File(fileName);

                    Clip audioClip = AudioSystem.getClip();
                    audioClip.open(AudioSystem.getAudioInputStream(yourFile));
                    audioClip.start();
                } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
                    Logger.getLogger(SetTimer.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (Desktop.isDesktopSupported()) {

                    try {
                        Desktop.getDesktop().browse(new URI(Url));
                    } catch (IOException | URISyntaxException ex) {
                        Logger.getLogger(SetTimer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                myTimer.cancel();
                myTimer.purge();
                try {
                    getContestTime();
                } catch (IOException | URISyntaxException | JSONException ex) {
                    Logger.getLogger(SetTimer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        myTimer.schedule(newTask, delay);
    }

    private boolean checkContest(String name) {

        if (name.equalsIgnoreCase("CODEFORCES")) {
            return codeforces;
        }
        if (name.equalsIgnoreCase("HACKERRANK")) {
            return hackerrank;
        }
        if (name.equalsIgnoreCase("TOPCODER")) {
            return topcoder;
        }
        if (name.equalsIgnoreCase("CODECHEF")) {
            return codechef;
        }
        if (name.equalsIgnoreCase("HACKEREARTH")) {
            return hackerearth;
        }
        if (name.equalsIgnoreCase("ATCODER")) {
            return atcoder;
        }
        return false;
    }

    private void getJSON() throws MalformedURLException, IOException, JSONException {

        api = new URL("http://contesttrackerapi.herokuapp.com/");
        connection = api.openConnection();
        InputStream input = connection.getInputStream();
        JSONParsed = true;
        JSON = "";
        int x;

        while ((x = input.read()) != -1) {
            JSON += (char) x;
        }

        contestList = new ArrayList<>();
        contests = new JSONObject(JSON);
        result = new JSONObject();
        upcoming = new JSONArray();
        result = contests.getJSONObject("result");
        upcoming = result.getJSONArray("upcoming");

        for (x = 0; x < upcoming.length(); x++) {

            contests = upcoming.getJSONObject(x);
            OnlineJudges sample = new OnlineJudges();

            sample.name = contests.getString("Name");
            sample.platform = contests.getString("Platform");
            sample.startTime = contests.getString("StartTime");
            sample.url = contests.getString("url");

            if (checkContest(sample.platform)) {

                if (sample.platform.equalsIgnoreCase("CODEFORCES")) {
                    sample.url = sample.url.substring(0, 29) + "s" + sample.url.substring(29);
                }
                boolean add = contestList.add(sample);
                if (!add) {
                    System.out.println("Error adding contest");
                }
            }
        }
    }

    private void getAC() throws IOException {

        Document doc = Jsoup.connect("https://atcoder.jp/").userAgent(Desktop.getDesktop().toString()).get();
        Element table = doc.getElementsByClass("table-responsive").get(1);
        Elements contestStartTime = table.getElementsByTag("td");
        int cnt = 1;
        OnlineJudges sample = new OnlineJudges();
        for (Element i : contestStartTime) {

            if (cnt % 2 == 1) {
                String date = i.text();
                LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                String newDate = dateTime.format(DateTimeFormatter.ofPattern("E, d MMM yyyy HH:mm"));
                sample.startTime = newDate;
            } else {
                Elements temp = i.getElementsByTag("a");
                for (Element j : temp) {
                    sample.url = j.attr("href");
                }
                sample.name = i.text();
            }
            if (cnt % 2 == 0 && checkContest("ATCODER")) {
                sample.platform = "ATCODER";
                boolean add = contestList.add(sample);
                if (!add) {
                    System.out.println("Error adding contest");
                }
            }
            cnt++;
        }
    }

    public static void main(String[] args) {

    }

}
