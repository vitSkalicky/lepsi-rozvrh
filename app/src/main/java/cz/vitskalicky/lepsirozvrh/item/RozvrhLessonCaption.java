package cz.vitskalicky.lepsirozvrh.item;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RozvrhLessonCaption {
    private String caption;
    private String beginTime; //10:00
    private String endTime; //10:45

    public RozvrhLessonCaption(String caption, String beginTime, String endTime) {
        this.caption = caption;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    //<editor-fold desc="Getters">
    public String getCaption() {
        return caption;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public String getEndTime() {
        return endTime;
    }
    //</editor-fold>\

    /**
     * Parses give xml node.
     * Format: ({@code node} is the {@code <hod>} tag.
     * <code>
 *     <hod>
 *         <caption>4</caption>
 *         <begintime>10:55</begintime>
 *         <endtime>11:40</endtime>
 *      </hod>
     * </code>
     * @param node
     */
    public RozvrhLessonCaption(Node node){
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            switch (item.getNodeName()){
                case "caption":
                    caption = item.getTextContent();
                    break;
                case "begintime":
                    beginTime = item.getTextContent();
                    break;
                case "endtime":
                    endTime = item.getTextContent();
                    break;
            }
        }
    }
}
