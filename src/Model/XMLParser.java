package Model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

/**
 * The XMLParser class
 */
public class XMLParser {

    private DateTimeFormatter format =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("GMT"));
    public NodeList parsedPrograms = null;
    public Document doc = null;
    public LinkedHashMap<Integer, Channel> channelData;
    public int activeChannelId = 0;

    private ImageIcon notAvaliable;

    /**
     * Constructor method for XMLParser
     * @param url String with url to radio API
     */
    public XMLParser(String url) throws Exception {
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File("src/images/"
                    + "notAvaliable.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(temp != null){
            notAvaliable = new ImageIcon(temp);
            notAvaliable = ImageScaler.scaleImage(notAvaliable);

        }else{
            throw new Exception("Unable to load default image");
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new URL(url).openStream());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void parseStream(String url){
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new URL(url).openStream());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedHashMap<Integer,Channel> collectChannels(){

        LinkedHashMap<Integer, Channel> channelData = new LinkedHashMap<>();
        int totalPages = Integer.parseInt(this.doc.getElementsByTagName
                ("totalpages").item(0).getTextContent());
        NodeList channels;
        for (int p = 1; p <= totalPages; p++) {
            parseStream("http://api.sr.se/api/v2/channels/?page=" + p);
            channels = this.doc.getElementsByTagName("channel");
            for (int c = 0; c < channels.getLength(); c++) {
                Node node = channels.item(c);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    int id = Integer.parseInt(element.getAttribute("id"));

                    String scheduleUrl = getChannelSchedule(element);
                    String name = element.getAttribute("name");
                    String desc = element.getElementsByTagName
                            ("channeltype").item(0).getTextContent();

                    channelData.put(id, new Channel(name, id, scheduleUrl));
                    channelData.get(id).description = desc;
                    BufferedImage image;
                    try{
                        image = getChannelImage(element);
                    }catch(IOException e){
                        System.err.println(e.getMessage());
                        continue;
                    }
                    ImageIcon icon = convertToIcon(image);
                    if(icon != null){
                        channelData.get(id).image =
                                ImageScaler.scaleImage(icon);
                    }
                }
            }
        }
        return channelData;
    }

    private String getChannelSchedule(Element element){
        String scheduleUrl = null;
        try{
            scheduleUrl = element.getElementsByTagName
                    ("scheduleurl").item(0).getTextContent();
        }
        catch(NullPointerException e){

            return null;
        }
        return scheduleUrl;
    }

    private BufferedImage getChannelImage(Element element)throws IOException{
        BufferedImage image;
        try{
            URL url = new URL(element.getElementsByTagName("image").item(0).getTextContent());
            image = ImageIO.read(url);
            return image;
        } catch (MalformedURLException e) {
            throw new MalformedURLException("Malformed url when collecting " +
                    "station image");
        } catch (IOException e) {
            throw new IOException("Problem getting the station image");
        }
    }

    private BufferedImage getProgramImage(Element element) throws IOException{
        BufferedImage image;
        try{
            URL url = new URL(element.getElementsByTagName("imageurl").item(0)
                    .getTextContent());
            image = ImageIO.read(url);
            return image;
        } catch (MalformedURLException e) {
            throw new MalformedURLException("Malformed url when collecting " +
                    "Program image");
        } catch (IOException e) {
            throw new IOException("Problem getting a program image");
        }
    }

    public synchronized void updateSchedule(Channel channel){
        this.activeChannelId = channel.id;
        if(channel.schedule != null){
            this.parseStream(channel.schedule);
            int totalPages = Integer.parseInt(this.doc.getElementsByTagName
                    ("totalpages").item(0).getTextContent());
            for (int page = 1; page <= totalPages; page++){
                this.parseStream("http://api.sr" +
                        ".se/v2/scheduledepisodes?channelid="
                        + channel.id + "&page="+page);
                parsedPrograms = doc.getElementsByTagName("scheduledepisode");

                for (int i = 0; i < parsedPrograms.getLength(); i++) {
                    channel.programs.removeIf(p -> p.start.isBefore(LocalDateTime.now().minusHours(12)));
                    Node node = parsedPrograms.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        LocalDateTime startDate = LocalDateTime.parse(element.getElementsByTagName("starttimeutc").item(0).getTextContent(), format);
                        LocalDateTime endDate = LocalDateTime.parse(element.getElementsByTagName("endtimeutc").item(0).getTextContent(), format);
                        if(endDate.isBefore(LocalDateTime.now().plusHours(12))){
                            Program program = new Program(element
                                    .getElementsByTagName("title")
                                    .item(0).getTextContent(), startDate,
                                    endDate);
                            program.description = element.getElementsByTagName
                                    ("description").item(0).
                                    getTextContent();

                            BufferedImage image = null;
                            ImageIcon icon = null;
                            try {
                                image = getProgramImage(element);
                            } catch (IOException e) {
                                System.err.println(e.getMessage());
                            }catch (MalformedURLException e) {
                                System.err.println(e.getMessage());
                            }finally {
                                icon = convertToIcon(image);
                                program.image = icon;
                                channel.programs.add(program);
                            }
                        }
                    }
                }
            }
        }
    }
    public Program getProgramByChannelIdAndStart(int channelId, LocalDateTime
            startTime){
        for (Program p: channelData.get(channelId).programs) {
            if(p.start.isEqual(startTime)){
                return p;
            }
        }
        return null;
    }

    private ImageIcon convertToIcon(BufferedImage image){
        ImageIcon icon;
        if(image!=null){
            icon = new ImageIcon(image);
            icon = ImageScaler.scaleImage(icon);
        }else{
            icon = notAvaliable;
        }
        return icon;
    }
}




