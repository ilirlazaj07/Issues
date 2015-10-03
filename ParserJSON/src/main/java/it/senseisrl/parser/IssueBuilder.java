package it.senseisrl.parser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class IssueBuilder {

    private JSONObject documentoBucket;
    private JSONArray issues;
    private JFrame frame;
    private JTextArea area;
    private String pathXML = "";
    private String pathJSON = "";
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(IssueBuilder.class);
    private static final String ASSIGNEE = "mmatricardi";

    public IssueBuilder() {

    }

    private void inizializzaDefault() {
        documentoBucket = new JSONObject();

        JSONObject mile1 = new JSONObject();
        mile1.put("name", "M1");

        JSONObject item1 = new JSONObject();
        item1.put("name", "M2");

        JSONObject item2 = new JSONObject();
        item2.put("name", "M3");

        JSONArray array = new JSONArray();

        array.put(mile1);
        array.put(item1);
        array.put(item2);

        documentoBucket.put("milestones", array);

        JSONObject comp1 = new JSONObject();
        comp1.put("name", "api");

        JSONObject comp2 = new JSONObject();
        comp2.put("name", "ui");

        JSONArray components = new JSONArray();
        components.put(comp1);
        components.put(comp2);

        documentoBucket.put("milestones", array);

        JSONObject meta = new JSONObject();
        meta.put("default_milestone", "M2");
        meta.put("default_assignee", ASSIGNEE);
        meta.put("default_kind", "bug");
        meta.put("default_component", "ui");
        meta.put("default_version", "1.0");

        documentoBucket.put("meta", meta);

        documentoBucket.put("components", components);

        documentoBucket.put("attachments", new JSONArray());

        JSONArray arrayVersions = new JSONArray();
        JSONObject primaVersione = new JSONObject();
        primaVersione.put("name", "0.9");
        JSONObject secondaVersione = new JSONObject();
        secondaVersione.put("name", "1.0");

        arrayVersions.put(primaVersione);
        arrayVersions.put(secondaVersione);

        documentoBucket.put("versions", arrayVersions);

        documentoBucket.put("comments", new JSONArray());

    }

    public void creaFrame() {
        frame = new JFrame("Migrazione Tickets da Assembla");

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(0, 0, 500, 500);
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().setLayout(null);
        JButton bottone = new JButton("Carica XML");

        bottone.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                creaFileChooser(frame);
            }
        });
        area = new JTextArea();
        area.setSize(100, 100);
        area.setEditable(false);
        area.setBackground(Color.GRAY);

        bottone.setBounds(180, 150, 120, 60);
        frame.add(bottone);

    }

    private void creaFileChooser(JFrame jframe) {
        String fileScelto = File.separator;
        JFileChooser fileChooser = new JFileChooser(new File(fileScelto));
        int risultato = fileChooser.showSaveDialog(jframe);

        if (risultato == JFileChooser.APPROVE_OPTION) {

            pathXML = fileChooser.getSelectedFile().getPath();
            if (!pathXML.endsWith(".xml")) {
                area.setText("Formato non consentito");
                LOGGER.info("Formato non consentito.");
            } else {
                LOGGER.info("Il file scelto è: " + pathXML);
                try {
                    File file = new File(pathXML);
                    InputStream inputStream = new FileInputStream(file);
                    StringBuilder builder = new StringBuilder();
                    int ptr = 0;

                    while ((ptr = inputStream.read()) != -1) {
                        builder.append((char) ptr);
                    }

                    String xml = builder.toString();
                    LOGGER.info("Il file XML scelto è: " + xml);
                    JSONObject jsonObj = XML.toJSONObject(xml);
                    pathJSON = pathXML.replaceFirst(".xml", ".json");
                    FileWriter fileWriter = new FileWriter(pathJSON);
                    try (
                            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                        for (String split : jsonObj.toString().split(",")) {
                            LOGGER.info(split);
                            bufferedWriter.write(split);
                            bufferedWriter.write("\n");
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.debug("Errore scrittura file: " + pathXML);
                }
                inizializzaDefault();
                creaIssues();
            }
        } else if (risultato == JFileChooser.CANCEL_OPTION) {
            LOGGER.info("In attesa di caricamento XML");
        }
    }

    private void creaIssues() {
        issues = new JSONArray();
        Object oggetto = null;
        String json = "";
        try {

            int id_issue = 0;
            JSONParser parser = new JSONParser();
            try {
                oggetto = parser.parse(new FileReader(pathJSON));
                json = oggetto.toString();

            } catch (IOException | ParseException ex) {
                Logger.getLogger(IssueBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }

            JSONObject objJSON = new JSONObject(json);

            JSONArray arrayJSON = objJSON.getJSONArray("ticket");

            int n = arrayJSON.length();

            for (int i = n - 1; i >= 0; i--) {
                id_issue += 1;
                JSONObject importance = null;
                JSONObject kinds = null;
                JSONObject kind = null;
                JSONObject ticket = arrayJSON.getJSONObject(i);
                JSONObject dataCreazione = ticket.optJSONObject("created-on");

                JSONObject issue = new JSONObject();
                issue.put("created_on", dataCreazione.getString("content"));

                String stato = Util.getStato(ticket.optString("status-name"));
                issue.put("status", stato);

                if (ticket.toString().contains("priority")) {
                    importance = ticket.optJSONObject("priority");
                    issue.put("priority", Util.getPriorita(Integer.valueOf(importance.optString("content"))));
                } else {
                    issue.put("priority", "major");
                }

                issue.put("title", ticket.optString("summary"));

                issue.put("component", "api");

                issue.put("content", ticket.optString("description"));

                issue.put("voters", new JSONArray());

                issue.put("assignee", ASSIGNEE);

                issue.put("version", "1.0");

                if (ticket.toString().contains("CustomFields")) {
                    kinds = ticket.optJSONObject("CustomFields");
                    if (kinds.toString().contains("CustomField")) {
                        kind = kinds.optJSONObject("CustomField");
                        issue.put("kind", Util.getKindIssue(kind.optString("content")));
                    } else {
                        issue.put("kind", "bug");
                    }

                } else {
                    issue.put("kind", "bug");
                }

                issue.put("edited_on", "2015-09-09T11:11:08.835525+00:00");

                issue.put("milestone", "M2");

                issue.put("reporter", ASSIGNEE);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                Date updateDate = new Date();

                String update = df.format(updateDate.getTime());

                issue.put("updated_on", "2015-09-10 23:03:28.294");

                issue.put("id", id_issue);

                issues.put(issue);
            }

        } catch (JSONException | NumberFormatException ex) {
            Logger.getLogger(IssueBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        documentoBucket.put("issues", issues);

        documentoBucket.put("logs", new JSONArray());

        Util.scriviJSON(documentoBucket.toString());
    }
}
