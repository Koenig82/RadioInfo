package Model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
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

    private ArrayList<LocalDate> days;

    /**
     * Constructor method for XMLParser. Initializes needed resources.
     */
    public XMLParser() throws Exception {

        days = new ArrayList<>();
    }

    /**
     * This method parses the XML document at a given url. The parser uses a
     * DocumentBuilderFactory to produce an object tree, stored in the "doc"
     * variable.
     *
     * @param url String with url to XML page
     */
    public void parseStream(String url){
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

                    channelData.put(id, new Channel(name, id));

                    if(element.getElementsByTagName("channeltype").item(0) != null){
                        channelData.get(id).description = element.getElementsByTagName("channeltype").item(0).getTextContent();
                    }
                    if(element.getElementsByTagName("image").item(0) != null){
                        channelData.get(id).imageUrl = element.getElementsByTagName("image").item(0).getTextContent();
                    }
                }
            }
        }
        return channelData;
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
        for (int page = 1; page <= totalPages; page++){

            this.parseStream("http://api.sr" +
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

                if(element.getElementsByTagName("description").item(0) != null){
                    program.description = element.getElementsByTagName("description").item(0).getTextContent();
                }

                if(element.getElementsByTagName("imageurl").item(0) != null){
                    program.imageUrl = element.getElementsByTagName("imageurl").item(0).getTextContent();
                }
                channel.programs.add(program);
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
}