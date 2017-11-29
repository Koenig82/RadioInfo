package Model;

import org.w3c.dom.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * The XMLParser class. Parses and creates objects from an XML document
 */
public class XMLParser {

    private DateTimeFormatter format =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("GMT"));
    private Document doc = null;

    public LinkedHashMap<Integer, Channel> channelData;
    public int activeChannelId = 0;

    private ImageIcon notAvailable;

    private ArrayList<LocalDate> days;

    /**
     * Constructor method for XMLParser. The constructor method does an
     * initial parse of the argument string. The parser uses a
     * DocumentBuilderFactory to produce an object tree. The object tree
     * is accessible through the Document "doc" variable.
     *
     * @param url String with url to radio API
     */
    public XMLParser(String url) throws Exception {
        BufferedImage temp = null;
        days = new ArrayList<>();
        try {
            temp = ImageIO.read(new File("src/images/"
                    + "notAvailable.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(temp != null){
            notAvailable = new ImageIcon(temp);
            notAvailable = ImageScaler.scaleImage(notAvailable);

        }else{
            throw new Exception("Unable to load default image");
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new URL(url).openStream());
        } catch (ParserConfigurationException e) {
            System.err.println("Error configuring parser");
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("Error parsing");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO Error during parsing");
            e.printStackTrace();
        }

    }

    /**
     * This method parses the XML document at a given url.
     *
     * @param url String with url to XML page
     */
    private void parseStream(String url){
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new URL(url).openStream());
        } catch (ParserConfigurationException e) {
            System.err.println("Error configuring parser");
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("Error parsing");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO Error during parsing");
            e.printStackTrace();
        }
    }

    /**
     * This method collects a LinkedHashMap of Channel objects from a parsed
     * XML document. The method assigns the channels id numbers as keys and
     * connects description and image from the document.
     * @return LinkedHashMap, The collection of channels.
     */
    public synchronized LinkedHashMap<Integer,Channel> collectChannels(){

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

                    String name = element.getAttribute("name");
                    String desc = element.getElementsByTagName
                            ("channeltype").item(0).getTextContent();

                    channelData.put(id, new Channel(name, id));
                    channelData.get(id).description = desc;
                    BufferedImage image = null;
                    try{
                        image = getChannelImage(element);
                    }catch(Exception e){
                        System.err.println(e.getMessage());
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

    /**
     * This method is used by collectChannels() to assign an image to a
     * channel from its image url.
     *
     * @param element , A parsed channel element
     * @return BufferedImage , The collected image (null when error)
     * @throws IOException , Exception thrown when image could not be loaded.
     * @throws MalformedURLException , Exeption thrown when url is malformed
     */
    private BufferedImage getChannelImage(Element element)throws Exception{
        BufferedImage image = null;
        try{
            URL url = new URL(element.getElementsByTagName("image").item(0)
                    .getTextContent());
            image = ImageIO.read(url);
            return image;
        } catch (MalformedURLException e) {
            throw new MalformedURLException("Malformed url when collecting " +
                    "station image");
        } catch (IOException e) {
            throw new IOException("Problem getting the station image");
        }
    }

    /**
     * This method is used by addProgram() to assign an image to a
     * program from its image url.
     *
     * @param element , A parsed program element
     * @return BufferedImage , The collected image (null when error)
     * @throws IOException , Exception thrown when image could not be loaded.
     * @throws MalformedURLException , Exeption thrown when url is malformed
     */
    private BufferedImage getProgramImage(Element element) throws Exception{
        BufferedImage image = null;
        try{
            URL url = new URL(element.getElementsByTagName("imageurl").item(0)
                    .getTextContent());
            image = ImageIO.read(url);
            return image;
        } catch (MalformedURLException e) {
            throw new Exception("Malformed url when collecting " +
                    "Program image");
        } catch (IOException e) {
            throw new Exception("Problem getting a program image");
        }
    }

    /**
     * This method updates the program table with information from a channel
     * object. First it assign two dates depending on the current hour of the
     * day to make sure the schedule always spans over 24 hours. Secondly,
     * the method parses the first day while also removing programs older than
     * 12 hours before finally adding programs from the second day if it is
     * within 12 hours from now.
     *
     * programs that meet the criteria are added with addProgram method.
     *
     * @param channel The channel object whos schedule to update
     */
    public synchronized void updateSchedule(Channel channel){

        this.activeChannelId = channel.id;
        days.clear();
        if(LocalDateTime.now().getHour() < 12){
            days.add(LocalDate.now().minusDays(1));
            days.add(LocalDate.now());
        }else{
            days.add(LocalDate.now());
            days.add(LocalDate.now().plusDays(1));
        }
        this.parseStream("http://api.sr" +
                ".se/api/v2/scheduledepisodes?channelid="
                + channel.id + "&date="+days.get(0));
        int totalPages = Integer.parseInt(doc.getElementsByTagName
                ("totalpages").item(0).getTextContent());
        System.out.println(totalPages);
        NodeList parsedPrograms;
        for (int page = 1; page <= totalPages; page++){

            this.parseStream("http://api.sr" +
                    ".se/v2/scheduledepisodes?channelid="
                    + channel.id +"&date="+days.get(0)+"&page="+page);
            parsedPrograms = doc.getElementsByTagName
                    ("scheduledepisode");
            for (int i = 0; i < parsedPrograms.getLength(); i++) {
                channel.programs.removeIf(p -> p.start.isBefore(LocalDateTime
                        .now().minusHours(12)));
                Node node = parsedPrograms.item(i);
                addProgram(channel, node);
            }
        }
        this.parseStream("http://api.sr" +
                ".se/api/v2/scheduledepisodes?channelid="
                + channel.id + "&date="+days.get(1));
        totalPages = Integer.parseInt(doc.getElementsByTagName
                ("totalpages").item(0).getTextContent());
        System.out.println(totalPages);
        for (int page = 1; page <= totalPages; page++){

            this.parseStream("http://api.sr" +
                    ".se/v2/scheduledepisodes?channelid="
                    + channel.id +"&date="+days.get(1)+"&page="+page);
            System.out.println("http://api.sr" +
                    ".se/v2/scheduledepisodes?channelid="
                    + channel.id +"&date="+days.get(1)+"&page="+page);
            parsedPrograms = doc.getElementsByTagName
                    ("scheduledepisode");
            for (int i = 0; i < parsedPrograms.getLength(); i++) {
                Node node = parsedPrograms.item(i);
                addProgram(channel, node);
            }
        }
    }

    /**
     * This method adds a program from a parsed Node to a channel if it is
     * within a 24 hour span. The method also assigns the program its image
     * if it has one.
     *
     * @param channel The channel object to assign the program to.
     * @param node The parsed Program node.
     */
    private void addProgram(Channel channel, Node node) {

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;

            LocalDateTime startDate = LocalDateTime.parse(element
                    .getElementsByTagName("starttimeutc").item(0)
                    .getTextContent(), format);

            LocalDateTime endDate = LocalDateTime.parse(element
                    .getElementsByTagName("endtimeutc").item(0)
                    .getTextContent(), format);

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
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }finally {
                    icon = convertToIcon(image);
                    program.image = icon;
                    channel.programs.add(program);
                }
            }
        }
    }

    /**
     * This method is used by the Tablelistener to associate a starting time
     * and a channel id to a specific program when a cell is clicked in the
     * program table.
     *
     * @param channelId The id of the current channel
     * @param startTime The Starting time of the program clicked.
     * @return Program object that matches the identifiers.
     */
    public Program getProgramByChannelIdAndStart(int channelId, LocalDateTime
            startTime){
        for (Program p: channelData.get(channelId).programs) {
            if(p.start.isEqual(startTime)){
                return p;
            }
        }
        return null;
    }

    /**
     * This method converts a BufferedImage to an ImageIcon. It uses a
     * static method from the ImageScaler class.
     *
     * @param image BufferedImage to convert
     * @return ImageIcon , The converted image(standard image if unavailable)
     */
    private ImageIcon convertToIcon(BufferedImage image){
        ImageIcon icon;
        if(image!=null){
            icon = new ImageIcon(image);
            icon = ImageScaler.scaleImage(icon);
        }else{
            icon = notAvailable;
        }
        return icon;
    }
}