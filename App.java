import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.util.*;
import javax.swing.table.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileWriter;
import java.io.*;

class App extends JFrame implements ActionListener {

    private JMenuBar mbar;
    private JMenu menu;
    private JMenuItem save;
    private JTextField search;
    private JButton searchBtn;
    private JTable tbl;
    private DefaultTableModel modl;
    private JPanel addPanel;
    private Font font1;
    private HashMap<String, ArrayList<String>> info;
    private String keyword;

    public App() {
        info = new HashMap<String, ArrayList<String>>();
        mbar = new JMenuBar();
        menu = new JMenu("File");
        save = new JMenuItem("Save");
        save.addActionListener(this);
        menu.add(save);
        mbar.add(menu);
        setJMenuBar(mbar);
        font1 = new Font("Monospaced", Font.PLAIN, 15);
        setTitle("Job Searching Application");
        setSize(1000, 480);
        SearchBar();
        table();
        setVisible(true);
    }

    public void SearchBar() {
        addPanel = new JPanel();
        search = new JTextField(20);
        search.setPreferredSize(new Dimension(120, 40));
        search.setFont(font1);
        addPanel.add(search);
        searchBtn = new JButton("SEARCH");
        searchBtn.setPreferredSize(new Dimension(120, 40));
        addPanel.add(searchBtn);
        searchBtn.setFont(font1);
        searchBtn.addActionListener(this);
    }

    public void table() {
        String data[][] = {};
        modl = new DefaultTableModel();
        modl.addColumn("Job Title");
        modl.addColumn("Company Name");
        modl.addColumn("Education");
        modl.addColumn("Location");
        modl.addColumn("Job Link");
        tbl = new JTable(modl) {

            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                return false;
            }
        };

        tbl.setGridColor(Color.black);
        tbl.setRowHeight(30);
        tbl.setBorder(BorderFactory.createLineBorder(Color.black));
        JScrollPane jbl = new JScrollPane(tbl);
        jbl.setPreferredSize(new Dimension(950, 350));
        addPanel.add(jbl);
        add(addPanel);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == searchBtn) {
            keyword = null;
            modl.setRowCount(0);
            keyword = search.getText().toString();
            Scrap(keyword);
            for (String key : info.keySet()) {
                String title = key;
                String cname = (String) info.get(key).toArray()[0];
                String location = (String) info.get(key).toArray()[1];
                String education = (String) info.get(key).toArray()[2];
                String url = (String) info.get(key).toArray()[3];
                modl.addRow(new Object[] { title, cname, location, education, url });
            }
            if (modl.getRowCount() == 0) {
                modl.addRow(new Object[] { "RESULT NOT FOUND!" });
            }
        } else if (e.getSource() == save) {
            saveFile();
        }
    }

    public static HttpURLConnection getconnection(String link) throws IOException {

        String userAgent = "Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Mobile Safari/537.36";
        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", userAgent);
        return conn;

    }

    private static InputStream getInputStream(URLConnection conn) throws IOException {
        return conn.getInputStream();
    }

    private static String readToEnd(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = " ";
        while ((line = br.readLine()) != null) {
            content.append(line).append("\r\n");
        }
        return content.toString();
    }

    public String get(String link) throws IOException {
        return readToEnd(getInputStream(getconnection(link)));
    }

    public void Scrap(String keyword) {
        String link = "https://www.kumarijob.com/search?csrf_test_name=dc0fd8639284f7d81d9cbddcd79b276c&keywords=";
        try {
            link = link + keyword;
            String content = get(link);
            String regEx = "<div class=\\\"card-header text-uppercase\\\">(.*?)<\\/div>\\s<div class=\\\"card-body\\\">\\s<table id=\\\"can-details\\\">\\s<tr>\\s<td><i class=\\\"far fa-building\\\"><\\/i><\\/td>\\s<td class=\\\"font-black\\\">Company\\s<\\/td>\\s<td class=\\\"font-15\\\">\\s:(.*?)<\\/td>\\s<\\/tr>\\s<tr>\\s<td><i class=\\\"fas fa-user-graduate\\\"><\\/i><\\/td> <td class=\\\"font-black\\\">Education <\\/td>\\s<td class=\\\"font-15\\\">\\s:(.*?)<\\/td>\\s<\\/tr>\\s<tr>\\s<td><i class=\\\"fas fa-map-marker-alt\\\"><\\/i><\\/td>\\s<td class=\\\"font-black\\\">Location <\\/td>\\s<td class=\\\"font-15\\\">\\s:(.*?)<\\/td>\\s<\\/tr>\\s<\\/table>\\s<\\/div>\\s<div class=\\\"card-footer border-0 text-center p-0\\\">\\s<ul class=\\\"f-dtls pl-0 my-2\\\"> <li class=\\\"d-inline px-md-3\\\"><i class=\\\"far fa-eye\\\"><\\/i>(.*?)<\\/li>\\s<li class=\\\"d-inline px-2 px-md-5\\\"><i class=\\\"fas fa-history\\\"><\\/i>(.*?)<\\/li>\\s<li class=\\\"d-inline p-0 px-md-3 view-resume\\\"><a\\shref=\\\"(.*?)\\\"";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(content);
            info.clear();
            while (matcher.find()) {
                ArrayList<String> desc = new ArrayList<String>();
                desc.add(matcher.group(2));
                desc.add(matcher.group(3));
                desc.add(matcher.group(4));
                desc.add(matcher.group(7));
                info.put(matcher.group(1), desc);
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }

    }

    public void saveFile() {
        try {
            FileWriter csvWriter = new FileWriter("Saved/" + keyword + ".csv");
            for (String key : info.keySet()) {

                String title = key;
                String cname = (String) info.get(key).toArray()[0];
                String location = (String) info.get(key).toArray()[1];
                String education = (String) info.get(key).toArray()[2];
                String url = (String) info.get(key).toArray()[3];

                csvWriter.append(title);
                csvWriter.append(",");
                csvWriter.append(cname);
                csvWriter.append(",");
                csvWriter.append(location);
                csvWriter.append(",");
                csvWriter.append(education);
                csvWriter.append(",");
                csvWriter.append(url);
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public static void main(String[] args) {
        new App();
    }
}