//package Model;
//
//        import org.w3c.dom.Document;
//        import org.w3c.dom.Element;
//        import org.w3c.dom.Node;
//        import org.w3c.dom.NodeList;
//        import org.xml.sax.SAXException;
//
//        import javax.imageio.ImageIO;
//        import javax.swing.*;
//        import javax.xml.parsers.DocumentBuilder;
//        import javax.xml.parsers.DocumentBuilderFactory;
//        import javax.xml.parsers.ParserConfigurationException;
//        import java.awt.image.BufferedImage;
//        import java.io.File;
//        import java.io.IOException;
//        import java.net.MalformedURLException;
//        import java.net.URL;
//        import java.time.LocalDateTime;
//        import java.time.ZoneId;
//        import java.time.format.DateTimeFormatter;
//        import java.util.LinkedHashMap;
//
//public class XMLParser {
//
//    private DateTimeFormatter format = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("GMT"));
//    public NodeList parsedPrograms = null;
//    public Document doc = null;
//    public LinkedHashMap<Integer, Channel> channelData;
//    public int activeChannelId = 0;
//
//    /**
//     * Constructor method for the Model.XMLParser
//     * @param url String with url to radio API
//     */
//    public XMLParser(String url) {
//
//
//        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//        try {
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            doc = dBuilder.parse(new URL(url).openStream());
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public void parseStream(String url){
//        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//        //DocumentBuilder dBuilder = null;
//        try {
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            doc = dBuilder.parse(new URL(url).openStream());
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public LinkedHashMap<Integer,Channel> collectChannels(){
//
//        LinkedHashMap<Integer, Channel> channelData = new LinkedHashMap<>();
//        int totalPages = Integer.parseInt(this.doc.getElementsByTagName
//                ("totalpages").item(0).getTextContent());
//        NodeList channels;
//        for (int p = 1; p <= totalPages; p++) {
//            parseStream("http://api.sr.se/api/v2/channels/?page=" + p);
//            channels = this.doc.getElementsByTagName("channel");
//
//            for (int c = 0; c < channels.getLength(); c++) {
//
//                Node node = channels.item(c);
//
//                if (node.getNodeType() == Node.ELEMENT_NODE) {
//
//                    Element element = (Element) node;
//                    String scheduleUrl = getChannelSchedule(element);
//                    BufferedImage image = getChannelImage(element);
//                    ImageIcon icon = null;
//
//                    icon = convertToIcon(image);
//
//
//                    String desc = element.getElementsByTagName
//                            ("channeltype").item(0).getTextContent();
//                    String name = element.getAttribute("name");
//                    int id = Integer.parseInt(element.getAttribute("id"));
//                    channelData.put(id, new Channel(name, id, scheduleUrl));
//                    if(icon != null){
//                        channelData.get(id).image = ImageScaler.scaleImage(icon);
//                    }
//                    channelData.get(id).description = desc;
//
//                }
//            }
//        }
//        return channelData;
//    }
//
//    private ImageIcon convertToIcon(BufferedImage image){
//        ImageIcon icon;
//        if(image!=null){
//            icon = new ImageIcon(image);
//        }else{
//            try{
//                image = ImageIO.read(new File("src/images/"
//                        + "notAvaliable.jpg"));
//                return new ImageIcon(image);
//            } catch (IOException e) {
//                System.err.println("Unable to load default " +
//                        "radio image");
//                e.printStackTrace();
//                return null;
//            }
//        }
//        return null;
//    }
//
//    private String getChannelSchedule(Element element){
//        String scheduleUrl = null;
//        try{
//            scheduleUrl = element.getElementsByTagName
//                    ("scheduleurl").item(0).getTextContent();
//        }
//        catch(NullPointerException e){
//
//            return null;
//        }
//        return scheduleUrl;
//    }
//
//    private BufferedImage getChannelImage(Element element){
//        BufferedImage image = null;
//        try{
//            URL url = new URL(element.getElementsByTagName("image").item(0)
//                    .getTextContent());
//            image = ImageIO.read(url);
//        } catch (MalformedURLException e) {
//            System.err.println("Malformed url when collecting station image");
//            e.printStackTrace();
//            return null;
//        } catch (IOException e) {
//            System.err.println("Problem getting the station image");
//            e.printStackTrace();
//            return null;
//        }
//        return image;
//    }
//
//    private BufferedImage getProgramImage(Element element){
//        BufferedImage image = null;
//        try{
//            URL url = new URL(element.getElementsByTagName("imageurl").item(0)
//                    .getTextContent());
//            image = ImageIO.read(url);
//        } catch (MalformedURLException e) {
//            System.err.println("Malformed url when collecting Program image");
//            e.printStackTrace();
//            return null;
//        } catch (IOException e) {
//            System.err.println("Problem getting a program image");
//            e.printStackTrace();
//            return null;
//        }
//        return image;
//    }
//
//    public void updateSchedule(Channel channel){
//
//        this.activeChannelId = channel.id;
//
//        if(channel.schedule != null){
//            this.parseStream(channel.schedule);
//            int totalPages = Integer.parseInt(this.doc.getElementsByTagName
//                    ("totalpages").item(0).getTextContent());
//            for (int page = 1; page <= totalPages; page++){
//                this.parseStream("http://api.sr.se/v2/scheduledepisodes?channelid="
//                        + channel.id + "&page=" + page);
//                parsedPrograms = doc.getElementsByTagName
//                        ("scheduledepisode");
//
//                for (int i = 0; i < parsedPrograms.getLength(); i++) {
//
//                    channel.programs.removeIf(p -> p.start.isBefore
//                            (LocalDateTime.now().minusHours(12)));
//
//                    Node node = parsedPrograms.item(i);
//                    if (node.getNodeType() == Node.ELEMENT_NODE) {
//                        Element element = (Element) node;
//                        LocalDateTime startDate = LocalDateTime.parse(element.getElementsByTagName("starttimeutc").item(0).getTextContent(), format);
//                        LocalDateTime endDate = LocalDateTime.parse(element.getElementsByTagName("endtimeutc").item(0).getTextContent(), format);
//                        if(endDate.isBefore(LocalDateTime.now().plusHours(12))) {
//                            Program program = new Program(element
//                                    .getElementsByTagName("title")
//                                    .item(0).getTextContent(), startDate,
//                                    endDate);
//                            program.description = element.getElementsByTagName
//                                    ("description").item(0).
//                                    getTextContent();
//
//                            BufferedImage image = getProgramImage(element);
//                            ImageIcon icon;
//                            icon = convertToIcon(image);
//                            program.image = ImageScaler.scaleImage(icon);
//                            channel.programs.add(program);
//
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public Program getProgramByChannelIdAndStart(int channelId, LocalDateTime
//            starttime){
//        for (Program p: channelData.get(channelId).programs) {
//            if(p.start == starttime){
//                return p;
//            }
//        }
//        return null;
//    }
//}
//*/